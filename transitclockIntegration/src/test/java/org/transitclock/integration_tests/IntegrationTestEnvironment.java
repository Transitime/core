package org.transitclock.integration_tests;

/**
 * Represents the configuration needed to invoke an integration test.
 */
public class IntegrationTestEnvironment {
    private String name;
    private String avl;
    private String apc;
    private String gtfs;
    private String history;
    private String predictions;
    private String config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvl() {
        return avl;
    }

    public void setAvl(String avl) {
        this.avl = avl;
    }

    public String getApc() {
        return apc;
    }

    public void setApc(String apc) {
        this.apc = apc;
    }

    public String getGtfs() {
        return gtfs;
    }

    public void setGtfs(String gtfs) {
        this.gtfs = gtfs;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getPredictions() {
        return predictions;
    }

    public void setPredictions(String predictions) {
        this.predictions = predictions;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
