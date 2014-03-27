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
package org.transitime.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.transitime.utils.threading.NamedThreadFactory;

/**
 * The standard java.util.Timer and java.util.TimerTask classes should be 
 * considered deprecated because as described in "Java Concurrency in 
 * Practice" by Brian Goetz on page 123, there are a few problems with
 * them. If an exception is thrown in the task then the entire Timer
 * stops running. And if a task takes a while then subsequent tasks
 * are not run at their appropriate time due to only a single thread
 * being used. Therefore should use ScheduledThreadPoolExecutor class
 * instead. 
 * 
 * This is simply a convenient way of using a ScheduledThreadPoolExecutor.
 * Once gotten then can use the usual methods such as scheduleAtFixedRate()
 * 
 * @author SkiBu Smith
 */
public class Timer {
	public static ScheduledThreadPoolExecutor get() {
		// A timer is created to be used. Therefore pretty much
		// always want to have a single thread available so that
		// don't have the overhead of creating new thread when
		// needed.
		int coreNumberThreads = 1;
		
		// Use the named factory so that it is easier to see what
		// thread is used for what purpose.
		NamedThreadFactory threadFactory = new NamedThreadFactory(Timer.class.getName());
				
		ScheduledThreadPoolExecutor executor = 
				new ScheduledThreadPoolExecutor(coreNumberThreads, threadFactory);
				
		return executor;
	}

}
