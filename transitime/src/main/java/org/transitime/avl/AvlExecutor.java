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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.AvlConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.threading.BoundedExecutor;
import org.transitime.utils.threading.NamedThreadFactory;

/**
 * Provides a queue and a thread executor for AVL reports to be executed. For
 * when not using JMS to handle queue of AVL reports. Once can dump AVL reports
 * into the queue and then have them be executed, possibly using multiple
 * threads.
 * <p>
 * A singleton class
 * 
 * @author Michael
 *
 */
public class AvlExecutor {

	private BoundedExecutor avlClientExecutor = null;

	private static AvlExecutor singleton;
	
	private final static int MAX_THREADS = 100;
	
	private static final Logger logger= 
			LoggerFactory.getLogger(AvlExecutor.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor declared private because singleton class 
	 */
	private AvlExecutor() {
		int numberThreads = AvlConfig.getNumAvlThreads();
		int maxAVLQueueSize = AvlConfig.getAvlQueueSize();

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
	 * Instead of writing AVL report to JMS topic this method directly processes
	 * it. By doing this one can bypass the need for a JMS server. Uses a thread
	 * executor so that can both use multiple threads and queue up requests.
	 * This is especially important if getting a dump of AVL data from an AVL
	 * feed hitting the Transitime web server and the AVL data getting then
	 * pushed to the core system in batches.
	 * 
	 * @param avlReport
	 *            The AVL report to be processed
	 */
	public void processAvlReport(AvlReport avlReport) {
		// Have another thread actually process the AVL data
		// using the AvlClient class. Actually uses AvlClient.run() to
		// do the processing. This way can use multiple
		// threads to simultaneously process the data.
		Runnable avlClient = new AvlClient(avlReport);
		
		// Want to both be able to use multiple threads and to be able to 
		// queue up requests so use a thread executor
		try {
			avlClientExecutor.execute(avlClient);
		} catch (InterruptedException e) {
			logger.error("Exception when processing AVL data", e);
		}		
	}

}
