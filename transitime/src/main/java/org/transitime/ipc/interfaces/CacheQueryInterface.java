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
import java.util.List;

import org.transitime.ipc.data.IpcArrivalDeparture;

/**
 * Defines the RMI interface used for obtaining cache runtime information. 
 * 
 * @author Sean Og Crudden
 *
 */
public interface CacheQueryInterface extends Remote {
		
	/**
	 * Returns a list of current arrival or departure events for a specified stop that are in the cache.
	 * 	
	 * @param stopId
	 * @return List of IpcArrivalDeparture objects for the stop, one
	 *         for each event.
	 * @throws RemoteException
	 */
	public List<IpcArrivalDeparture> getStopArrivalDepartures(
			String stopId) 
				throws RemoteException;
	
	/**
	 * 
	 * Returns the number of entries in the cacheName cache
	 * @param cacheName
	 * @return
	 * @throws RemoteException
	 */
	public Integer entriesInCache(String cacheName)
			throws RemoteException;
	
	

}
