package org.transitclock.core.dataCache.memcached.scheduled;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.utils.Time;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

public class KalmanErrorCache implements ErrorCache {

	private static StringConfigValue memcachedHost = new StringConfigValue("transitclock.cache.memcached.host", "127.0.0.1",
			"Specifies the host machine that memcache is running on.");

	private static IntegerConfigValue memcachedPort = new IntegerConfigValue("transitclock.cache.memcached.port", 11211,
			"Specifies the port that memcache is running on.");

	private static IntegerConfigValue memcachedPoolSize = new IntegerConfigValue("transitclock.cache.memcached.poolSize",
					5,
					"The number of concurrent memcached clients to instantiate");

	MemcachedClient[] memcachedClientArray = null;
	private static String keystub = "KALMANERROR_";
	private static String dwellKeystub = "DWELLERROR_";
	private Random rng = new Random();
	Integer expiryDuration=Time.SEC_PER_DAY*28;
	
	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);
	
	public KalmanErrorCache() throws IOException {
		memcachedClientArray = new MemcachedClient[memcachedPoolSize.getValue()];
		for (int i = 0; i < memcachedPoolSize.getValue(); i++) {
			memcachedClientArray[i] = new MemcachedClient(
							new InetSocketAddress(memcachedHost.getValue(), memcachedPort.getValue().intValue()));
		}
	}

	private MemcachedClient getMemcachedClient() {
		return memcachedClientArray[rng.nextInt(memcachedPoolSize.getValue())];
	}

	@Override
	public KalmanError getErrorValue(Indices indices) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		return getErrorValue(key);
	}

	@Override
	public KalmanError getDwellErrorValue(Indices indices) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		return getDwellErrorValue(key);
	}

	@Override
	public KalmanError getErrorValue(KalmanErrorCacheKey key) {
		Double errorValue = (Double) getMemcachedClient().get(createKey(key));
		if (errorValue == null || errorValue.isNaN()) {
			return null;
		}
		return new KalmanError(errorValue);
	}

	@Override
	public KalmanError getDwellErrorValue(KalmanErrorCacheKey key) {
		Double errorValue = (Double) getMemcachedClient().get(createDwellKey(key));
		if (errorValue == null || errorValue.isNaN()) {
			return null;
		}
		return new KalmanError(errorValue);
	}

	@Override
	public void putErrorValue(Indices indices, Double value) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		putErrorValue(key, value);
	}

	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		getMemcachedClient().set(createKey(key), expiryDuration, value);
	}

	@Override
	public void putDwellErrorValue(Indices indices, Double value) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		putDwellErrorValue(key, value);
	}

	@Override
	public void putDwellErrorValue(KalmanErrorCacheKey key, Double value) {
		getMemcachedClient().set(createDwellKey(key), expiryDuration, value);
	}


	public List<KalmanErrorCacheKey> getKeys() {
		logger.info("Not implemented for memecached.");
		return null;
	}

	private String createKey(KalmanErrorCacheKey key) {

		return keystub + key.getRouteId()
						+ "_" + key.getDirectionId()
						+ "_" + key.getStartTimeSecondsIntoDay()
						+ "_" + key.getOriginStopId()
						+ "_" + key.getDestinationStopId();
	}
	private String createDwellKey(KalmanErrorCacheKey key) {
		return dwellKeystub
						+ key.getRouteId()
						+ "_" + key.getDirectionId()
						+ "_" + key.getStartTimeSecondsIntoDay()
						+ "_" + key.getOriginStopId()
						+ "_" + key.getDestinationStopId();
	}


}
