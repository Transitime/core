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
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.transitime.ipc.data.IpcArrivalDeparture;
import org.transitime.ipc.data.IpcHistoricalAverage;
import org.transitime.ipc.data.IpcHistoricalAverageCacheKey;
import org.transitime.ipc.data.IpcHoldingTime;
import org.transitime.ipc.data.IpcHoldingTimeCacheKey;
import org.transitime.ipc.data.IpcKalmanErrorCacheKey;

/**
 * Defines the RMI interface used for obtaining holding time information. 
 * 
 * @author Sean Og Crudden
 *
 */
public interface HoldingTimeInterface extends Remote {

	public IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId) throws RemoteException;
	public IpcHoldingTime getHoldTime(String stopId, String vehicleId) throws RemoteException;
	
	}
