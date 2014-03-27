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
package org.transitime.ipc.rmi;

import java.rmi.RemoteException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.transitime.utils.Time;
import org.transitime.utils.threading.NamedThreadFactory;

/**
 * Sample test program to show how a client can do RMI calls.
 * 
 * @author SkiBu Smith
 *
 */
public class HelloClient {
	private static Hello hello;
	
	/**
	 * Does a test of the Hello.concat() RMI call. 
	 */
	private static void test() {
		try {
			System.out.println("running...");
			String result = hello.concat("s1", "s2");
			System.out.println("result=" + result);
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String projectId = args.length > 0 ? args[0] : "testProjectId";

		hello = ClientFactory.getInstance(projectId, Hello.class);
		
		// Access the class a bunch of times simultaneous using multiple
		// threads. This will cause some of the calls to fail
		NamedThreadFactory threadFactory = new NamedThreadFactory("testThreadName");
		ScheduledThreadPoolExecutor executor = 
				new ScheduledThreadPoolExecutor(
						RmiCallInvocationHandler.getMaxConcurrentCallsPerProject()+10, 
						threadFactory);
		for (int i=0; i<RmiCallInvocationHandler.getMaxConcurrentCallsPerProject()+10; ++i)
			executor.execute(new Runnable() {public void run() { test(); }	});
		
		// Now test a couple more times now that the other threads should have finished.
		// This test is to make sure that calls can go through again. 
		Time.sleep(3000);
		test();
		test();
	}

}
