package org.transitclock.integration_tests.prediction;

import org.junit.Test;

import java.io.File;

/**
 * Integration Test designed to take its configuration from environment variables.
 * Can be called directly via:
 * mvn test -Dtest=org.transitclock.integration_tests.prediction.EnvironmentBasedPredictionAccuracyIntegrationTestImpl -Dit.name=baseline -Dit.avl=avl.csv -Dit.gtfs=gtfs -Dit.history=history.csv -Dit.apc=apc.csv
 */
public class EnvironmentBasedPredictionAccuracyIntegrationTestImpl
        extends AbstractPredictionAccuracyIntegrationTest {

    public EnvironmentBasedPredictionAccuracyIntegrationTestImpl() {
        TraceConfig config = new TraceConfig();
        try {
            config.setId(getRequiredProperty("it.name"));
        } catch (IllegalStateException ise) {
            setConfig(null);
        }
        String prefix = "src/test/resources/tests/" + config.getId() + "/";
        config.setAvlReportsCsv(getRequirePropertyRelativeTo(prefix, "it.avl"));
        config.setApcCsv(getOptionalPropertyRelativeTo(prefix, "it.apc"));
        config.setGtfsDirectoryName(getRequirePropertyRelativeTo(prefix, "it.gtfs"));
        config.setArrivalDepartureCsv(getOptionalPropertyRelativeTo(prefix, "it.history"));
        config.setPredictionCsv(getOptionalPropertyRelativeTo(prefix, "it.predictions"));
        config.setConfigFileNames(getOptionalPropertyRelativeTo(prefix, "it.config"));
        config.setOutputDirectory(getOutputDirectory()
                + getRequiredProperty("it.runid")
                + File.separator
                + config.getId());
        setConfig(config);
    }

    @Test
    public void testPredictions() {
        if (config == null) {
            logger.info("test not configured, nothing to do");
            return;
        }
        logger.info("running testPredictions for {}", config.getId());
        try {
            super.testPredictions();
            logger.info("success for {}", config.getId());
            logger.info("Output files: {}", this.getGeneratedFiles());
        } finally {
            logger.info("exiting testPredictions for {}", config.getId());
        }
    }

    private String getOptionalPropertyRelativeTo(String prefix, String key) {
        String value = getOptionalProperty(key);
        if (value != null) return prefix + value;
        return null;
    }

    private String getRequirePropertyRelativeTo(String prefix, String key) {
        return prefix + getRequiredProperty(key);
    }


    private String getOptionalProperty(String property) {
        return System.getProperty(property);
    }

    private String getRequiredProperty(String property) {
        String value = System.getProperty(property);
        if (value == null) throw new IllegalStateException("missing require property '" + property
         + ";");
        return value;
    }

}
