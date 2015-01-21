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

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.AvlConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.jms.JMSWrapper;
import org.transitime.ipc.jms.RestartableMessageProducer;
import org.transitime.modules.Module;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;
import org.transitime.utils.threading.BoundedExecutor;
import org.transitime.utils.threading.NamedThreadFactory;


/**
 * Subclass of Module to be used when reading AVL data from a feed. Calls the
 * abstract method getAndProcessData() for the subclass to actually get data
 * from the feed. If in JMS mode then it outputs the data to the appropriate JMS
 * topic so that it can be read from an AvlClient. If not in JMS mode then uses
 * a BoundedExecutor with multiple threads to directly call AvlClient.run().
 * 
 * @author SkiBu Smith
 * 
 */
public abstract class AvlModule extends Module {
	// For writing the AVL data to the JMS topic
	protected RestartableMessageProducer jmsMsgProducer = null; 

	// For when using a direct queue instead of JMS for processing the AVL 
	// reports
	private final static int MAX_THREADS = 100;
	private BoundedExecutor avlClientExecutor = null;

	private static final Logger logger = 
			LoggerFactory.getLogger(AvlModule.class);	

	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 */
	protected AvlModule(String agencyId) {
		super(agencyId);		
		
		if (!AvlConfig.shouldUseJms()) 
			initForUsingQueueInsteadOfJms();
	}
	
	/**
	 * Feed specific URL to use when accessing data.
	 * @return
	 */
	protected abstract String getUrl();
	
	/**
	 * Actually processes the data from the InputStream
	 * 
	 * @param in
	 *            The input stream containing the AVL data
	 * @throws Exception
	 *             Throws a generic exception since the processing is done in
	 *             the abstract method processData() and it could throw any type
	 *             of exception since we don't really know how the AVL feed will
	 *             be processed.
	 */
	protected abstract void processData(InputStream in) throws Exception;
	
	/**
	 * Actually reads data from feed and processes it.
	 * 
	 * @throws Exception
	 *             Throws a generic exception since the processing is done in
	 *             the abstract method processData() and it could throw any type
	 *             of exception since we don't really know how the AVL feed will
	 *             be processed.
	 */
	protected void getAndProcessData() 
		throws Exception {
		// For logging
		IntervalTimer timer = new IntervalTimer(); 

		// Get from the AVL feed subclass the URL to use for this feed
		String fullUrl = getUrl();
		
		// Log what is happening
		logger.info("Getting data from feed using url=" + fullUrl);
	
		// Create the connection
		URL url = new URL(fullUrl);
		URLConnection con = url.openConnection();
		
		// Set the timeout so don't wait forever
		int timeoutMsec = AvlConfig.getAvlFeedTimeoutInMSecs();
		con.setConnectTimeout(timeoutMsec);
		con.setReadTimeout(timeoutMsec);
		
		// Request compressed data to reduce bandwidth used
		con.setRequestProperty("Accept-Encoding", "gzip,deflate");
		
		// Create appropriate input stream depending on whether content is 
		// compressed or not
		InputStream in = con.getInputStream();
		if ("gzip".equals(con.getContentEncoding())) {
		    in = new GZIPInputStream(in);
		    logger.debug("Returned XML data is compressed");
		} else {
		    logger.debug("Returned XML data is NOT compressed");			
		}

		// For debugging
		logger.debug("Time to access inputstream {} msec", 
				timer.elapsedMsec());
				
		// Call the abstract method to actually process the data
		timer.resetTimer();
		processData(in);		
		in.close();
		logger.debug("Time to parse XML document {} msec", timer.elapsedMsec());
	}
	
	/**
	 * For when using a direct queue instead of JMS for handling
	 * the AVL reports.
	 */
	private void initForUsingQueueInsteadOfJms() {
		int maxAVLQueueSize = AvlConfig.getAvlQueueSize();
		int numberThreads = AvlConfig.getNumAvlThreads();
		
		logger.info("Starting AvlModule for directly handling AVL reports " +
				"via a queue instead of JMS. For agencyId={} with "
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

		// Create the executor that actually processes the AVL data. The executor
		// will be passed an AvlClient and then AvlClient.run() is called.
		NamedThreadFactory avlClientThreadFactory = 
				new NamedThreadFactory("avlClient"); 
		Executor executor = Executors.newFixedThreadPool(numberThreads,
				avlClientThreadFactory);
		avlClientExecutor = new BoundedExecutor(executor, maxAVLQueueSize);
	}
	
	/**
	 * Initializes JMS if need be. Needs to be done from same thread that
	 * JMS is written to. Otherwise get concurrency error. 
	 */
	private void initializeJmsIfNeedTo() {
		// If JMS already initialized then can return
		if (jmsMsgProducer != null)
			return;
		
		// JMS not already initialized so create the MessageProducer 
		// that the AVL data can be written to
		try {
			String jmsTopicName = AvlJmsClientModule.getTopicName(agencyId);
			JMSWrapper jmsWrapper = JMSWrapper.getJMSWrapper();
			jmsMsgProducer = jmsWrapper.createTopicProducer(jmsTopicName);
		} catch (Exception e) {
			logger.error("Problem when setting up JMSWrapper for the AVL feed", e);			
		}

	}
	
	/** 
	 * Does all of the work for the class. Runs forever and reads in 
	 * AVL data from feed and writes it to the appropriate JMS topic
	 * so that AVL clients can access it.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Started module {} for agencyId={}", 
				getClass().getName(), getAgencyId());
		
		// Run forever
		while (true) {
			IntervalTimer timer = new IntervalTimer();
			
			try {
				// Process data
				getAndProcessData();
			} catch (SocketTimeoutException e) {
				logger.error("Error accessing AVL feed using URL={} with a " +
						"timeout of {} msec.", 
						getUrl(), AvlConfig.getAvlFeedTimeoutInMSecs(), e);
			} catch (Exception e) {
				logger.error("Error accessing AVL feed using URL={}.", 
						getUrl(), e);
			}
			
			// Wait appropriate amount of time till poll again
			long elapsedMsec = timer.elapsedMsec();
			long sleepTime = 
					AvlConfig.getSecondsBetweenAvlFeedPolling()*Time.MS_PER_SEC - 
					elapsedMsec;
			if (sleepTime < 0) {
				logger.warn("Supposed to have a polling rate of " + 
						AvlConfig.getSecondsBetweenAvlFeedPolling()*Time.MS_PER_SEC +
						" msec but processing previous data took " +
						elapsedMsec + " msec so polling again immediately.");
			} else {
				Time.sleep(sleepTime);
			}
		}
	}
	
	/**
	 * Processes AVL report read from feed. To be called from the subclass for
	 * each AVL report. Can use JMS or bypass it, depending on how configured.
	 */
	protected void processAvlReport(AvlReport avlReport) {
		if (AvlConfig.shouldUseJms()) {
			processAvlReportUsingJms(avlReport);
		} else {
			processAvlReportWithoutJms(avlReport);
		}
	}
	
	/**
	 * Sends the AvlReport object to the JMS topic so that AVL clients can read it.
	 * @param avlReport
	 */
	private void processAvlReportUsingJms(AvlReport avlReport) {
		// Make sure the JMS stuff setup successfully
		initializeJmsIfNeedTo();
		if (jmsMsgProducer == null) {
			logger.error("Cannot write AvlReport to JMS because JMS tools " + 
					"were not initialized successfully.");
			return;
		}
			
		// Send the AVL report to the JMS topic
		try {
			jmsMsgProducer.sendObjectMessage(avlReport);
		} catch (JMSException e) {
			logger.error("Problem sending AvlReport to the JMS topic", e);
		}		
	}

	/**
	 * Instead of writing AVL report to JMS topic this method directly
	 * processes it. By doing this one can bypass the need for a JMS server.
	 * 
	 * @param avlReport
	 */
	private void processAvlReportWithoutJms(AvlReport avlReport) {
		// Have another thread actually process the AVL data
		// using the AvlClient class. Actually uses AvlClient.run() to
		// do the processing. This way can use multiple
		// threads to simultaneously process the data.
		Runnable avlClient = new AvlClient(avlReport);
		try {
			avlClientExecutor.execute(avlClient);
		} catch (InterruptedException e) {
			logger.error("Exception when processing AVL data", e);
		}								
	}
}
