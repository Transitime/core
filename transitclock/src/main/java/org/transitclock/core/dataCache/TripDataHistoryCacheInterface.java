package org.transitclock.core.dataCache;

import java.util.List;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

public interface TripDataHistoryCacheInterface {


	List<IpcArrivalDeparture> getTripHistory(TripKey tripKey);

	TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);
		
	void populateCacheFromDb(List<ArrivalDeparture> results);

	IpcArrivalDeparture findPreviousArrivalEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current);

	IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current);

	List<TripKey> getKeys();
}