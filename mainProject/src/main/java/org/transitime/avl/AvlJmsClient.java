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
 
package org.transitime.avl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.jms.JMSWrapper;
import org.transitime.modules.Module;
import org.transitime.utils.Time;
import org.transitime.utils.threading.BoundedExecutor;
import org.transitime.utils.threading.NamedThreadFactory;

/**
 * Reads AVL data from JMS topic and processes it. Can use
 * multiple threads to do the processing.
 * 
 * @author SkiBu Smith
 */
public class AvlJmsClient extends Module {
	private final static int MAX_THREADS = 100;

	private MessageConsumer msgConsumer;

	private final BoundedExecutor avlClientExecutor;
	
	private static final Logger logger= 
			LoggerFactory.getLogger(AvlJmsClient.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor made private because it should only be called by start()
	 * since caller doesn't need access to object.
	 * 
	 * @param projectId
	 *            Specifies name of JMS topic to read AVL data from. Topic name
	 *            is clientName + "-AVLTopic".
	 * @param maxAVLQueueSize
	 *            How large the AVL queue can be before will block inserting
	 *            additional reports from the JMS feed.
	 * @param numberThreads
	 *            How many threads to be used to simultaneous process the AVL
	 *            data by AVLCients. For systems that receive a very high volume
	 *            of data using multiple threads allows a server to be more
	 *            fully utilized. This is especially true if the processing of
	 *            the AVL data takes a lot of IO time or is simply
	 *            computationally expensive. Must be between 1 and MAX_THREADS.
	 * 
	 * @throws NamingException
	 * @throws JMSException
	 */
	private AvlJmsClient(final String projectId, int maxAVLQueueSize, int numberThreads) 
			throws JMSException, NamingException {
		super(projectId);
		
		logger.info("Starting AvlClient for projectId={} with maxAVLQueueSize={} and numberThreads={}", 
				projectId, maxAVLQueueSize, numberThreads);
		
		// Make sure that numberThreads is reasonable
		if (numberThreads < 1) {
			logger.error("Number of threads must be at least 1 but " + numberThreads +
					" was specified. Therefore using 1 thread.");
			numberThreads = 1;
		}
		if (numberThreads > MAX_THREADS) {
			logger.error("Number of threads must be no greater than " + MAX_THREADS + 
					" but " + numberThreads + " was specified. Therefore using " +
					MAX_THREADS + " threads.");
			numberThreads = MAX_THREADS;
		}
		
		// Create the executor that actually processes the AVL data 
		NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory("avlClient");
		Executor executor = Executors.newFixedThreadPool(numberThreads, avlClientThreadFactory);
		avlClientExecutor = new BoundedExecutor(executor, maxAVLQueueSize);
	}
	
	/**
	 * Initiates the processing of the AVL data in separate threads.
	 * 
	 * @param clientName Specifies name of JMS topic to read AVL
	 * data from. Topic name is clientName + "-AVLTopic".
	 * @param maxAVLQueueSize How large the AVL queue can be before
	 * will block inserting additional reports from the JMS feed.
	 * @param numberThreads How many threads to be used to simultaneous process
	 * the AVL data by AVLCients. For systems that receive a very high volume of data 
	 * using multiple threads allows a server to be more fully utilized. This is 
	 * especially true if the processing of the AVL data takes a lot of IO time
	 * or is simply computationally expensive. Must be between 1 and MAX_THREADS.
	 *
	 * @throws NamingException 
	 * @throws JMSException 
	 */
	public static void start(final String clientName, final int maxAVLQueueSize, 
			final int numberThreads) 
			throws JMSException, NamingException{
		AvlJmsClient avlJmsClient = 
				new AvlJmsClient(clientName, maxAVLQueueSize, numberThreads);
		// Spawn the single thread that will read the AVL data from the JMS topic.
		// This is done here in start() instead of in the constructor because don't
		// want the constructor to call execute(this) since that would "leak"
		// the object before it was fully constructed.
		avlJmsClient.start();
	}
	
	/**
	 * Returns the name of the JMS topic to be used for the AVL feed.
	 * @param projectId Topic name is clientName + "-AVLTopic".
	 * @return the topic name for the AVL feed
	 */
	public static String getTopicName(String projectId) {
		return projectId + "-AVLTopic";
	}		
	
	/**
	 * So that Runnable interface used for each thread.
	 * Makes sure that runs continuously even if an 
	 * exception is thrown.
	 * Should not be called directly.
	 */
	@Override
	public void run() {
		// Establish the AVL message consumer. This is done here instead
		// of in the constructor since can't access session from multiple
		// threads. So apparently need to create it in thread that it is used.
		// Otherwise get a warning "HQ214021: Invalid concurrent session usage."
		String jmsTopicName = getTopicName(projectId);		
		JMSWrapper jmsWrapper = null;
		try {
			jmsWrapper = JMSWrapper.getJMSWrapper();
		} catch (JMSException e1) {
			logger.error("JMSException when getting JMS Wrapper. " + 
					"Make sure the HornetQ/JMS server is running!!! " + "" +
					"AVL feed terminated.", e1);
			return;
		} catch (NamingException e1) {
			logger.error("NamingException when getting JMS Wrapper. " + 
					"Make sure the HornetQ/JMS server is running!!! " + "" +
					"AVL feed terminated.", e1);
			return;
		}
		
		msgConsumer = jmsWrapper.createTopicConsumer(jmsTopicName);

		// Simply continue to do things
		while (true) {
			// Surround thread with a try/catch to catch 
			// all exceptions and loop forever. This way
			// don't have to worry about a thread dying.
			try {
				// Actually process the data
				processAVLDataFromJMSTopic();		
			} catch (Exception e) {
				logger.error("Unexpected exception occurred in AvlClient", e);
			}
		}
	}
	
	/**
	 * Infinite loop that actually processes the AVL data
	 * by reading it from the JMS topic. Intended to only be
	 * called from run().
	 */
	private void processAVLDataFromJMSTopic() {
		// Loop forever processing AVL data
		while (true) {
			try {
				// Read in AVL report from JMS. Block until an AVL report is available.
				logger.debug("Thread={} About to read AVL data from JMS topic",
						Thread.currentThread().getName());
				AvlReport avlReport = 
						(AvlReport) JMSWrapper.receiveObjectMessage(msgConsumer);
				
				// Log the AVL report				
				logger.debug("Thread={} Processing AVL report: {}",
						Thread.currentThread().getName(), avlReport);

				// Have another thread actually process the AVL data
				// using the AvlClient class. This way can use multiple
				// threads to simultaneously process the data.
				Runnable avlClient = new AvlClient(avlReport);
				avlClientExecutor.execute(avlClient);								
			} catch (JMSException e) {
				// This kind of exception can happen when there is a problem
				// with JMS such as "Consumer is closed". When this happens
				// need to make sure that don't just cycle through the while
				// loop and log a huge amount quickly. Therefore not 
				// displaying only the exception message instead of the full 
				// stack trace. Also, sleeping for a couple of seconds so 
				// that only get an error message every couple of seconds.
				logger.error("Error when waiting for AVL message. {}", 
						e.getMessage());
				Time.sleep(2000);
			} catch (ClassCastException e) {
				logger.error("AVL Client received an object that was not an AvlReport", e);
			} catch (InterruptedException e) {
				logger.error("Exception when processing AVL data", e);
			}
		}
	}
	 
	/**
	 * Just for testing.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new AvlJmsClient("testAVLFeed", 100, 3);
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
