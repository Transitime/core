package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * Build off of Abstract Prediction Integration Test.
 */
public class PredictionAccuracyS2Run2113IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    private static final String GTFS = "classpath:gtfs/S2";
    private static final String AVL = "classpath:avl/S2_2113.csv";
    private static final String PREDICTIONS_CSV = "classpath:pred/S2_2113.csv";

    private static final String OUTPUT_DIRECTORY = "/tmp/output/S2_2113";

    public PredictionAccuracyS2Run2113IntegrationTest() {
        super("S2_2113", OUTPUT_DIRECTORY, GTFS, AVL, PREDICTIONS_CSV, "America/New_York");
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }
}
