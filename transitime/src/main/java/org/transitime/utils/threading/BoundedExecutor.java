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
package org.transitime.utils.threading;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.AgencyConfig;
import org.transitime.logging.Markers;

/**
 * An Executor but limits how many tasks can be queued. If queue is full and
 * attempt to execute an additional task then execute() will block.
 * <p>
 * Based on code from the book "Java Concurrency in Practice" by Brian Goetz
 * 
 * @author SkiBu Smith
 * 
 */
public class BoundedExecutor {
	private final Executor exec;
	private final Semaphore semaphore;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(BoundedExecutor.class);
	
	/**
	 * Constructs an Executor that allows Runnables to be executed using
	 * multiple threads. If try to execute more Runnables than there are
	 * available threads then those are queued up. If queue fills up then adding
	 * an additional Runnable will block.
	 * 
	 * @param exec
	 *            The executor. Recommend using Executors.newFixedThreadPool()
	 *            to get a fixed thread pool.
	 * @param allowableNumberBeforeBlocking
	 *            How many items that can be queued before execute() will block
	 *            if another Runnable is added. Includes both Runnables that are
	 *            currently running plus ones that have already been submitted
	 *            but are still queued up. The value needs to be at least as
	 *            large as the number of threads allowed by the Executor in
	 *            order for all threads to be usable simultaneously.
	 */
	public BoundedExecutor(Executor exec, int allowableNumberBeforeBlocking) {
		this.exec = exec;
		this.semaphore = new Semaphore(allowableNumberBeforeBlocking);
	}
	
	/**
	 * Returns how many items can still be added to the executor's queue.
	 * 
	 * @return How many items can add to queue
	 */
	public int spaceInQueue() {
		return semaphore.availablePermits();
	}
	
	/**
	 * Executes the task by running the Runnable.run() method. Blocks if queue
	 * is already full. The queue includes tasks already being executed so if
	 * Executor can use 5 threads and all 5 threads are executing a task and
	 * allowableNumberBeforeBlocking is 8 then can only have 3 items queued up
	 * and not yet started execution before this method will block.
	 * 
	 * @param command
	 *            For which run() is to be called
	 * @throws InterruptedException
	 *             if this task cannot be accepted for execution.
	 */
	public void execute(final Runnable command) 
			throws InterruptedException {
		// Only allow bound number of threads to run.
		semaphore.acquire();
		
		try {
			// Call the run() method for the command
			exec.execute(new Runnable() {
				public void run() {
					try {
						// Actually call the run() method for the command
						command.run();
					} catch (Throwable t) {
						// Need to catch (and log) exception. Otherwise 
						// exception would bubble upwards and get infinite 
						// number of threads, at least if have a breakpoint
						// in Eclipse.
						logger.error("Exception occurred in thread. ", t);
						// Log the problem but do so within a try/catch in case it is
						// an OutOfMemoryError and need to exit even if get another
						// OutOfMemoryError when logging.
						try {
							t.printStackTrace();
							if (t instanceof OutOfMemoryError) {
								logger.error(Markers.email(),
										"For {} OutOfMemoryError occurred in "
										+ "BoundedExecutor so "
										+ "terminating application",
										AgencyConfig.getAgencyId(), t);
							} else {
								logger.error(Markers.email(),
										"For {} unexpected Throwable occurred "
										+ "in BoundedExecutor", 
										AgencyConfig.getAgencyId(), t);
							}
						} catch (Throwable t2) {
						}
						
						// OutOfMemoryErrors are really serious. Don't want application to
						// continue in some kind of crippled mode that monitoring has a 
						// difficult time detecting. Therefore exit the application so that
						// can be automatically restarted.
						if (t instanceof OutOfMemoryError) {
							System.exit(-1);
						}
					} finally {
						semaphore.release();
					}
				}
			});
		} catch (RejectedExecutionException e) {
			logger.error("Exception occurred when running new thread. ", e);
			semaphore.release();
		}
	}
}
