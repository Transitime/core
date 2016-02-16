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
import org.transitime.config.IntegerConfigValue;
import org.transitime.configData.AgencyConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.jms.JMSWrapper;
import org.transitime.logging.Markers;
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
public class AvlJmsClientModule extends Module {
	private MessageConsumer msgConsumer;

	private final BoundedExecutor avlClientExecutor;
	
	/*********************** Config Params ****************************/
	
	private final static int MAX_THREADS = 100;

	private static IntegerConfigValue avlQueueSize = 
			new IntegerConfigValue("transitime.avl.jmsQueueSize", 350,
					"How many items to go into the blocking AVL queue "
					+ "before need to wait for queue to have space. "
					+ "Only for when JMS is used.");

	private static IntegerConfigValue numAvlThreads = 
			new IntegerConfigValue("transitime.avl.jmsNumThreads", 1,
					"How many threads to be used for processing the AVL " +
					"data. For most applications just using a single thread " +
					"is probably sufficient and it makes the logging simpler " +
					"since the messages will not be interleaved. But for " +
					"large systems with lots of vehicles then should use " +
					"multiple threads, such as 3-5 so that more of the cores " +
					"are used. Only for when JMS is used.");

	private static final Logger logger = 
			LoggerFactory.getLogger(AvlJmsClientModule.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor. start() needs to be run to actually start the thing.
	 * 
	 * @param agencyId
	 *            Specifies name of JMS topic to read AVL data from. Topic name
	 *            is clientName + "-AVLTopic".
	 * 
	 * @throws NamingException
	 * @throws JMSException
	 */
	public AvlJmsClientModule(String agencyId) throws JMSException,
			NamingException {
		super(agencyId);
		
		int maxAVLQueueSize = avlQueueSize.getValue();
		int numberThreads = numAvlThreads.getValue();
		
		logger.info("Starting AvlClient for agencyId={} with "
				+ "maxAVLQueueSize={} and numberThreads={}", agencyId,
				maxAVLQueueSize, numberThreads);

		// Make sure that numberThreads is reasonable
		if (numberThreads < 1) {
			logger.error("Number of threads must be at least 1 but {} was "
					+ "specified. Therefore using 1 thread.", numberThreads);
			numberThreads = 1;
		}
		if (numberThreads > MAX_THREADS) {
			logger.error("Number of threads must be no greater than {} but "
					+ "{} was specified. Therefore using {} threads.",
					MAX_THREADS, numberThreads, MAX_THREADS);
			numberThreads = MAX_THREADS;
		}

		// Create the executor that actually processes the AVL data
		NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory(
				"avlClient");
		Executor executor = Executors.newFixedThreadPool(numberThreads,
				avlClientThreadFactory);
		avlClientExecutor = new BoundedExecutor(executor, maxAVLQueueSize);
	}
		
	/**
	 * Returns the name of the JMS topic to be used for the AVL feed.
	 * @param agencyId Topic name is clientName + "-AVLTopic".
	 * @return the topic name for the AVL feed
	 */
	public static String getTopicName(String agencyId) {
		return agencyId + "-AVLTopic";
	}		
	
	/**
	 * Creates the JMS message consumer. If there is a problem then
	 * msgConsumer will be null.
	 */
	private void createMessageConsumer() {
		// Establish the AVL message consumer. 
		String jmsTopicName = getTopicName(agencyId);		
		JMSWrapper jmsWrapper = null;
		try {
			jmsWrapper = JMSWrapper.getJMSWrapper();
		} catch (JMSException e1) {
			logger.error("JMSException when getting JMS Wrapper. " + 
					"Make sure the HornetQ/JMS server is running!!! " + "" +
					"AVL feed terminated.", e1);
			msgConsumer = null;
			return;
		} catch (NamingException e1) {
			logger.error("NamingException when getting JMS Wrapper. " + 
					"Make sure the HornetQ/JMS server is running!!! " + "" +
					"AVL feed terminated.", e1);
			msgConsumer = null;
			return;
		}
		
		msgConsumer = jmsWrapper.createTopicConsumer(jmsTopicName);
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
		createMessageConsumer();

		// Simply continue to do things
		while (true) {
			// Surround thread with a try/catch to catch 
			// all exceptions and loop forever. This way
			// don't have to worry about a thread dying.
			try {
				// Actually process the data
				processAVLDataFromJMSTopic();		
			} catch (Exception e) {
				logger.error(Markers.email(),
						"Unexpected exception occurred in AvlClient for "
						+ "agencyId={}", AgencyConfig.getAgencyId(), e);
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
				// loop and log a huge amount quickly. Therefore  
				// displaying only the exception message instead of the full 
				// stack trace. Also, sleeping for a couple of seconds so 
				// that only get an error message every couple of seconds.
				logger.error("Error when waiting for AVL message. {}", 
						e.getMessage());
				Time.sleep(2000);
				
				// Since there was a problem try creating the message consumer
				// again.
				createMessageConsumer();
			} catch (ClassCastException e) {
				logger.error("AVL Client received an object that was not an AvlReport", e);
			} catch (InterruptedException e) {
				logger.error("Exception when processing AVL data", e);
			}
		}
	}
	 
}
