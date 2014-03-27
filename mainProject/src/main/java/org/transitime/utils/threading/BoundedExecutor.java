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

/**
 * An Executor but limits how many tasks can be queued. If queue is full
 * and attempt to execute an additional task then execute() will block.
 * Based on code from the book "Java Concurrency in Practice" by Brian Goetz
 * @author SkiBu Smith
 *
 */
//@ThreadSafe
public class BoundedExecutor {
	private final Executor exec;
	private final Semaphore semaphore;
	
	private static final Logger logger = LoggerFactory
			.getLogger(BoundedExecutor.class);
	
	public BoundedExecutor(Executor exec, int bound) {
		this.exec = exec;
		this.semaphore = new Semaphore(bound);
	}
	
	public void execute(final Runnable command) 
			throws InterruptedException {
		semaphore.acquire();
		try {
			exec.execute(new Runnable() {
				public void run() {
					try {
						command.run();
					} catch(Exception e) {
						// Need to catch (and log) exception. Otherwise exception
						// would bubble upwards and get infinite number of threads,
						// at least if have a breakpoint in Eclipse.
						logger.error("Exception occurred in thread. ", e);
					} finally {
						semaphore.release();
					}
				}
			});
		} catch (RejectedExecutionException e) {
			semaphore.release();
		}
	}
}
