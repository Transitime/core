package org.transitclock.integration_tests.playback;

/**
 * State configuration for running a Playback trace.
 */
public class PlaybackConfig {
    private String gtfsDirectoryName;
    private String avlReportsCsv;
    private String arrivalDepartureCsv;
    private String configFileNames;
    private String apcCsv;
    private boolean addPredictionAccuracy;
    private boolean log;
    private String tz;

    public String getGtfsDirectoryName() {
        return gtfsDirectoryName;
    }

    public void setGtfsDirectoryName(String gtfsDirectoryName) {
        this.gtfsDirectoryName = gtfsDirectoryName;
    }

    public String getAvlReportsCsv() {
        return avlReportsCsv;
    }

    public void setAvlReportsCsv(String avlReportsCsv) {
        this.avlReportsCsv = avlReportsCsv;
    }

    public String getArrivalDepartureCsv() {
        return arrivalDepartureCsv;
    }

    public void setArrivalDepartureCsv(String arrivalDepartureCsv) {
        this.arrivalDepartureCsv = arrivalDepartureCsv;
    }

    public String getApcCsv() {
        return apcCsv;
    }

    public void setApcCsv(String apcCsv) {
        this.apcCsv = apcCsv;
    }

    public boolean isAddPredictionAccuracy() {
        return addPredictionAccuracy;
    }

    public void setAddPredictionAccuracy(boolean addPredictionAccuracy) {
        this.addPredictionAccuracy = addPredictionAccuracy;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String tz) {
        this.tz = tz;
    }

    public String getConfigFileNames() {
        return configFileNames;
    }

    public void setConfigFileNames(String configFileNames) {
        this.configFileNames = configFileNames;
    }
}
