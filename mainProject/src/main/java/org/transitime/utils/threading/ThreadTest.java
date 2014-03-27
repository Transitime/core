/**
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * For debugging. Should be a unit test.
 *
 * @author SkiBu Smith
 *
 */
public class ThreadTest implements Runnable {

	private String name;
	
	public ThreadTest(int i) {
		name = Integer.toString(i);
	}

	@Override
	public void run() {
		// Simply continue to do things
		while (true) {
			// Surround thread with a try/catch to catch 
			// all exceptions and loop forever. This way
			// don't have to worry about a thread dying.
			try {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				
				System.err.println("Name=" + name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NamedThreadFactory namedFactory = new NamedThreadFactory("testName");
		int NTHREDS = 5;
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS, namedFactory);
		
		for (int i = 0; i < NTHREDS+1; i++) {
		      Runnable worker = new ThreadTest(i);
		      executor.execute(worker);
		    }
	}

}
