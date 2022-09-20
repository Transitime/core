package org.transitclock.integration_tests.prediction;

import org.junit.Test;

/**
 * Test of Trace with APC data.  APCStopTimeGenerator will not succeed until we add real-time headway data.
 * create_trace.sh 2391 921 "2022-05-12 12:30:00" "2022-05-12 13:05:00"
 */
public class PredictionAccuracy921_2391IntegrationTest extends AbstractPredictionAccuracyIntegrationTest {
    public PredictionAccuracy921_2391IntegrationTest() {
        super(createApcTraceConfig("921-2391", "America/Chicago", false, true));
    }
    @Test
    public void testPredictions() { super.testPredictions(); }

}
