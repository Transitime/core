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

package org.transitclock.ipc.servers;

import java.rmi.RemoteException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.ipc.data.IpcServerStatus;
import org.transitclock.ipc.interfaces.ServerStatusInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.monitoring.AgencyMonitor;

/**
 * Runs on the server side and receives IPC calls and returns results.
 *
 * @author SkiBu Smith
 *
 */
public class ServerStatusServer extends AbstractServer 
	implements ServerStatusInterface {

	// Should only be accessed as singleton class
	private static ServerStatusServer singleton;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ServerStatusServer.class);

	/********************** Member Functions **************************/

	/**
	 * 
	 * @param agencyId
	 * @return
	 */
	public static ServerStatusServer start(String agencyId) {
		if (singleton == null) {
			singleton = new ServerStatusServer(agencyId);
		}
		
		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error("Tried calling ServerStatusServer.start() for " +
					"agencyId={} but the singleton was created for projectId={}", 
					agencyId, singleton.getAgencyId());
			return null;
		}
		
		return singleton;	
	}
	
	/**
	 * Constructor is private because singleton class
	 * 
	 * @param projectId
	 * @param objectName
	 */
	private ServerStatusServer(String projectId) {
		super(projectId, ServerStatusInterface.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ServerStatusInterface#get()
	 */
	@Override
	public IpcServerStatus get() throws RemoteException {
		AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(getAgencyId());
		return new IpcServerStatus(agencyMonitor.getMonitorResults());
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.ServerStatusInterface#monitor()
	 */
	@Override
	public String monitor() throws RemoteException {
		// Monitor everything having to do with an agency server. Send
		// out any notifications if necessary. Return any resulting
		// error message.
		AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(getAgencyId());
		String resultStr = agencyMonitor.checkAll();

		return resultStr;
	}

	@Override
	public Date getCurrentServerTime() throws RemoteException {
		return new Date(Core.getInstance().getSystemTime());
	}

}
