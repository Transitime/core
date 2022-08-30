package org.transitclock.integration_tests.prediction;

import org.transitclock.integration_tests.playback.PlaybackConfig;

/**
 * State configuration for running an Integration Test Trace.
 */
public class TraceConfig extends PlaybackConfig {

    private String id;
    private String predictionCsv;
    private String outputDirectory;
    private String tz;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPredictionCsv() {
        return predictionCsv;
    }

    public void setPredictionCsv(String predictionCsv) {
        this.predictionCsv = predictionCsv;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String getTz() {
        return tz;
    }

    @Override
    public void setTz(String tz) {
        this.tz = tz;
    }

}
