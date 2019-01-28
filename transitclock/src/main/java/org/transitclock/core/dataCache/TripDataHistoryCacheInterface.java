package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.transitclock.db.structs.ArrivalDeparture;

public interface TripDataHistoryCacheInterface {

	List<TripKey> getKeys();

	void logCache(Logger logger);

	List<ArrivalDeparture> getTripHistory(TripKey tripKey);

	TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);
		
	void populateCacheFromDb(Session session, Date startDate, Date endDate);

	ArrivalDeparture findPreviousArrivalEvent(List<ArrivalDeparture> arrivalDepartures, ArrivalDeparture current);

	ArrivalDeparture findPreviousDepartureEvent(List<ArrivalDeparture> arrivalDepartures, ArrivalDeparture current);
}