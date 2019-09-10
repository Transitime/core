package org.transitclock.core.dataCache.ehcache.dummy;

import org.slf4j.Logger;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.StopPathPredictionCacheInterface;
import org.transitclock.db.structs.PredictionForStopPath;

import java.util.List;

public class DummyStopPathPredictionCache implements StopPathPredictionCacheInterface {
    @Override
    public void logCache(Logger logger) {
        return;
    }

    @Override
    public List<PredictionForStopPath> getPredictions(StopPathCacheKey key) {
        return null;
    }

    @Override
    public void putPrediction(PredictionForStopPath prediction) {
        return;
    }

    @Override
    public void putPrediction(StopPathCacheKey key, PredictionForStopPath prediction) {
        return;
    }
}
