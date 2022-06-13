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
package org.transitclock.ipc.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcHistoricalAverage;
import org.transitclock.ipc.data.IpcHistoricalAverageCacheKey;
import org.transitclock.ipc.data.IpcHoldingTimeCacheKey;
import org.transitclock.ipc.data.IpcKalmanErrorCacheKey;

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
	
	/**
	 * Returns the historical average value for the trip stopPathIndex that is held in the HistoricalAverageCache
	 * @param tripId
	 * @param stopPathIndex
	 * @return IpcHistoricalAverage
	 * @throws RemoteException
	 */
	public IpcHistoricalAverage getHistoricalAverage(String tripId, Integer stopPathIndex)
		throws RemoteException;
	
	
	/**
	 * Return the arrivals and departures for a trip on a specific day and start time
	 * @param routeId
	 * @param directionId
	 * @param date
	 * @param starttime
	 * @return
	 * @throws RemoteException
	 */
	public List<IpcArrivalDeparture> getTripArrivalDepartures(String routeId, String directionId,
																														LocalDate date, Integer starttime)
		throws RemoteException;
	
	
	/**
	 * @return a list of the keys that have values in the historical average cache for schedules based services
	 * @throws RemoteException
	 */
	public List<IpcHistoricalAverageCacheKey> getScheduledBasedHistoricalAverageCacheKeys()
		throws RemoteException;
	
	/**
	 * @return a list of the keys that have values in the historical average cache for frequency based services.
	 * @throws RemoteException
	 */
	public List<IpcHistoricalAverageCacheKey> getFrequencyBasedHistoricalAverageCacheKeys()
		throws RemoteException;
	
	/**
	 * @return a list of the keys that have values in the Kalman error value cache
	 * @throws RemoteException
	 */
	public List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys()
		throws RemoteException;

	/**
	 * @return a list of the keys for the holding times in the cache
	 * @throws RemoteException
	 */
	public List<IpcHoldingTimeCacheKey> getHoldingTimeCacheKeys()
		throws RemoteException;
	
	
	/**
	 * Return the latest Kalman error value for a the stop path of a trip.
	 * @param routeId
	 * @param directionId
	 *
	 * @return
	 * @throws RemoteException
	 */	
	public  Double getKalmanErrorValue(String routeId, String directionId,
																		 Integer startTimeSecondsIntoDay,
																		 String originStopId, String destinationStopId) throws RemoteException;

}
