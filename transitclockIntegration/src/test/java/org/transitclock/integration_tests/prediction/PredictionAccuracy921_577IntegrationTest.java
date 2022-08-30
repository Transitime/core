package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * create_trace.sh 2391 921 "2022-05-10 08:36:00" "2022-05-10 09:22:00"
 */
public class PredictionAccuracy921_577IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {

    public PredictionAccuracy921_577IntegrationTest() {
        super (createTraceConfig("921-577", "America/Chicago"));
    }

    @Test
    public void testPredictions() {
        super.testPredictions();
    }

}
