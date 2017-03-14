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
import java.util.Collection;

import org.transitime.ipc.data.IpcAvl;

/**
 * Defines the RMI interface for sending commands or data to the server (as
 * opposed to for requesting data).
 * 
 * @author Michael
 *
 */
public interface CommandsInterface extends Remote {

	/**
	 * Sends AVL data to server.
	 * 
	 * @param avlData
	 * @return If error then contains error message string, otherwise null
	 * @throws RemoteException
	 */
	public String pushAvl(IpcAvl avlData) throws RemoteException;

	/**
	 * Sends collection of AVL data to server.
	 * 
	 * @param avlData collection of data
	 * @return If error then contains error message string, otherwise null
	 * @throws RemoteException
	 */
	public String pushAvl(Collection<IpcAvl> avlData) throws RemoteException;
	
	/*
	 * WIP This is to give a means of manually setting a vehicle unpredictable and unassigned so it will be reassigned quickly.
	 */
	public void setVehicleUnpredictable(String vehicleId) throws RemoteException;
	
}
