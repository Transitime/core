package org.transitclock.integration_tests.prediction;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.integration_tests.playback.ReplayResults;
import org.transitclock.integration_tests.playback.ReplayService;

import java.util.List;

/**
 * This integration test builds an entirely new transitime DB from GTFS files, prepares the DB for the app to run
 * imports a CSV file of avl test data, waits for predictions to be generated, then checks the output against
 * a csv file of expected prediction values. 
 * 
 * For the test to succeed, prediction quality must improve overall. Up to 5% of individual predictions 
 * (by stop and AVL time of creation) can be worse.
 *
 */
public abstract class AbstractPredictionAccuracyIntegrationTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPredictionAccuracyIntegrationTest.class);

	private static final String DEFAULT_CONFIG_FILE = "classpath:transitclockConfigHsql.xml";
    private ReplayService rs;
	private TraceConfig config;
	public AbstractPredictionAccuracyIntegrationTest(String id, String outputDirectory, String gtfs, String avl,
													 String predictionsCsv, String tz, String description) {
		TraceConfig pc = new TraceConfig();
		pc.setId(id);
		pc.setGtfsDirectoryName(gtfs);
		pc.setAvlReportsCsv(avl);
		pc.setOutputDirectory(outputDirectory);
		pc.setPredictionCsv(predictionsCsv);
		pc.setOutputDirectory(outputDirectory);
		pc.setTz(tz);
		pc.setArrivalDepartureCsv(null);
		pc.setDescription(description);
		this.config = pc;

	}
	public AbstractPredictionAccuracyIntegrationTest(TraceConfig config) {
		this.config = config;
	}

	public static TraceConfig createApcTraceConfig(String id, String tz, String description) {
		return createTraceConfig(id, tz, description, true, true, false);
	}
	public static TraceConfig createApcTraceConfig(String id, String tz, String description, boolean includePredictionCsv, boolean addConfigFile) {
		return createTraceConfig(id, tz, description, includePredictionCsv, true, addConfigFile);
	}
	public static TraceConfig createTraceConfig(String id, String tz, String description) {
		return createTraceConfig(id, tz, description, true, true, false);
	}
	public static TraceConfig createTraceConfig(String id, String tz, String description,
												boolean includePredictionsCsv, boolean includeApcCsv,
												boolean addConfigFile) {
		TraceConfig config = new TraceConfig();
		config.setId(id);
		config.setGtfsDirectoryName("src/test/resources/tests/" + id + "/gtfs");
		config.setAvlReportsCsv("src/test/resources/tests/" + id + "/avl.csv");
		if (includePredictionsCsv) {
			config.setPredictionCsv("src/test/resources/tests/" + id + "/pred.csv");
		}
		if (includeApcCsv) {
			config.setApcCsv("src/test/resources/tests/" + id + "/apc.csv");
		}
		config.setArrivalDepartureCsv("src/test/resources/tests/" + id + "/history.csv");
		config.setOutputDirectory(getOutputDirectory() + id);
		config.setTz(tz);
		if (addConfigFile) {
			config.setConfigFileNames("src/test/resources/config/" + id + ".xml"
					+ ";" + DEFAULT_CONFIG_FILE);
		}
		config.setDescription(description);
		return config;
	}

	private static String getOutputDirectory() {
		// expecting ~/transitime/transitclockIntegration/ to which we add the classes dir
		// so the output of the integration tests will end up in the jar file
		String property = System.getProperty("user.dir");
		if (property == null)
			return "/tmp/output/";
		if (!property.endsWith("/"))
			property = property + "/";
		if (property.endsWith("transitclockIntegration/"))
			property = property + "target/classes/reports/";
		System.out.println("using outputdirectory of '" + property + "'");
		logger.info("using outputdirectory of '" + property + "'");
		return property;
	}

	@Override
    public void setUp() {
		rs = new ReplayService(config.getId(), config.getOutputDirectory());

		List<ArrivalDeparture> arrivalDepartures = rs.run(config);

		if (config.getPredictionCsv() != null)
			rs.loadPastPredictions(config.getPredictionCsv());
		rs.accumulate(arrivalDepartures);

    }

    public void testPredictions() {
		// TODO
    	ReplayResults results = rs.compare();
		// New method is bad if...

		// there are fewer new predictions than old predictions
//		assertTrue(results.getOldTotalPreds() <= results.getNewTotalPreds());

		// total scaled error did not improve
//		assertTrue(results.getNewTotalError() <= results.getOldTotalError());

		// old is more accurate in over 5% of cases
//		assertTrue(((double) results.getOldBetter()/results.getBothTotalPreds()) <= 0.5);

	}

}
