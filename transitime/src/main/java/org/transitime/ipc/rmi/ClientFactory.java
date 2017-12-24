/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.ipc.rmi;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.ipc.rmi.Hello;
import org.transitime.utils.Time;

/**
 * For clients to access RMI based method calls for a remote object simply need
 * to call something like: Hello hello = ClientFactory.getInstance(agencyId,
 * Hello.class);
 * 
 * @author SkiBu Smith
 *
 */
public class ClientFactory<T extends Remote> {

	// So that only need to setup RMI timeout once
	private static Boolean rmiTimeoutEnabled = false;

	private static IntegerConfigValue timeoutSec = new IntegerConfigValue(
			"transitime.rmi.timeoutSec", 4,
			"Specifies the timeout time in seconds for RMI calls. Note "
					+ "that when an RMI failure occurs a second try is done "
					+ "so total timeout time is twice what is specified here.");

	private static StringConfigValue debugRmiServerHost = new StringConfigValue(
	    "transtime.rmi.debug.rmi.server", 
	    null, 
	    "The RMI server to connect to when in debug mode");
	
	private static final Logger logger = LoggerFactory
			.getLogger(ClientFactory.class);

	/********************** Member Functions **************************/

	/**
	 * This serves as a factory class For a Remote object. It is expected that a
	 * new instance is created every time getInstance() is called. This is
	 * reasonable because usually will only be creating a single such object and
	 * if there are a few that is fine as well since they are not that
	 * expensive.
	 * 
	 * @param host
	 *            Where remote object lives
	 * @param agencyId
	 *            For creating bind name for remote object
	 * @param clazz
	 *            Because this is a static method the only way to get the class
	 *            name is to pass in the class.
	 * @return Proxy to the RMI object on the server. If there is a problem
	 *         accessing the object then null is returned.
	 */
	public static <T extends Remote> T getInstance(String agencyId,
			Class<T> clazz) {
		// Set RMI timeout if need to
		enableRmiTimeout();

		try {
			// Create the info object that contains what is needed to
			// create the RMI stub. This info will also be used if the
			// stub needs to be recreated by the Invoker if there is
			// an error.
			RmiStubInfo info = new RmiStubInfo(agencyId, clazz.getSimpleName());

			// Get the RMI stub. Don't update host name since there is no 
			// indication of a problem with the cached version. Instead,
			// just use the cached version to reduce db access.
			boolean updateHostName = true;
			T rmiStub = getRmiStub(info, updateHostName);

			logger.debug("Getting proxy instance...");

			// Create proxy for the RMI stub so that the special
			// RmiCallInvocationHandler can be used to instrument
			// remote method calls.
			@SuppressWarnings("unchecked")
			T proxiedStub =
					(T) Proxy.newProxyInstance(clazz.getClassLoader(),
							new Class<?>[] { clazz },
							new RmiCallInvocationHandler(rmiStub, info));

			logger.debug("Got proxy instance. proxiedStub={}", proxiedStub);

			// Return the object that the client can use
			return (T) proxiedStub;
		} catch (Exception e) {
			logger.error("Exception occurred when creating the RMI client "
					+ "object for class={} and agencyId={}. {}", 
					clazz.getName(), agencyId, e.getMessage());
			return null;
		}
	}

	/**
	 * Creates an RMI stub based on the project name, host name, and class name.
	 * An RMI stub is a remote reference to an object.
	 * 
	 * @param info
	 *            Species the agency ID and the host name
	 * @param updateHostName
	 *            Indicates whether should update host name instead of just
	 *            getting old values from cache. Should update if there is a
	 *            problem in invoking an RMI call since perhaps the server for
	 *            an agency was moved to a new hostname. But when first creating
	 *            a client factory don't want to access db so should just use
	 *            cached value.
	 * @return The RMI stub for the remote object
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static <T extends Remote> T getRmiStub(RmiStubInfo info,
			boolean updateHostName) throws RemoteException, NotBoundException {
		// Determine the hostname depending on if should update cache or not
		String hostName = updateHostName ? 
				info.getHostNameViaUpdatedCache() : info.getHostName();
		
	    if (debugRmiServerHost.getValue() != null) {
	      logger.info("using debug RMI server value of {}", debugRmiServerHost.getValue());
	      hostName = debugRmiServerHost.getValue();
	    }

		logger.debug("Getting RMI registry for hostname={} port={} ...",
		    hostName, RmiParams.getRmiPort());
		// Get the registry
		Registry registry =
				LocateRegistry.getRegistry(hostName, RmiParams.getRmiPort());

		// Get the remote object's bind name
		String bindName =
				AbstractServer.getBindName(info.getAgencyId(),
						info.getClassName());

		logger.debug("Got RMI registry. Getting RMI stub from registry for "
				+ "bindName={} ...", bindName);

		// Get the RMI stub object from the registry
		@SuppressWarnings("unchecked")
		T rmiStub = (T) registry.lookup(bindName);

		logger.debug("Got RMI stub from registry for bindName={}", bindName);

		return rmiStub;
	}

	/**
	 * Sets the RMI timeout if haven't done so yet. This way RMI calls will not
	 * just hang if can't connect.
	 */
	private static void enableRmiTimeout() {
		synchronized (rmiTimeoutEnabled) {

			// If already enabled for process then don't need to enable it again
			if (rmiTimeoutEnabled)
				return;

			try {
				// Enable the RMI timeout by creating a new RMI socket factory
				// for
				// a socket with a timeout.
				RMISocketFactory.setSocketFactory(new RMISocketFactory() {
					public Socket createSocket(String host, int port)
							throws IOException {
						// Need to do quite a bit to get a timeout to work with
						// RMI
						Socket socket = new Socket();
						int timeoutMillis =
								timeoutSec.getValue() * Time.MS_PER_SEC;
						socket.setSoTimeout(timeoutMillis);
						socket.setSoLinger(false, 0);

						if (debugRmiServerHost.getValue() != null) {
						  host = debugRmiServerHost.getValue();
						}
						socket.connect(new InetSocketAddress(host, port),
								timeoutMillis);
						return socket;
					}

					public ServerSocket createServerSocket(int port)
							throws IOException {
						return new ServerSocket(port);
					}
				});

				// Remember that successfully enabled so don't have to enable
				// again
				rmiTimeoutEnabled = true;
			} catch (IOException e) {
				logger.error("Couldn't set RMI timeout to {} seconds.",
						timeoutSec.getValue(), e);
			}
		}
	}

	/**
	 * Returns the timeout time. Useful for error messages.
	 * 
	 * @return Timeout time in seconds
	 */
	public static int getTimeoutSec() {
		return timeoutSec.getValue();
	}

	/**
	 * Just for debugging.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String agencyId = "testProjectId";

		Hello hello = ClientFactory.getInstance(agencyId, Hello.class);
		try {
			String result = hello.concat("s1", "s2");
			System.err.println("result=" + result);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
