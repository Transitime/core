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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.utils.IntervalTimer;

/**
 * This code is called on the client side when RMI object is accessed.
 * The RMI stub methods could be called directly but then one would
 * have no control over the invocation. This is a great use of a proxy
 * object with a custom InvocationHandler. We proxy the RMI stub and then when
 * any of the remote methods are called this class' invocation handler adds 
 * logging, timing, and handles exceptions. This way we can make sure that
 * a call will work properly even if the server or the registry is 
 * restarted or if there is some kind of temporary network problem.
 * 
 * @author SkiBu Smith
 *
 */
public class RmiCallInvocationHandler implements InvocationHandler {

	// The object that does the work. For the RMI package this will
	// be the RMI stub that communicates with the server.
	private Object delegate;

	// For being able to recreate RMI stub objects when there are 
	// errors. Also for logging since can't otherwise get classname 
	// for a proxied object.
	private final RmiStubInfo info;
	
	// For limiting how many RMI calls are in process for a particular
	// host. Don't want too many calls at once because if the server
	// gets stops or slows due to something like a stop the world 
	// garbage collection want to make sure that a web server doesn't
	// keep on creating new connections.
	private static final ConcurrentHashMap<String, AtomicInteger> currentCallsByProjectMap =
			new ConcurrentHashMap<String, AtomicInteger>();
	// Set default value to 100 
	private static int maxConcurrentCallsPerProject = 100;
	
	// Logging
	private static final Logger logger = 
			LoggerFactory.getLogger(RmiCallInvocationHandler.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor.
	 * 
	 * @param delegate
	 * @param info
	 */
	public RmiCallInvocationHandler(Object delegate, RmiStubInfo info) {
		this.delegate = delegate;
		this.info = info;
	}

	/**
	 * @return current setting of max concurrent calls per project
	 */
	public static int getMaxConcurrentCallsPerProject() {
		return maxConcurrentCallsPerProject;
	}
	
	/**
	 * For if need to change max concurrent calls per project
	 * 
	 * @param maxConcurrentCalls
	 *            New value for max concurrent calls per project
	 */
	public static void setMaxConcurrentCallsPerProject(int maxConcurrentCalls) {
		// Don't allow a value less than zero
		if (maxConcurrentCalls < 1)
			maxConcurrentCalls = 1;
		
		maxConcurrentCallsPerProject = maxConcurrentCalls;
	}
	
	private static AtomicInteger getAccessCounter(String projectId) {
		AtomicInteger counter = currentCallsByProjectMap.get(projectId);
		if (counter == null) {
			currentCallsByProjectMap.putIfAbsent(projectId, new AtomicInteger());
			counter = currentCallsByProjectMap.get(projectId);
		}
		return counter;
	}
	
	private static class ConcurrentAccessException extends Throwable {
		// Needed because exceptions are Serializable
		private static final long serialVersionUID = 2130715701627365891L;

		public ConcurrentAccessException(String message) {
			super(message);
		}
	}
	
	/**
	 * The invoke() method does all the work. It calls the remote method
	 * but it also logs debug info such as how long the call took. Also
	 * handles exceptions. If there is an RMI RemoteException then will
	 * automatically try to rebind and then try again. This allows RMI
	 * to work even if there is a temporary problem like the RMI registry
	 * changing. If it is an exception thrown by the server object 
	 * then that exception is propagated to the client. Also controls
	 * how many simultaneous RMI calls there are from this client to a
	 * project. If there are too many calls already happening then an
	 * exception will be thrown. This prevents a client such as a web
	 * server from opening up too many connections when the project gets
	 * bogged down.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// For keeping track of which try we are on so can retry
		// if get a RemoteException.
		int tryNumber = 0;
		
		// When dispatching method invocations delegate object 3 methods from 
		// java.lang.Object need special handling: toString(), hashCode() and 
		// equals(Object). Since they are related to the proxy object identity, 
		// they should be serviced directly by the handler and not passed to 
		// the proxy. This code is from
		// http://javahowto.blogspot.com/2011/12/java-dynamic-proxy-example.html
		if (Object.class  == method.getDeclaringClass()) {
			String name = method.getName();
			if("equals".equals(name)) {
				return proxy == args[0];
			} else if("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if("toString".equals(name)) {
				return proxy.getClass().getName() + "@" +
						Integer.toHexString(System.identityHashCode(proxy)) +
						", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		
		// Keep looping until get valid result or exception is thrown.
		// If get RemoteException will retry once in case rebind to
		// server object helps or if there was an unusual and short
		// lived networking problem.
		while (true) {
			// Update how many tries used to invoke method call
			++tryNumber;
			
			try {
				// Not a java.lang.Object method that needs to be handled specially so
				// handle it normally. But instrument it with a timer and logging
				// and also handle RemoteExceptions.
				IntervalTimer t = new IntervalTimer();
				boolean debug = logger.isDebugEnabled();
				if (debug) {
					logger.debug("About to call remote method " + 
							info.getClassName() + "." + method.getName() + "()" +		
							" for project " + info.getProjectId());
				}
				
				// Do the actual invocation of the method!
				Object result = invokeIfNotTooMuchConcurrentAccess(method, args);
							
				// If debug, log how long remote method took
				if (debug) {
					logger.debug("Remote method " + 
							info.getClassName() + "." + method.getName() + "()" +
							" for project " + info.getProjectId() +
							" took " + 
							t.elapsedMsec() + " msec.");
				}
				
				// Finally done so return results of method call
				return result;
			} catch (RemoteException e) {
				// Getting a remote exception could mean the server object
				// needs to be rebound. Perhaps the server was restarted or
				// moved. For this situation want to create new RMI stub
				// and try again, if not too many tries.
				// Note that this can in turn throw RemoteException or NotBoundException
				delegate = ClientFactory.getRmiStub(info);
	
				// If this is already the second try give up. Trying yet again
				// right away is most likely not going to help with a networking
				// problem or the server not running.
				if (tryNumber >= 2)
					throw new RemoteException(e.getMessage() + 
							". Gave up after second attempt.");
			} catch (ConcurrentAccessException e) {
				// Encountered a ConcurrentAccessException which means the RMI
				// call could not be done. Throw a RemoteException since that is
				// what these methods are supposed to throw. Need to handle 
				// differently from RemoteException because for 
				// ConcurrentAccessException don't want to rebind and such since
				// that is not the problem.
				throw new RemoteException(e.getMessage());
			}
		}	
	}
	
	/**
	 * Checks to see how many RMI calls are currently active for the project. If
	 * not too many then the RMI call is invoked. But if too many then
	 * ConcurrentAccessException is thrown.
	 * 
	 * @param method
	 * @param args
	 * @return the Object result of the RMI call
	 * @throws Throwable
	 *             the RMI method can throw just about anything. And this method
	 *             might also throw ConcurrentAccessException
	 */
	private Object invokeIfNotTooMuchConcurrentAccess(Method method, Object[] args) 
			throws Throwable {
		// If don't have too many simultaneous calls happening for the 
		// project server then execute the RMI call. But if too many
		// calls simply log the error and throw exception. This is an 
		// important limit because if something goes wrong with the 
		// project and it gets behind (due to stop the world garbage
		// collecting, denial of service attack, etc) don't want to
		// burden the project even more with additional calls. 
		// Therefore when behind want to return as quickly as possible.
		AtomicInteger accessCounter = getAccessCounter(info.getProjectId());
		if (accessCounter.get() >= getMaxConcurrentCallsPerProject()) {
			// Currently too many RMI calls is progress so log error
			// and throw exception
			String message = "Reached MAX_CURRENT_CALLS_PER_PROJECT="
					+ getMaxConcurrentCallsPerProject()
					+ " of concurrent RMI calls when calling remote "
					+ "method " + info.getClassName() + "."
					+ method.getName() + "() for project "
					+ info.getProjectId() + " so throwing exception.";
			logger.error(message);
			throw new ConcurrentAccessException(message);
		} else {
			try {
				// Keep track that another RMI call is being initiated
				accessCounter.incrementAndGet();
				
				// Actually make the RMI call
				Object result = lowLevelInvoke(method, args);
				return result;
			} finally {
				// Make sure that access counter decrement no matter what
				accessCounter.decrementAndGet();
			}
		}
	}
	
	/**
	 * Does the low level RMI invocation and throws proper exception if there is
	 * a problem.
	 * 
	 * @param method
	 * @param args
	 * @return the Object result of the RMI call
	 * @throws Throwable
	 *             the RMI method can throw just about anything
	 */
	private Object lowLevelInvoke(Method method, Object[] args) 
			throws Throwable {
		// Actually invoke the method on the RMI object
		try {
			// Invoke it
			Object result = method.invoke(delegate, args);
			return result;
		} catch (InvocationTargetException e) {
			// If there is an exception on the server side then 
			// we get a InvocationTargetException here. Convert
			// that exception to one that relates directly.
			Throwable causeException = e.getCause();
			
			// If the server is not available then we get a 
			// ConnectException
			if (causeException instanceof ConnectException) {
				// ConnectException means the server was not available
				// even though the object was registered.
				logger.error("When calling remote method " + 
						info.getClassName() + "." + method.getName() + "()" +
						" for project " + info.getProjectId() +
						" encountered exception " + causeException.getMessage() + "." +
						" This is most likely due to the project not currently running.");
				// No point in just retrying again since the server isn't 
				// going to happen to start up again right away and don't
				// want to wait here before retrying since because if web
				// server request then it would hold connection open for a 
				// while which with a heavily loaded server could crash the site.
				// Therefore simply throw an exception. Throw RemoteException
				// since the class already declares that such exceptions
				// should be expected.
				throw new RemoteException(causeException.getMessage(), causeException);
			} else {
				// Got an unchecked exception such as one declared with the
				// method or an unnamed one such as NullPointerException.
				// Just debug log this since it will be logged in full on 
				// the server side. Then pass the cause exception to the
				// caller.
				logger.debug("When calling remote method " + 
						info.getClassName() + "." + method.getName() + "()" +
						" for project " + info.getProjectId() +
						" encountered exception " +	causeException.getMessage());
				throw causeException;
			}
		}		
	}
	
}