package org.transitclock.core.dataCache;

import java.util.List;

import org.slf4j.Logger;
import org.transitclock.core.Indices;

public interface ErrorCache {
	
	KalmanError getErrorValue(Indices indices);

	KalmanError getErrorValue(KalmanErrorCacheKey key);

	void putErrorValue(Indices indices, Double value);
		
	void putErrorValue(KalmanErrorCacheKey key, Double value);

	List<KalmanErrorCacheKey> getKeys();



}