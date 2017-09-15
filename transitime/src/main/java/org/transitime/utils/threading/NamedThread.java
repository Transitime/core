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

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.AgencyConfig;
import org.transitime.logging.Markers;

/**
 * Creates a Thread but sets the name of it and sets the 
 * UncaughtExceptionHandler so that uncaught exceptions are logged.
 * These features can make debugging of multiple threaded system
 * much easier.
 * <p>
 * Based on code from the book "Java Concurrency in Practice" by Brian Goetz
 * 
 * @author SkiBu Smith
 *
 */
public class NamedThread extends Thread {
	public static final String DEFALT_NAME = "UnnamedThread";
	private static final HashMap<String, Integer> threadNameCountMap = 
			new HashMap<String, Integer>();
	// Number of live threads isn't currently used but was in Brian Goetz book
	private static final AtomicInteger numAlive = new AtomicInteger();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(NamedThread.class);
	
	/**
	 * Creates the named thread using a default name.
	 * @param r
	 */
	public NamedThread(Runnable r) {
		this(r, DEFALT_NAME);
	}
	
	/**
	 * Creates the named thread. The actual name of the thread will include
	 * the name and a counter, such as "name-6".
	 * @param r
	 * @param name
	 */
	public NamedThread(Runnable r, String name) {
		super(r, threadNameWithCounter(name));
		setUncaughtExceptionHandler(
				new Thread.UncaughtExceptionHandler() {					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						logger.error("Uncaught exception in thread {}", 
								t.getName(), e);						
					}
				});
	}
	
	/**
	 * Returns the name of the thread. Each thread name has its own counter
	 * and the name returned will be "name-3".
	 * @param name
	 * @return
	 */
	private static String threadNameWithCounter(String name) {
		synchronized (threadNameCountMap) {
			Integer count = threadNameCountMap.get(name);
			if (count == null) {
				count = 1;				
			} else {
				// Increment the count;
				count++;			
			}		
			threadNameCountMap.put(name, count);
			return name + "-" + count;
		}
	}
	
	@Override
	public void run() {
		logger.debug("Created NamedThread {}", getName());
		try {
			numAlive.incrementAndGet();
			super.run();
		} catch (Throwable t) {
			// Log the problem but do so within a try/catch in case it is
			// an OutOfMemoryError and need to exit even if get another
			// OutOfMemoryError when logging.
			try {
				// Output info to stderr since this is an exception situation.
				// This will log it to the nohup file used for running core app.
				System.err.println("Throwable \"" + t.getMessage() 
						+ "\" occurred at " + new Date());
				t.printStackTrace();
				
				// Log since this is a serious problem
				if (t instanceof OutOfMemoryError) {
					// Would like to send out an e-mail as part of logging but
					// found that when running out of memory that sending out an
					// e-mail can hang the system for a while. This is a bad 
					// thing since OutOfMemoryError is really serious and want
					// to terminate the program right away so that it can be
					// automatically restarted. This is unfortunate since
					// really want to notify folks that there is an out of
					// memory problem but notifying via email is not as 
					// important as quickly getting the system restarted.
					logger.error("OutOfMemoryError occurred in thread {} so "
							+ "terminating application for agencyId={}", 
							getName(), AgencyConfig.getAgencyId(), t);
				} else {
					// Log and send out e-mail since this is an unexpected problem
					logger.error(Markers.email(),
							"Unexpected Throwable occurred which will cause "
							+ "thread {} to terminate for agencyId={}", 
							getName(), AgencyConfig.getAgencyId(), t);
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
			numAlive.decrementAndGet();
			logger.debug("Exiting NamedThread {}", getName());
		}
	}
}
