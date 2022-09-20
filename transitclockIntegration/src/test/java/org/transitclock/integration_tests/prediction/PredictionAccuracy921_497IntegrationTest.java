package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * Bad predictions on route 921, MET-208
 */
public class PredictionAccuracy921_497IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {

    public PredictionAccuracy921_497IntegrationTest() {
        super(createTraceConfig("921-497", "America/Chicago"));
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }
}
