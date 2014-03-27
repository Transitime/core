/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.ipc.jms;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.management.JMSManagementHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;

/**
 * This class is a intended to make JMS easy to use. This class
 * is hornetq specific. If another JMS implementation is to be
 * used then the class would need to be modified. 
 * 
 * A key feature of this class is that it allows topics and queues
 * to be created dynamically. This goes against the normal idea
 * that JMS queues and topics should managed administratively, using
 * management tools. But for some applications this would be too 
 * burdensome. The user would have to additionally know about the
 * administration tools as well. Given that might be creating quite
 * a few AVL feeds, each one being a separate topic, this could be
 * a real nuisance.
 * 
 * To be able to dynamically create topics had to modify the hornetq
 * config file hornetq/config/stand-alone/non-clustered/hornetq-configuration.xml
 * to comment out the <security-settings>xxx</security-settings> part and 
 * also add <security-enabled>false</security-enabled>
 * 
 * TODO:
 *   Fix logging for hortnetq jars so hopefully using default. Don't show 
 *   DEBUG or FINE messages document
 *   
 * NOTE: truly useful documentation on using hornetQ is at
 * http://www.packtpub.com/article/basic-coding-hornetq-creating-consuming-messages
 * http://my.safaribooksonline.com/book/-/9781849518406/1dot-getting-started-with-hornetq/id286697617#X2ludGVybmFsX0h0bWxWaWV3P3htbGlkPTk3ODE4NDk1MTg0MDYlMkZpZDI4NjY5ODA3OCZxdWVyeT0=
 * Basically they are online versions of the HornetQ book.
 * 
 * @author SkiBu Smith
 *
 */
public class JMSWrapper {
	
	// Parameter that specifies URL of where to find the hornetq server
	private static StringConfigValue hornetqServerURL = 
			new StringConfigValue("transitime.ipc.hornetqServerURL", "jnp://localhost:1099");
	public static String getHornetqServerURL() {
		return hornetqServerURL.getValue();
	}
	
	// Regular member variables
	private InitialContext initialContext;
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	
	// The singleton since only need a single JMSWrapper object.
	private static JMSWrapper sharedWrapper = null;

	private static final Logger logger= 
			LoggerFactory.getLogger(JMSWrapper.class);
	
	//////////////////// Member methods /////////////////////////////
	
	/**
	 * Constructor. Establishes connection to JMS/HornetQ server 
	 * and initializes the members connection, session, etc.
	 * 
	 * @throws NamingException
	 * @throws JMSException
	 */
	private JMSWrapper() throws NamingException, JMSException {
		initiateConnection();
	}
	
	/**
	 * Does all the initialization. Called by constructor but
	 * sometimes needs to be called by another class if the session
	 * had closed down and needs to be restarted again.
	 * 
	 * @throws NamingException
	 * @throws JMSException
	 */
	public void initiateConnection() 
			throws NamingException, JMSException {
		// Specify how to access the hornetq server
		java.util.Properties p = new java.util.Properties();	
		p.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		p.put(javax.naming.Context.URL_PKG_PREFIXES,
				"org.jboss.naming:org.jnp.interfaces");			
		p.put(javax.naming.Context.PROVIDER_URL, getHornetqServerURL());
		
		// Initialize the member variables so that have a session that can reuse
		initialContext = new InitialContext(p);
		connectionFactory = 
				(ConnectionFactory) initialContext.lookup("/ConnectionFactory");
		connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);		
	}
	
	/**
	 * Creating a JMSWrapper is a heavyweight operation. Also, only 
	 * need a single JMSWrapper for the application. This method
	 * is a factory method that provides the singleton JMSWrapper.
	 * @return
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static JMSWrapper getJMSWrapper() throws JMSException, NamingException {
		if (sharedWrapper == null)
			sharedWrapper = new JMSWrapper();
		return sharedWrapper;
	}
	
	/**
	 * Needed for classes such as RestartableMessageProducer
	 * which needs Session to create message objects.
	 * @return
	 */
	public Session getSession() {
		return session;
	}
	
	/**
	 * Dynamically creates a topic. This goes against the normal idea
     * that JMS queues and topics should managed administratively, using
     * management tools. But for some applications this would be too 
     * burdensome. The user would have to additionally know about the
     * administration tools as well. Given that might be creating quite
     * a few AVL feeds, each one being a separate topic, this could be
     * a real nuisance.
     * 
	 * @param topicName
	 * @return true if topic created successfully
	 * @throws JMSException
	 */
	private boolean createTopic(String topicName) throws JMSException {
		QueueConnectionFactory queueConnectionFactory = 
				(QueueConnectionFactory) connectionFactory;
		QueueConnection connection = queueConnectionFactory.createQueueConnection();

		Queue managementQueue = HornetQJMSClient.createQueue("hornetq.management");
		QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		connection.start();
		Message message = session.createMessage();
		JMSManagementHelper.putOperationInvocation(message, 
				"jms.server", 
				"createTopic", // management command
				topicName,     // Name in hornetq
				topicName);    // JNDI name. This peculiar seemingly undocumented 
							   // parameter is needed so that can use JNDI to access 
		                       // the dynamically created topic. Found info on doing 
		                       // this at https://community.jboss.org/thread/165355 .
		QueueRequestor requestor = new QueueRequestor(session, managementQueue);

		// Determine if was successful
		Message reply = requestor.request(message);
		boolean topicCreated = JMSManagementHelper.hasOperationSucceeded(reply);
		
		if (topicCreated)
			logger.info("Dynamically created topic \"" + topicName + "\"");
		else
			logger.error("Failed to dynamically created topic \"" + topicName + "\"");
		
		// Return whether successful
		return topicCreated;
	}
	
	/**
	 * Returns named Queue. 
	 * Note: Unlike getTopic(), getQueue currently does not create the 
	 * queue if it doesn't already exist.
	 * 
	 * @param queueName
	 * @return
	 */
	public Queue getQueue(String queueName) {
		try {
			Queue queue = (Queue) initialContext.lookup(queueName);
			return queue;
		} catch (NamingException e) {
			logger.error("Could not get JMS queue \"" + queueName + 
					"\". That queue does not exist.", e);
			return null;
		} catch (ClassCastException e) {
			logger.error("Could not get JMS queue \"" + queueName + "\"", e);
			return null;
		}
	}
	
	/**
	 * Returns named Topic. If Topic not already configured as part of JMS server
	 * than that Topic is created dynamically. 
	 * 
	 * Usually don't need to call this method directly. Instead would use
	 * createTopicProducer(), createQueueProducer(), createTopicConsumer(),
	 * and createQueueConsumer().
	 * 
	 * @param topicName
	 * @return
	 */
	public Topic getTopic(String topicName) {
		try {
			Topic topic = (Topic) initialContext.lookup(topicName);
			return topic;
		} catch (NamingException e) {
			logger.info("Could not get JMS topic \"" + topicName + 
					"\". That topic does not exist. " +
					"Therefore will try to create it dynamically");
			try {
				// Try creating the topic dynamically
				boolean createdTopic = createTopic(topicName);
				if (createdTopic) {
					// Was able to create the topic so look it up
					Topic topic = (Topic) initialContext.lookup(topicName);
					return topic;
				}
			} catch (JMSException e1) {
				logger.error("Could not get JMS topic \"" + topicName + 
						"\" even after tried to dynamically create one.", e1);
			} catch (NamingException e1) {
				logger.error("Could not get JMS topic \"" + topicName + 
						"\" even after tried to dynamically create one.", e1);
			}
			// Was not able to dynamically create topic so return null 
			return null;
		} catch (ClassCastException e) {
			logger.error("Could not get JMS topic \"" + topicName + "\"", e);
			return null;
		}
	}
	
	// So can have single method for creating a consumer or producer
	private static enum Type {TOPIC, QUEUE};
	
	/**
	 * Creates a topic or a queue producer. Topics are dynamically 
	 * created if need be.
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	private RestartableMessageProducer createProducer(Type type, String name) {
		Destination destination;
		if (type == Type.TOPIC)
			destination = getTopic(name);
		else
			destination = getQueue(name);
		
		if (destination == null)
			return null;
		
		try {
			RestartableMessageProducer messageProducer = 
					new RestartableMessageProducer(this, destination);
			return messageProducer;
		} catch (JMSException e) {
			logger.error("Could not created JMS producer " + type + 
					" \"" + name + "\"", e);
			return null;
		}
	}
	
	/**
	 * Creates a topic or a queue consumer. Topics are dynamically 
	 * created if need bee.
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	private MessageConsumer createConsumer(Type type, String name) {
		Destination destination;
		if (type == Type.TOPIC)
			destination = getTopic(name);
		else
			destination = getQueue(name);
		
		if (destination == null)
			return null;
		
		try {
			MessageConsumer messageConsumer = 
					session.createConsumer(destination);
			return messageConsumer;
		} catch (JMSException e) {
			logger.error("Could not created JMS consumer " + type + 
					" \"" + name + "\"", e);
			return null;
		}

	}

	/**
	 * This is how a Topic producer is typically created. If topic
	 * doesn't already exist it will be dynamically created. 
	 * 
	 * @param topicName
	 * @return
	 */
	public RestartableMessageProducer createTopicProducer(String topicName) {
		return createProducer(Type.TOPIC, topicName);
	}
	
	/**
	 * This is how a Queue producer is typically created.
	 * 
	 * @param queueName
	 * @return
	 */
	public RestartableMessageProducer createQueueProducer(String queueName) {
		return createProducer(Type.QUEUE, queueName);
	}

	/**
	 * This is how a Topic consumer is typically created. If topic
	 * doesn't already exist it will be dynamically created.
	 * 
	 * @param topicName
	 * @return
	 */
	public MessageConsumer createTopicConsumer(String topicName) {
		return createConsumer(Type.TOPIC, topicName);
	}
	
	/**
	 * This is how a Queue consumer is typically created.
	 * 
	 * @param queueName
	 * @return
	 */
	public MessageConsumer createQueueConsumer(String queueName) {
		return createConsumer(Type.QUEUE, queueName);
	}
	
	/**
	 * For reading a message from a Topic or a Queue. The message can
	 * be any serializable object. Will block until a message can be read.
	 * 
	 * @param consumer
	 * @return the read serializable object
	 * @throws JMSException
	 */
	public static Serializable receiveObjectMessage(MessageConsumer consumer) 
			throws JMSException {
		ObjectMessage objectMessage = (ObjectMessage) consumer.receive();
		return objectMessage.getObject();
	}
	
	/**
	 * For reading a string message from a Topic or a Queue.
	 * 
	 * @param consumer
	 * @return the read string object
	 * @throws JMSException
	 */
	public static String receiveTextMessage(MessageConsumer consumer) 
			throws JMSException {
		TextMessage receivedMessage = (TextMessage) consumer.receive();
		return receivedMessage.getText();
	}
	
	/**
	 * For testing
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JMSWrapper tester = getJMSWrapper();
			
			RestartableMessageProducer msgProducer = tester.createTopicProducer("TopicTest");

			// Send out one message a second. Then JMSTest.java can be used
			// as a separate program to test receiving the messages.
			for (int i=0; i<100; ++i) {
				System.out.println(("Sending message #" + i));
				msgProducer.sendTextMessage("Message #" + i);
				Thread.sleep(1000);
			}
			
		} catch (NamingException e2) {
			e2.printStackTrace();
		} catch (JMSException e2) {
			e2.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}				
	}

}
