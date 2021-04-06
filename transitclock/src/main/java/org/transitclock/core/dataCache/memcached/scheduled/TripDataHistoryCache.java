package org.transitclock.core.dataCache.memcached.scheduled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.dataCache.IpcArrivalDepartureComparator;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import net.spy.memcached.MemcachedClient;

public class TripDataHistoryCache implements TripDataHistoryCacheInterface {

	private static StringConfigValue memcachedHost = new StringConfigValue("transitclock.cache.memcached.host", "127.0.0.1",
			"Specifies the host machine that memcache is running on.");

	private static IntegerConfigValue memcachedPort = new IntegerConfigValue("transitclock.cache.memcached.port", 11211,
			"Specifies the port that memcache is running on.");

	MemcachedClient memcachedClient = null;

	private static String keystub = "TRIPHISTORY_";
	Integer expiryDuration=Time.SEC_PER_DAY*28;
	public TripDataHistoryCache() throws IOException {

		memcachedClient = new MemcachedClient(
				new InetSocketAddress(memcachedHost.getValue(), memcachedPort.getValue().intValue()));
	}

	
	public List<TripKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void logCache(Logger logger) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {

		Object value = memcachedClient.get(createKey(tripKey));
		if (value instanceof List<?>)
			return (List<IpcArrivalDeparture>) value;
		return null;

	}

	@Override
	public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);

		DbConfig dbConfig = Core.getInstance().getDbConfig();

		Trip trip = dbConfig.getTrip(arrivalDeparture.getTripId());
		if (trip != null) {
			TripKey tripKey = new TripKey(arrivalDeparture.getTripId(), nearestDay, trip.getStartTime());

			List<IpcArrivalDeparture> list = this.getTripHistory(tripKey);

			if (list == null)
				list = new ArrayList<IpcArrivalDeparture>();

			try {
				list.add(new IpcArrivalDeparture(arrivalDeparture));
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			memcachedClient.set(createKey(tripKey), expiryDuration, list);
		}

		return null;
	}

	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results) {

		for(ArrivalDeparture result : results)		
		{						
			// TODO this might be better done in the database.						
			if(GtfsData.routeNotFiltered(result.getRouteId()))
			{
				TripDataHistoryCacheFactory.getInstance().putArrivalDeparture(result);
			}
		}		

	}

	@Override
	public IpcArrivalDeparture findPreviousArrivalEvent(List<IpcArrivalDeparture> arrivalDepartures,
			IpcArrivalDeparture current) {
		Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());
		for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			if(tocheck.getStopId().equals(current.getStopId()) && (current.isDeparture() && tocheck.isArrival()))
			{
				return tocheck;
			}			
		}
		return null;
	}

	@Override
	public IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDepartures,
			IpcArrivalDeparture current) {
		Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());							
		for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			try {
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isArrival() && tocheck.isDeparture()))
				{
					return tocheck;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
		return null;	
	}

	private String createKey(TripKey tripKey) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		return keystub + tripKey.getTripId() + "_" + formatter.format(tripKey.getTripStartDate());				
	}
	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
}
