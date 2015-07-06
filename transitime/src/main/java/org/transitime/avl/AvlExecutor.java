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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.Time;
import org.transitime.utils.threading.BoundedExecutor;
import org.transitime.utils.threading.NamedThreadFactory;

/**
 * Provides a queue and a thread executor for AVL reports to be executed. For
 * when not using JMS to handle queue of AVL reports. One can dump AVL reports
 * into the queue and then have them be executed, possibly using multiple
 * threads. The number of threads is specified using the Java property
 * transitime.avl.numThreads .
 * <p>
 * A singleton class.
 * <p>
 * Actually uses two queues. There is a queue associated with the
 * BoundedExecutor that manages the threads that process the data. This is the
 * main queue. It should be large enough to handle an entire batch of AVL data
 * for an AVL polling cycle. For example, if you have 1,000 vehicles reporting
 * once per minute and the data is polled 4 times a second then need a
 * comfortable margin over 250 (should use perhaps 350) for the queue size. The
 * size of the executor queue is set using the Java property
 * transitime.avl.queueSize .
 * <p>
 * There is also a second queue, this one per vehicle. It is so that if
 * processing gets behind and the main executor queue fills up then previous
 * unprocessed AVL reports for a vehicle are discarded. This way if run out of
 * processing power the system won't get further and further behind. This is
 * important for when have high reporting rate and possibly variable processing
 * power, such as when using a cloud system such as AWS.
 * 
 * @author SkiBu Smith
 *
 */
public class AvlExecutor {

	// This secondary queue used if can't write data to the executor since 
	// it is full. LinkedHashMap is not thread safe, but that is OK since 
	// synchronizing access.
	Map<String, AvlReport> avlDataPerVehicleQueue =
			new LinkedHashMap<String, AvlReport>();
	
	// The actual executor
	private BoundedExecutor avlClientExecutor = null;

	// Singleton class
	private static AvlExecutor singleton;
	
	// For making sure that AvlConfig.getNumAvlThreads() config doesn't specify
	// an absurdly large number of threads.
	private final static int MAX_THREADS = 25;
	
	/********************* Configurable parameters *************************/
	
	private static IntegerConfigValue avlQueueSize = 
			new IntegerConfigValue("transitime.avl.queueSize", 350,
					"How many items to go into the blocking AVL queue "
					+ "before need to wait for queue to have space. Should "
					+ "be approximately 50% more than the number of reports "
					+ "that will be read during a single AVL polling cycle. "
					+ "If too big then too much data will be queued when "
					+ "system gets backed up. If too small then not all the "
					+ "data will be processed during an AVL polling cycle. "
					+ "Instead, some will end up in the secondary per vehicle "
					+ "queue and won't be processed until next polling cycle.");

	private static IntegerConfigValue numAvlThreads = 
			new IntegerConfigValue("transitime.avl.numThreads", 1,
					"How many threads to be used for processing the AVL " +
					"data. For most applications just using a single thread " +
					"is probably sufficient and it makes the logging simpler " +
					"since the messages will not be interleaved. But for " +
					"large systems with lots of vehicles then should use " +
					"multiple threads, such as 3-5 so that more of the cores " +
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

		// Create the executor that actually processes the AVL data. The executor
		// will be passed an AvlClient and then AvlClient.run() is called.
		NamedThreadFactory avlClientThreadFactory = 
				new NamedThreadFactory("avlClient"); 
		Executor executor = Executors.newFixedThreadPool(numberThreads,
				avlClientThreadFactory);
		avlClientExecutor = new BoundedExecutor(executor, maxAVLQueueSize);
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
	 * Actually calls AvlClient.run() on the AVL report.
	 * 
	 * @param avlReport
	 * @param useTestExecutor
	 *            If set to true then will use AvlClientTester class instead of
	 *            AvlClient
	 */
	private void execute(AvlReport avlReport, boolean useTestExecutor) {
		try {
			// Have another thread actually process the AVL data
			// using the AvlClient class. Actually uses AvlClient.run() to
			// do the processing. This way can use multiple
			// threads to simultaneously process the data.
			Runnable avlClient = !useTestExecutor ? 
					new AvlClient(avlReport) : new AvlClientTester(avlReport); 
			avlClientExecutor.execute(avlClient);
		} catch (InterruptedException e) {
			logger.error("Exception when processing AVL data", e);
		}		
	}
	
	/**
	 * Instead of writing AVL report to JMS topic this method directly processes
	 * it. By doing this one can bypass the need for a JMS server. Uses a thread
	 * executor so that can both use multiple threads and queue up requests.
	 * This is especially important if getting a dump of AVL data from an AVL
	 * feed hitting the Transitime web server and the AVL data getting then
	 * pushed to the core system in batches.
	 * <p>
	 * Uses a queue so that if system gets behind in processing AVL data then
	 * AVL data is written to a queue that keeps track of the latest AVL report
	 * per vehicle. If another AVL report is to be added to the queue then the
	 * previous one is removed since there is no point processing an old AVL
	 * report for a vehicle when new data is available.
	 * <p>
	 * Synchronized because could be called from multiple threads and need to
	 * make sure that the queue is used coherently. This means that can use a
	 * plain LinkedHashMap for the queue even though it itself is not
	 * threadsafe. Since the time consuming processing of the AVL report is done
	 * through an executor in a separate thread this method finishes quickly
	 * such that synchronizing this method won't bog things down.
	 * 
	 * @param newAvlReport
	 *            The AVL report to be processed
	 * @param useTestExecutor
	 *            So can optional specify that should use a different test
	 *            executor for testing out the queuing
	 */
	public synchronized void processAvlReport(AvlReport newAvlReport,
			boolean... useTestExecutor) {
		boolean testing = useTestExecutor.length > 0 && useTestExecutor[0];
		
		// If there are previous AVL reports in avlDataPerVehicleQueue then 
		// add as many as possible to the executor. There will only be items
		// in avlDataPerVehicleQueue if system is getting behind processing
		// them since the executor also has a separate queue.
		if (!avlDataPerVehicleQueue.isEmpty()) {
			// Get oldest item in queue
			Iterator<AvlReport> queueIterator =
					avlDataPerVehicleQueue.values().iterator();
			while (queueIterator.hasNext()
					&& avlClientExecutor.spaceInQueue() > 0) {
				AvlReport oldestAvlReport = queueIterator.next();
				logger.info("The avlDataPerVehicleQueue has {} elements in it. "
						+ "Using oldest AVL report from queue {}",
						avlDataPerVehicleQueue.size(), oldestAvlReport);
				execute(oldestAvlReport, testing);
				queueIterator.remove();
			}
		}

		// If executor queue is full then add new AVL report to 
		// avlDataPerVehicleQueue instead of executing it now.
		if (avlClientExecutor.spaceInQueue() == 0) {
			// Add new AVL report to local queue.
			// First, remove any old AVL report for the vehicle form the queue
			// so that when the new one is inserted it will be at the end of the
			// queue.
			logger.info("Executor full so putting AVL report in queue {}",
					newAvlReport);
			String vehicleId = newAvlReport.getVehicleId();
			if (avlDataPerVehicleQueue.containsKey(vehicleId))
				avlDataPerVehicleQueue.remove(vehicleId);
			avlDataPerVehicleQueue.put(vehicleId, newAvlReport);
		} else {
			// Executor is not full so write AVL report directly to executor
			logger.debug("Space in executor so handling AVL report {}",
					newAvlReport);
			execute(newAvlReport, testing);
		}
	}

	/**
	 * Separate executor, just for testing. The run method simply sleeps for a
	 * while so can verify that the queuing works when system getting behind in
	 * the processing of AVL reports
	 */
	private static class AvlClientTester implements Runnable {
		private final AvlReport avlReport;

		private AvlClientTester(AvlReport avlReport) {
			this.avlReport = avlReport;
		}
		
		/**
		 * Delays for a while
		 */
		@Override
		public void run() {
			// Let each call get backed up so can see what happens to queue
			logger.info("Starting processing of {}", avlReport);
			Time.sleep(20 * Time.SEC_IN_MSECS);
			logger.info("Finished processing of {}", avlReport);
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
