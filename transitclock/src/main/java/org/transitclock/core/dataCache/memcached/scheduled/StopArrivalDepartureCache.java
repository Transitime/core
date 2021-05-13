package org.transitclock.core.dataCache.memcached.scheduled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;

import org.transitclock.core.dataCache.IpcArrivalDepartureComparator;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopEvents;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import net.spy.memcached.MemcachedClient;

public class StopArrivalDepartureCache extends StopArrivalDepartureCacheInterface {

	private static StringConfigValue memcachedHost = new StringConfigValue("transitclock.cache.memcached.host", "127.0.0.1",
			"Specifies the host machine that memcache is running on.");

	private static IntegerConfigValue memcachedPort = new IntegerConfigValue("transitclock.cache.memcached.port", 11211,
			"Specifies the port that memcache is running on.");

	MemcachedClient memcachedClient = null;
	Integer expiryDuration=Time.SEC_PER_DAY;
	private static String keystub = "STOPAD_";

	private static final Logger logger = LoggerFactory.getLogger(StopArrivalDepartureCache.class);

	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results) {
		// make it obvious we are using the base implementation
		defaultPopulateCacheFromDb(results);
	}

	@Override
	protected void putAll(Map<StopArrivalDepartureCacheKey, StopEvents> map) {
		// do nothing
	}

	@Override
	public StopArrivalDepartureCacheKey putArrivalDepartureInMemory(Map<StopArrivalDepartureCacheKey, StopEvents> map, ArrivalDeparture arrivalDeparture) {
		return putArrivalDeparture(arrivalDeparture);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key) {

		Calendar date = Calendar.getInstance();
		date.setTime(key.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		key.setDate(date.getTime());
		List<IpcArrivalDeparture> result = (List<IpcArrivalDeparture>) memcachedClient.get(createKey(key));

		return result;
	}

	@Override
	public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

		Calendar date = Calendar.getInstance();
		date.setTime(arrivalDeparture.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
				date.getTime());

		List<IpcArrivalDeparture> list = getStopHistory(key);

		if (list == null)
			list = new ArrayList<IpcArrivalDeparture>();

		try {
			list.add(new IpcArrivalDeparture(arrivalDeparture));
			Collections.sort(list, new IpcArrivalDepartureComparator());
			memcachedClient.set(createKey(key), expiryDuration, list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return key;
	}

	public StopArrivalDepartureCache() throws IOException {
		super();
		memcachedClient = new MemcachedClient(
				new InetSocketAddress(memcachedHost.getValue(), memcachedPort.getValue().intValue()));
	}

	private String createKey(StopArrivalDepartureCacheKey key) {

		Calendar date = Calendar.getInstance();
		date.setTime(key.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		key.setDate(date.getTime());

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		return keystub + key.getStopid() + "_" + formatter.format(key.getDate());
	}

}
