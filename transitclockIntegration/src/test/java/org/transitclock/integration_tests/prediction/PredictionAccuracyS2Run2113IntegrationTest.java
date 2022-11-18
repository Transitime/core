package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * Build off of Abstract Prediction Integration Test.
 */
public class PredictionAccuracyS2Run2113IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    private static final String GTFS = "classpath:tests/S2_2113/gtfs";
    private static final String AVL = "classpath:tests/S2_2113/avl.csv";
    private static final String PREDICTIONS_CSV = "classpath:tests/S2_2113/pred.csv";

    private static final String OUTPUT_DIRECTORY = "/tmp/output/S2_2113";

    public PredictionAccuracyS2Run2113IntegrationTest() {
        super("S2_2113", OUTPUT_DIRECTORY, GTFS, AVL, PREDICTIONS_CSV, "America/New_York", "Build off of Abstract Prediction Integration Test.");
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }
}
