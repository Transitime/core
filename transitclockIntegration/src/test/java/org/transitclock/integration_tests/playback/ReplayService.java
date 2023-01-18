package org.transitclock.integration_tests.playback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.integration_tests.prediction.TraceConfig;
import org.transitclock.utils.DateRange;

import java.util.*;

/**
 * Integration test services for replaying data.
 */
public class ReplayService {

    private static final Logger logger = LoggerFactory.getLogger(ReplayService.class);

    private String id;
    private ReplayAnalysis analysis;

    private ReplayLoader loader;

    public Collection<CombinedPredictionAccuracy> getCombinedPredictionAccuracy() {
        return loader.getCombinedPredictionAccuracies();
    }

    public ReplayService(String id, String outputDirectory) {
         analysis = new ReplayAnalysis();
         loader = new ReplayLoader(outputDirectory);
         this.id = id;
         System.setProperty("transitclock.integration_test.enabled", "true");
    }

    public List<ArrivalDeparture> run(TraceConfig config) {
        // Run trace
        logger.info(config.getDescription());
        System.out.println(config.getDescription());
        DateRange range = PlaybackModule.runTrace(config, null);
        return loader.queryArrivalDepartures(range, config.getArrivalDepartureCsv());

    }

    public void loadPastPredictions(String predictionsCsvFileName) {
        loader.loadPredictionsFromCSV(predictionsCsvFileName);
    }

    public List<String> accumulate(List<ArrivalDeparture> arrivalDepartures) {
        // Fill new predictions
        try {
            return loader.accumulate(id, arrivalDepartures);
        } catch (Throwable t) {
            logger.error("accumulate failed: {}", t, t);
        }
        return null;
    }

    public ReplayResults compare() {
        return analysis.compare(getCombinedPredictionAccuracy());
    }
}
