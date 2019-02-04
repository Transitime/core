package org.transitclock.core.dataCache.memcached.scheduled;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;

import net.spy.memcached.MemcachedClient;

public class TripDataHistoryCache implements TripDataHistoryCacheInterface {
	
	private static StringConfigValue memcachedHost = 
			new StringConfigValue("transitclock.cache.memcached.host", 
					null, 
					"Specifies the host machine that memcache is running on.");
	
	private static IntegerConfigValue memcachedPort = 
			new IntegerConfigValue("transitclock.cache.memcached.port", 
					null, 
					"Specifies the port that memcache is running on.");
	MemcachedClient memcachedClient = null;
	
	
	
	private static String keystub="ERRORKEY_";
	public TripDataHistoryCache() throws IOException {
				
		memcachedClient = new MemcachedClient(new InetSocketAddress(
				memcachedHost.getValue(), memcachedPort.getValue().intValue()));		
	}

	@Override
	public List<TripKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logCache(Logger logger) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ArrivalDeparture> getTripHistory(TripKey tripKey) {
		// TODO Auto-generated method stub
		Object value = memcachedClient.get(createKey(tripKey));
		if(value instanceof List<?>)
			return (List<ArrivalDeparture>)value;
		return null;
		
	}

	@Override
	public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		// TODO Auto-generated method stub
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		

		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
		if(trip!=null)
		{
			TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
					nearestDay,
					trip.getStartTime());
			
			List<ArrivalDeparture> list  = this.getTripHistory(tripKey);
			
			if(list==null)				
				list = new ArrayList<ArrivalDeparture>();				
			
			list.add(arrivalDeparture);
			memcachedClient.set(createKey(tripKey),100, Collections.synchronizedList(list));	
		}
			
									
		return null;
	}

	@Override
	public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrivalDeparture findPreviousArrivalEvent(List<ArrivalDeparture> arrivalDepartures,
			ArrivalDeparture current) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrivalDeparture findPreviousDepartureEvent(List<ArrivalDeparture> arrivalDepartures,
			ArrivalDeparture current) {
		// TODO Auto-generated method stub
		return null;
	}
	private String createKey(TripKey tripKey)
	{
		SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMdd");
		return keystub+tripKey.getTripId()+"_"+formatter.format(tripKey.getTripStartDate())+"_"+tripKey.getStartTime();
	}
}
