package org.transitclock.core.dataCache.memcached.scheduled;

import java.util.List;

import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;

public class KalmanErrorCache implements ErrorCache {

	public KalmanErrorCache() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Double getErrorValue(Indices indices) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getErrorValue(KalmanErrorCacheKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putErrorValue(Indices indices, Double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<KalmanErrorCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

}
