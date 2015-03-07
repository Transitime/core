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

package org.transitime.ipc.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.transitime.ipc.data.IpcServerStatus;

/**
 * RMI interface for determining the status of the server.
 *
 * @author SkiBu Smith
 *
 */
public interface ServerStatusInterface extends Remote {

	/**
	 * Gets from the server a IpcStatus object indicating
	 * the status of the server.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public IpcServerStatus get() throws RemoteException;
	
	/**
	 * Monitors the agency server for problems. If there is a problem then a
	 * message indicating such is returned. Sending out notifications is done by
	 * the server side.
	 * 
	 * @return Error message if there is one, otherwise null
	 * @throws RemoteException
	 */
	public String monitor() throws RemoteException;
	
}
