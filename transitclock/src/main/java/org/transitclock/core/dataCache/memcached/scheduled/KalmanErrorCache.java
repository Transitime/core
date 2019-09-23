package org.transitclock.core.dataCache.memcached.scheduled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.utils.Time;

import net.spy.memcached.MemcachedClient;

public class KalmanErrorCache implements ErrorCache {

	private static StringConfigValue memcachedHost = new StringConfigValue("transitclock.cache.memcached.host", "127.0.0.1",
			"Specifies the host machine that memcache is running on.");

	private static IntegerConfigValue memcachedPort = new IntegerConfigValue("transitclock.cache.memcached.port", 11211,
			"Specifies the port that memcache is running on.");

	MemcachedClient memcachedClient = null;
	private static String keystub = "KALMANERROR_";
	Integer expiryDuration=Time.SEC_PER_DAY*28;
	
	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);
	
	public KalmanErrorCache() throws IOException {
		memcachedClient = new MemcachedClient(
				new InetSocketAddress(memcachedHost.getValue(), memcachedPort.getValue().intValue()));
	}

	@Override
	public KalmanError getErrorValue(Indices indices) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		
		return getErrorValue(key);
	}

	@Override
	public KalmanError getErrorValue(KalmanErrorCacheKey key) {

		KalmanError value = (KalmanError)memcachedClient.get(createKey(key));
		return value;
	}

	@Override
	public void putErrorValue(Indices indices, Double value) {
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		putErrorValue(key, value);
	}

	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		memcachedClient.set(createKey(key), expiryDuration, value);

	}

	
	public List<KalmanErrorCacheKey> getKeys() {
		
		logger.info("Not implemented for memecached.");
		return null;
	}

	private String createKey(KalmanErrorCacheKey key) {
		return keystub + key.getTripId() + "_" + key.getStopPathIndex();

	}

}
