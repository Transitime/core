package org.transitclock.core.dataCache;

import org.slf4j.Logger;
import org.transitclock.db.structs.PredictionForStopPath;

import java.util.List;

public interface StopPathPredictionCacheInterface {

    void logCache(Logger logger);

    @SuppressWarnings("unchecked")
    List<PredictionForStopPath> getPredictions(StopPathCacheKey key);

    void putPrediction(PredictionForStopPath prediction);

    @SuppressWarnings("unchecked")
    void putPrediction(StopPathCacheKey key, PredictionForStopPath prediction);
}
