package org.transitime.playback;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.BatchCsvAvlFeedModule;
import org.transitime.config.ConfigFileReader;
import org.transitime.configData.AgencyConfig;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.VehicleEvent;
import org.transitime.gtfs.GtfsData;
import org.transitime.gtfs.TitleFormatter;

/*
 * Note that MS also has a playback module. It's a little different. Reads in AVLs from the database.
 */


public class PlaybackModule {
	
	private static final Logger logger = LoggerFactory.getLogger(PlaybackModule.class);

	private static final String gtfsDirectoryName = "/Users/sjacobs/work/predictions-stuff/39"; //"src/main/resources/wmata_gtfs";
	private static final String avlReportsCsv = "/Users/sjacobs/work/predictions-stuff/avl/AvlReports_4-6-16_2807.csv";
	private static final String transitimeConfigFile = "src/main/resources/transiTimeConfigHsql.xml";
	
	private static Session session;
	
	public static void main(String[] args) {

        System.setProperty("transitime.avl.csvAvlFeedFileName", avlReportsCsv);
		System.setProperty("transitime.configFiles", transitimeConfigFile);
		System.setProperty("transitime.core.agencyId", "1");
		
		ConfigFileReader.processConfig();
		
		System.out.println("Adding GTFS to database...");
		setupGtfs();
		System.out.println("Done with GTFS.");

		System.out.println("adding avls");
		// Core is created on first access
		new BatchCsvAvlFeedModule("1").run(); 
		System.out.println("done");

		session = HibernateUtils.getSession();
		
		// Prediction accuracy post process. Can't use the module because
		// we're not reading in in real time.
		// This may not be performant for larger samples.
		System.out.println("add predictionaccuracy");
		addPredictionAccuracy();
		session.close();

		session = HibernateUtils.getSession();
		getResults(1800); // within 30 min
		getResults(300); // 5 min
		//getResults(60); // 1 min
		
		statError();
		logEvents();
	
		session.close();
		System.exit(0); // threads somewhere. maybe in Core.		
		
	}

	private static void statError() {
		Query query = session.getNamedQuery("scaled_prediction_error");
		
		List<Object[]> results = (List<Object[]>) query.list();
		
		double total = 0;
		double avgAccuracy = 0, avgHorizon = 0;
			
		for (Object[] a : results) {
			double acc = ((Number) a[0]).doubleValue();
			double horizon = ((Number) a[1]).doubleValue();
			avgAccuracy += Math.abs(acc);
			avgHorizon += horizon;
			total += Math.abs(acc) / Math.sqrt(horizon);
		}
		
		avgAccuracy /= results.size();
		avgHorizon /= results.size();
		
		String stmt = String.format("avg accuracy: %g, avg horizon: %g, scaled error %g", avgAccuracy/1000, avgHorizon/1000, total/1000);
		System.out.println(stmt);
		
	}
	
	private static void getResults(int maxPredLength) {
		Query query = session.getNamedQuery("get_prediction_accuracy_within_length");
		query.setParameter("maxPredLength", maxPredLength);
		
		// Integer if using HSQL server, BigInteger if using HSQL in-process.
		List<Number> results = query.list();

		int n = results.size();
		
		if (n == 0)
			return;
		
		int	min = results.get(0).intValue(),
				max = results.get(n-1).intValue(),
				median = results.get(n/2).intValue(),
				q1 = results.get(n/4).intValue(),
				q3 = results.get(3*n/4).intValue();
		
		int avg = 0;
		int absAvg = 0;
		float nWithinMin = 0;
		double rmse = 0;
		for (Number r : results) {
			int x = r.intValue();
			avg += x;
			absAvg += Math.abs(x);
			nWithinMin += (Math.abs(x) < 60) ? 1.0 : 0.0;
			rmse += x*x;
		}
		avg /= n;
		absAvg /= n;
		rmse = Math.sqrt(rmse)/n;
		
		System.out.printf("Results within prediction length %d min: [%d, %d, %d, %d, %d] (mean %d) (abs mean: %d) (within min: %g) (rmse: %g) \n", 
				maxPredLength/60, min, q1, median, q3, max, avg, absAvg, nWithinMin*100/(float)n, rmse);
	}
	
	private static void logEvents() {
		List<VehicleEvent> events = session.createCriteria(VehicleEvent.class).list();
		for (VehicleEvent event : events)
			System.out.println(event);
	}
	
	
	private static void addPredictionAccuracy() {
		int n = session.getNamedQuery("create_prediction_accuracy").executeUpdate();
		System.out.println("updated " + n + " records");
	}

	// Adapted from GtfsFileProcessor. May need to add setTimezone in the future,
	// but actually maybe it doesn't matter for playback.
	private static void setupGtfs() {
		TitleFormatter titleFormatter = new TitleFormatter(null, true);
		GtfsData gtfsData = new GtfsData(1, null, null, true, AgencyConfig.getAgencyId(), gtfsDirectoryName, null, 0.0, 
				50.0, 0.0, 6, 97.0, 1000.0, false, titleFormatter);
		gtfsData.processData();
	}
}
