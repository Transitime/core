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

import java.util.concurrent.ThreadFactory;

/**
 * A thread factory that names the threads. Eases debugging.
 * Based on code from the book "Java Concurrency in Practice" by Brian Goetz
 * 
 * @author SkiBu Smith
 *
 */
public class NamedThreadFactory implements ThreadFactory {
	private final String poolName;
	
	public NamedThreadFactory(String poolName) {
		this.poolName = poolName;
	}
	
	public Thread newThread(Runnable runnable) {
		return new NamedThread(runnable, poolName);
	}
}
