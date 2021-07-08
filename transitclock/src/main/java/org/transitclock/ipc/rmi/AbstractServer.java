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
package org.transitclock.ipc.rmi;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.logging.Markers;
import org.transitclock.utils.Timer;

/**
 * This class does all of the work on the server side for an RMI object. A
 * server side RMI object should inherit/extend this class. The constructor
 * automatically registers the RMI server object with the registry. It also
 * periodically rebinds the object every 30 seconds so that if the rmiregistry
 * gets restarted the RMI object will then automatically become available again.
 * Sends an e-mail alert when rebinding if the rmiregistry is not available.
 * <p>
 * Once a AbstractServer superclass object is created it will continue to exist.
 * <p>
 * AbstractServer configures RMI to not use port 0, basically a random port,
 * when establishing secondary communication. Instead, RMI is configured to use
 * port 2098. In this way the firewalls for the servers can be configured to
 * only allow traffic on the two ports 2099 & 2098 for RMI. If there are
 * multiple systems running on a single server then need to use a separate
 * secondary port for each system (such as 2097, 2096, etc). Using a specific
 * port for secondary communication is much safer than having to completely open
 * up network traffic.
 * <p>
 * If another agency is already using the secondary RMI port on the server then
 * will get an exception and e-mail notification is sent out.
 * 
 * @author SkiBu Smith
 *
 */
public abstract class AbstractServer {
	
	protected boolean constructed = false;

	// Need to store this so can rebind
	private String bindName;
	
	private String agencyId;
	
	// Need to store this so can rebind
	private Remote stub;
	
	// Only want to send e-mail the first time a rebind exception
	// occurs. This way don't flood users with e-mails.
	private boolean firstRebindTrySoLogError = true;
	
	// If sending out message that rebind exception failed then
	// should send out another message when successful again so
	// that supervisors know that situation handled. 
	private boolean errorEmailedSoAlsoNotifyWhenSuccessful = false;
	
	// How frequently should rebind to rmiregistry in case 
	// it is restarted.
	private static final long REBIND_RATE_SEC = 30;
	
	// Cache the registry. Only need one for entire application.
	private static Registry registry = null;
	
	// Share the timer. Don't want separate thread for every RMI class
	private static ScheduledThreadPoolExecutor rebindTimer = Timer.get();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(AbstractServer.class);

	/********************** Member Functions **************************/
	
	/**
	 * Binds the RMI implementation object to the registry so that it can be
	 * accessed by a client. It also periodically rebinds the object every 30
	 * seconds so that if the rmiregistry gets restarted the RMI object will
	 * then automatically become available again. Sends an e-mail alert when
	 * rebinding if the rmiregistry is not available.
	 * <p>
	 * Protected because should only be instantiated by a superclass factory.
	 * <p>
	 * This constructor "leaks" a reference to the object before the constructor
	 * has completed via UnicastRemoteObject.exportObject(). This normally
	 * should not be done because the function could cause the object to be used
	 * before it has been fully constructed. But by leaking the object we can
	 * guarantee that any Server class that inherits from this class will be
	 * properly initialized when the subclass constructor is called. This is
	 * fine as long as the Server object constructor doesn't do any further
	 * initialization.
	 * 
	 * @param agencyId
	 *            For registering object with rmiregistry. Can have several
	 *            different projects running on a server so need to specify the
	 *            project name.
	 * @param objectName
	 *            For registering object with rmiregistry. Will usually use
	 *            something like ClassName.class.getSimpleName()
	 */
	protected AbstractServer(String agencyId, String objectName) {
		start(agencyId, objectName);
	}

	protected AbstractServer(){}

	protected void start(String agencyId, String objectName){
		this.agencyId = agencyId;

		try {
			// First make sure that this object is a subclass of Remote
			// since this class is only intended to be a parent class
			// of Remote.
			if (!(this instanceof Remote)) {
				logger.error("Class {} is not a subclass of Remote. Therefore "
								+ "it cannot be used with {}",
						this.getClass().getName(), getClass().getSimpleName());
				return;
			}
			Remote remoteThis = (Remote) this;

			logger.info("Setting up AbstractServer for RMI using secondary "
					+ "port={}", RmiParams.getSecondaryRmiPort());
			// Export the RMI stub. Specify that should use special port for
			// secondary RMI communication.
			stub = UnicastRemoteObject.exportObject(remoteThis,
					RmiParams.getSecondaryRmiPort());

			// Make sure the registry exists
			if (registry == null) {
				try {
					// Start up the RMI registry. If it has already been
					// started manually or by another process then will
					// get an exception, which is fine.
					LocateRegistry.createRegistry(RmiParams.getRmiPort());
				} catch (Exception e) {
					// Most likely registry was already running
					logger.debug("Exception occurred when trying to create " +
									"RMI registry. Most likely the registry was " +
									"already running, which is fine. Message={}",
							e.getMessage());
				}
				registry = LocateRegistry.getRegistry(RmiParams.getRmiPort());
			}

			// Bind the stub to the RMI registry so that it can be accessed by
			// name by the client.
			bindName = getBindName(agencyId, objectName);

			// Bind the stub to the RMI registry in a loop so that even if
			// rmiregistry is restarted the stub will quickly get bound to it.
			// rebind() is called immediately and then again every
			// REBIND_RATE_SEC seconds.
			rebindTimer.scheduleAtFixedRate(
					// Call rebind() using anonymous class
					new Runnable() {
						public void run() {
							rebind();
						}
					}, 0, REBIND_RATE_SEC, TimeUnit.SECONDS);

			constructed = true;
		} catch (Exception e) {
			// Log the error. Since RMI is critical send out e-mail as well so
			// that the issue is taken care of.
			logger.error(Markers.email(),
					"For agencyId={} error occurred when constructing a RMI {}",
					AgencyConfig.getAgencyId(), getClass().getSimpleName(),
					e);
		}
	}
	
	/**
	 * Rebinds the stub object to the RMI registry. This is important because
	 * the RMI registry could be terminated and restarted, losing the information
	 * on what has been bound. 
	 */
    private void rebind() {
    	logger.debug("Trying to rebind {} to RMI registry.", bindName);

    	try {
    		try {
				// Use rebind() instead of bind() for frequent case where 
    			// object already bound
				registry.rebind(bindName, stub);
			} catch (Exception e) {
				// An error occurred, likely because rmiregistry not running. This
				// can happen if a process started the RMI registry internally but
				// then the process died.
				logger.warn("Error occurred when trying to rebind RMI to {}. " +
						"Therefore trying to automatically restart the " +
						"rmiregistry. {}", 
						bindName, e.getMessage());
				LocateRegistry.createRegistry(RmiParams.getRmiPort());
				// Now that registry has restarted try rebinding again
				registry.rebind(bindName, stub);
			}
			
			logger.debug("Successfully rebound {} to RMI registry.", bindName);
			
			// Was successful rebinding so remember that for next time 
			// try to rebind.
			firstRebindTrySoLogError = true;
			
			// If e-mailed error message indicating problem then e-mail
			// another notification indicating problem has been resolved
			if (errorEmailedSoAlsoNotifyWhenSuccessful) {
				String hostname = null;
				try {
					hostname = java.net.InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e1) {
				}
				logger.error(Markers.email(), 
						"For agencyId={} problem with rmiregistry on host {} "
						+ "has been resolved.", 
						AgencyConfig.getAgencyId(), hostname);
				errorEmailedSoAlsoNotifyWhenSuccessful = false;
			}
		} catch (Exception e) {
			if (firstRebindTrySoLogError) {
				// This isn't the first try so remember that so don't send out
				// a flood of error e-mails.
				firstRebindTrySoLogError = false;

				// Log error. This is an important one because someone needs to
				// start up the rmiregistry for the host. Therefore send out
				// an e-mail alerting appropriate people.
				String msg = rebindErrorMessage(e);
				logger.error(Markers.email(), msg, e);
								
				errorEmailedSoAlsoNotifyWhenSuccessful = true;
			} else {
				// Have already logged and sent out e-mail so this time
				// just log it as a warning (and don't send e-mail).
				logger.warn(rebindErrorMessage(e));
			}
		}
	}
    
	/**
	 * Returns appropriate error message for when rebind error occurs.
	 * 
	 * @param e
	 * @return
	 */
    private String rebindErrorMessage(Exception e) {
		String hostname = null;
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
		}
		
		String msg = "It appears that the rmiregistry is not running on host "
				+ hostname
				+ ". Make sure it is started immediately. "
				+ "Exception occurred when rebinding "
				+ bindName + ": " + e.getMessage();
		return msg;
    }
    
	/**
	 * The name that the server object is bound to needs both the classname
	 * to identify the object but also the agencyId since multiple 
	 * projects can be running on a machine that uses a single RMI registry.
	 * @param agencyId
	 * @param className
	 * @return
	 */
	public static String getBindName(String agencyId, String className) {
		return agencyId + "-" + className;
	}
	
	public String getAgencyId() {
		return agencyId;
	}
	
	/**
	 * Specifies if this object was successfully constructed and 
	 * is ready for use.
	 * @return
	 */
	public boolean isConstructed() {
		return constructed;
	}

}