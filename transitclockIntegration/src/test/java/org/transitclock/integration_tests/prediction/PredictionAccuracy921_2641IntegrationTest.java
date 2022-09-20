package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * create_trace.sh 2641 921 "2022-05-12 12:40:00" "2022-05-10 13:15:00"
 */
public class PredictionAccuracy921_2641IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    public PredictionAccuracy921_2641IntegrationTest() {
        super (createTraceConfig("921-2641", "America/Chicago"));
    }
    @Test
    public void testPredictions() {
        super.testPredictions();
    }
}
