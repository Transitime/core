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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ThreadPoolExecutor that also logs when a thread is created.
 * Very useful for when need to understand thread usage.
 *
 * @author SkiBu Smith
 *
 */
public class LoggingThreadPoolExecutor extends ThreadPoolExecutor {

	private static final Logger logger= 
			LoggerFactory.getLogger(LoggingThreadPoolExecutor.class);
	
	public LoggingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, 
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) { 
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue); 
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		logger.info("About to exeute thread %s: start %s", t, r);
	}
}
