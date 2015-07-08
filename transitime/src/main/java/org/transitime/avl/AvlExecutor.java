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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.Time;
import org.transitime.utils.threading.NamedThreadFactory;

/**
 * A singleton thread executor for executing AVL reports. For
 * when not using JMS to handle queue of AVL reports. One can dump AVL reports
 * into this executor and then have them be executed, possibly using multiple
 * threads. The number of threads is specified using the Java property
 * transitime.avl.numThreads .
 * 
 * @author SkiBu Smith
 *
 */
public class AvlExecutor {
	
	// The actual executor
	ThreadPoolExecutor avlClientExecutor = null;
	
	// Singleton class
	private static AvlExecutor singleton;
	
	/********************* Configurable parameters *************************/
	
	// For making sure that AvlConfig.getNumAvlThreads() config doesn't specify
	// an absurdly large number of threads.
	private final static int MAX_THREADS = 25;
	
	private static IntegerConfigValue avlQueueSize = 
			new IntegerConfigValue("transitime.avl.queueSize", 2000,
					"How many items to go into the blocking AVL queue "
					+ "before need to wait for queue to have space. Should "
					+ "be approximately 50% more than the number of reports "
					+ "that will be read during a single AVL polling cycle. "
					+ "If too big then wasteful. If too small then not all the "
					+ "data will be rejected by the ThreadPoolExecutor. ");

	private static IntegerConfigValue numAvlThreads = 
			new IntegerConfigValue("transitime.avl.numThreads", 1,
					"How many threads to be used for processing the AVL " +
					"data. For most applications just using a single thread " +
					"is probably sufficient and it makes the logging simpler " +
					"since the messages will not be interleaved. But for " +
					"large systems with lots of vehicles then should use " +
					"multiple threads, such as 3-15 so that more of the cores " +
					"are used.");
	
	private static final Logger logger= 
			LoggerFactory.getLogger(AvlExecutor.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor declared private because singleton class 
	 */
	private AvlExecutor() {
		int numberThreads = numAvlThreads.getValue();
		int maxAVLQueueSize = avlQueueSize.getValue();

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

		logger.info("Starting AvlExecutor for directly handling AVL reports " +
				"via a queue instead of JMS. maxAVLQueueSize={} and "
				+ "numberThreads={}", 
				maxAVLQueueSize, numberThreads);

		// Start up the ThreadPoolExecutor
		int corePoolSize = 1;
		int maximumPoolSize = numberThreads;
		long keepAliveTime = 1; /* 1 hour */
		BlockingQueue<Runnable> workQueue = new AvlQueue(maxAVLQueueSize);
		NamedThreadFactory avlClientThreadFactory =
				new NamedThreadFactory("avlClient");
		RejectedExecutionHandler re = new RejectedExecutionHandler() {
			@Override
			public void
					rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				logger.error("Rejected {}", ((AvlClient) arg0).getAvlReport());
			}};
		
		avlClientExecutor =
				new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
						keepAliveTime, TimeUnit.HOURS, workQueue,
						avlClientThreadFactory,
						re);
	}
	
	/**
	 * Returns singleton instance. Not synchronized since it is OK if an
	 * executor is replaced by a new one.
	 * 
	 * @return the singleton AvlExecutor
	 */
	public static AvlExecutor getInstance() {
		if (singleton == null) {
			singleton = new AvlExecutor();
		}
		
		return singleton;
	}
	
	/**
	 * Instead of writing AVL report to JMS topic this method directly processes
	 * it. By doing this one can bypass the need for a JMS server. Uses a thread
	 * executor so that can both use multiple threads and queue up requests.
	 * This is especially important if getting a dump of AVL data from either
	 * polling a feed or from an AVL feed hitting the Transitime web server and
	 * the AVL data getting then pushed to the core system in batches.
	 * <p>
	 * Uses a queue so that if system gets behind in processing AVL data then
	 * AVL data is written to a queue that keeps track of the latest AVL report
	 * per vehicle. If another AVL report is to be added to the queue then the
	 * previous one is removed since there is no point processing an old AVL
	 * report for a vehicle when new data is available.
	 * 
	 * @param newAvlReport
	 *            The AVL report to be processed
	 * @param useTestExecutor
	 *            So can optional specify that should use a different test
	 *            executor for testing out the queuing
	 */
	public void processAvlReport(AvlReport newAvlReport,
			boolean... useTestExecutor) {
		boolean testing = useTestExecutor.length > 0 && useTestExecutor[0];
		Runnable avlClient = !testing ? 
		new AvlClient(newAvlReport) : new AvlClientTester(newAvlReport); 

		avlClientExecutor.execute(avlClient);		
	}

	/**
	 * Separate executor, just for testing. The run method simply sleeps for a
	 * while so can verify that the queuing works when system getting behind in
	 * the processing of AVL reports
	 */
	private static class AvlClientTester extends AvlClient {
		private AvlClientTester(AvlReport avlReport) {
			super(avlReport);
		}
		
		/**
		 * Delays for a while
		 */
		@Override
		public void run() {
			// Let each call get backed up so can see what happens to queue
			logger.info("Starting processing of {}", getAvlReport());
			Time.sleep(6 * Time.SEC_IN_MSECS);
			logger.info("Finished processing of {}", getAvlReport());
		}
	}
	
	/**
	 * For testing.
	 */
	public static void main(String args[]) {
		AvlExecutor executor = AvlExecutor .getInstance();
		
		executor.processAvlReport(new AvlReport("v1", 0, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v2", 1000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v3", 2000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v1", 3000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v2", 4000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v3", 5000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v1", 6000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v1", 7000, 12.34, 43.21, null), true);
		executor.processAvlReport(new AvlReport("v1", 8000, 12.34, 43.21, null), true);
	}
}
