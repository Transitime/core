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

import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.ipc.rmi.Hello;

/**
 * For clients to access RMI based method calls for a remote
 * object simply need to call something like:
 *   Hello hello = ClientFactory.getInstance(projectId, Hello.class);
 *   
 * @author SkiBu Smith
 *
 */
public class ClientFactory<T extends Remote> {

	private static final Logger logger = 
			LoggerFactory.getLogger(ClientFactory.class);

	/********************** Member Functions **************************/

	/**
	 * This serves as a factory class For a Remote object. It is 
	 * expected that a new instance is created every time getInstance()
	 * is called. This is reasonable because usually will only be
	 * creating a single such object and if there are a few
	 * that is fine as well since they are not that expensive.
	 * 
	 * @param host Where remote object lives
	 * @param projectId For creating bind name for remote object
	 * @param clazz Because this is a static method the only way to get the class name
	 * is to pass in the class.
	 * @return Proxy to the RMI object on the server. If there is a problem
	 * accessing the object then null is returned.
	 */
	public static <T extends Remote> T getInstance(String projectId, Class<T> clazz) {
		try {
			// Create the info object that contains what is needed to
			// create the RMI stub. This info will also be used if the
			// stub needs to be recreated by the Invoker if there is
			// an error.
			RmiStubInfo info = new RmiStubInfo(projectId, clazz.getSimpleName());
			
			// Get the RMI stub
			T rmiStub = getRmiStub(info);
			
			// Create proxy for the RMI stub so that the special 
			// RmiCallInvocationHandler can be used to instrument
			// remote method calls.
			@SuppressWarnings("unchecked")
			T proxiedStub = (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                    new Class<?>[] {clazz},
                    new RmiCallInvocationHandler(rmiStub, info));

			// Return the object that the client can use
			return (T) proxiedStub;
		} catch (Exception e) {
			logger.error("Exception occurred when creating the RMI client " + 
					"object for class=" + clazz.getName() + 
					" and projectId=" + projectId,
					e);			
			return null;
		}		
	}
	
	/**
	 * Creates an RMI stub based on the project name, host name,
	 * and class name.
	 * @param info
	 * @return
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static <T extends Remote> T getRmiStub(RmiStubInfo info) 
			throws RemoteException, NotBoundException {
		// Get the registry
		Registry registry = 
				LocateRegistry.getRegistry(info.getHostName(), RmiParams.getRmiPort());
		
		// Get the remote object's bind name
		String bindName = 
				AbstractServer.getBindName(info.getProjectId(), info.getClassName());
				
		// Get the RMI stub object from the registry 
		@SuppressWarnings("unchecked")
		T rmiStub = (T) registry.lookup(bindName);
		
		return rmiStub;
	}
	
	/**
	 * Just for debugging.
	 * @param args
	 */
	public static void main(String args[]) {
		String projectId = "testProjectId";
		
		Hello hello = ClientFactory.getInstance(projectId, Hello.class);
		try {
			String result = hello.concat("s1", "s2");
			System.err.println("result=" + result);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
