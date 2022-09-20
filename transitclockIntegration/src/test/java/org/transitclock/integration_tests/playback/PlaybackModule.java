package org.transitclock.integration_tests.playback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.applications.UpdateTravelTimes;
import org.transitclock.avl.BatchCsvApcModule;
import org.transitclock.avl.BatchCsvArrivalDepartureModule;
import org.transitclock.avl.BatchCsvAvlFeedModule;
import org.transitclock.avl.BatchCsvAvlFeedModule.AvlPostProcessor;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitclock.utils.DateRange;
import org.transitclock.utils.Time;


/*
 * MS also has a playback module. It's a little different. Reads in AVLs from the database.
 */
// Stateful tables:  arrivalsdepartures, avlreports, matches, vehicleevents, vehiclestates

public class PlaybackModule {
	
	private static final Logger logger = LoggerFactory.getLogger(PlaybackModule.class);

	private static String defaultGtfsDirectoryName = "src/main/resources/wmata_gtfs"; 
	private static String defaultAvlReportsCsv = "src/main/resources/avl/03142016_SE-04.csv";
	private static final String defaultTransitimeConfigFile = "classpath:transitclockConfigHsql.xml";

	private static final String agencyId = "1";
	
	// Take defaults from GtfsFileProcessor.java
	private static final double pathOffsetDistance = 0.0;					
	private static final double maxStopToPathDistance =  60.0;
	private static final double maxDistanceForEliminatingVertices = 3.0;
	private static final int defaultWaitTimeAtStopMsec = 10 * Time.MS_PER_SEC;
	private static final double maxSpeedKph = 97.0;
	private static final double maxTravelTimeSegmentLength = 200.0;

	private static Session session;
	
	public static void main(String[] args) {
		
		String gtfsDirectoryName = defaultGtfsDirectoryName;
		String avlReportsCsv = defaultAvlReportsCsv;
		
		if (args.length >= 2) {
			gtfsDirectoryName = args[0];
			avlReportsCsv = args[1];
		}
		
        runTrace(gtfsDirectoryName, avlReportsCsv, null,true, true, null);
		
		session = HibernateUtils.getSession();
		// Look at 5 min intervals
		for (int i = 1800; i > 0; i -= 300)
			getResults(i-300, i);
		
		statError();
	
		session.close();
		
		//updateTravelTimes();
		checkPredictionsCache();
		
		System.exit(0); // threads somewhere. maybe in Core.		
		
	}
	
	public static DateRange runTrace(PlaybackConfig state, AvlPostProcessor processor) {
		DateRange avlRange = null;
		int configRev = 1;
		if (state.getTz() != null) {
			TimeZone.setDefault(TimeZone.getTimeZone(state.getTz()));
		}
		String configFiles = state.getConfigFileNames();
		if (configFiles == null) configFiles = defaultTransitimeConfigFile;

		populateSystemProperties(state);
		runConfig(state.getAvlReportsCsv(), configFiles, agencyId);

		if (state.isLog())
			System.out.println("Adding GTFS to database... " + state.getGtfsDirectoryName());
		
		setupGtfs(state.getGtfsDirectoryName());
		
		if (state.isLog())
			System.out.println("Done with GTFS. Adding AVLs.");

		session = HibernateUtils.getSession();
		if (state.getArrivalDepartureCsv() != null) {

			int size = session.createCriteria(ArrivalDeparture.class).list().size();
			logger.info("pre load has {} ADs", size);

			BatchCsvArrivalDepartureModule arrivalDepartureModule = new BatchCsvArrivalDepartureModule(agencyId, configRev, session);
			arrivalDepartureModule.run();
			if (arrivalDepartureModule.getArrivalDepartures().isEmpty()) {
				throw new RuntimeException("History was configured with " + state.getArrivalDepartureCsv()
				+ " but no records were available to save");
			}

			int expectedSize = arrivalDepartureModule.getArrivalDepartures().size();
			logger.info("expected {} ADs", expectedSize);
			size = session.createCriteria(ArrivalDeparture.class).list().size();
			logger.info("post load has {} ADs", size);
			if (size != expectedSize) {
				throw new RuntimeException("Expected " + expectedSize + " A/Ds but loaded"
				+ size + " entries");
			}

		}

		if (state.getApcCsv() != null) {
			logger.info("loading APC..");
			BatchCsvApcModule apcModule = new BatchCsvApcModule(agencyId, configRev, session);
			apcModule.setCsvFileName(state.getApcCsv());
			List<ApcReport> reports = apcModule.run();
			logger.info("loaded {} APCReports", reports.size());
		} else {
			logger.info("not loading APC data.");
		}



		try {
			Core.getInstance().populateCaches();
		} catch (Exception e) {
			logger.error(" populate caches failed.", e);
		}

		// Core is created on first access
		BatchCsvAvlFeedModule avlModule = new BatchCsvAvlFeedModule(agencyId);
		if (processor != null)
			avlModule.setAvlPostProcessor(processor);
		Date readTimeStart = new Date();
		avlModule.run();
		avlRange = avlModule.getAvlRange();
		Date readTimeEnd = new Date();

		if (state.isLog())
			System.out.println("done");
		
		if (!state.isAddPredictionAccuracy())
			return avlRange;
		
		// Prediction accuracy post process. Can't use the module because
		// we're not reading in real time.
		// This may not be performant for larger samples.
		session = HibernateUtils.getSession();
		
		if (state.isLog())
			System.out.println("add predictionaccuracy");
		
		addPredictionAccuracy(readTimeStart, readTimeEnd);
		session.close();
		
		if (state.isLog())
			System.out.println("Update travel times");
		UpdateTravelTimes.manageSessionAndProcessTravelTimes(agencyId, null, new Date(0), new Date(Long.MAX_VALUE));
		if (state.isLog())
			System.out.println("Done");
		return avlRange;
	}

	private static void populateSystemProperties(PlaybackConfig state) {
		if (state.getArrivalDepartureCsv() != null) {
			System.setProperty("transitclock.core.cacheReloadStartTimeStr", "2010-01-01 00:00:00");
			System.setProperty("transitclock.core.cacheReloadEndTimeStr", "2030-01-01 00:00:00");
			System.setProperty("transitclock.avl.csvArrivalDepartureFeedFileName", state.getArrivalDepartureCsv());
		}
	}

	public static void runConfig(String avlReportsCsv, String transitimeConfigFile, String agencyId) {
		System.setProperty("transitclock.avl.csvAvlFeedFileName", avlReportsCsv);
		System.setProperty("transitclock.configFiles", transitimeConfigFile);
		System.setProperty("transitclock.core.agencyId", agencyId);
		// set time to the earliest example of tests so all service is future
		// and therefore not ignored as expired
		System.setProperty("transitclock.gtfs.systemTime", "2016-01-01 00:00:00");

		ConfigFileReader.processConfig();
	}

	public static DateRange runTrace(String gtfsDirectoryName, String avlReportsCsv, String arrivalDepartureFileName,
									 boolean addPredictionAccuracy, boolean log, String tz) {
		PlaybackConfig config = new PlaybackConfig();
		config.setGtfsDirectoryName(gtfsDirectoryName);
		config.setAvlReportsCsv(avlReportsCsv);
		config.setArrivalDepartureCsv(arrivalDepartureFileName);

		config.setAddPredictionAccuracy(addPredictionAccuracy);
		config.setLog(log);
		config.setTz(tz);

		return runTrace(config, null);
	}
	
	public static DateRange runTrace(String gtfsDirectoryName, String avlReportsCsv, String arrivalDepartureFileName,
									 String tz) {
		return runTrace(gtfsDirectoryName, avlReportsCsv, arrivalDepartureFileName, false, true,
				tz);
	}
	
	public static DateRange runTrace(String gtfsDirectoryName, String avlReportsCsv, String arrivalDepartureFileName,
									 AvlPostProcessor processor, String tz) {
		PlaybackConfig config = new PlaybackConfig();
		config.setGtfsDirectoryName(gtfsDirectoryName);
		config.setAvlReportsCsv(avlReportsCsv);
		config.setArrivalDepartureCsv(arrivalDepartureFileName);
		config.setTz(tz);
		// defaults
		config.setLog(false);
		config.setAddPredictionAccuracy(false);

		return runTrace(config, processor);
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
			total += Math.abs(acc) / horizon;
		}
		
		avgAccuracy /= results.size();
		avgHorizon /= results.size();
		total /= results.size();
		
		String stmt = String.format("avg accuracy: %g, avg horizon: %g, avg scaled error %g", avgAccuracy/1000, avgHorizon/1000, total);
		System.out.println(stmt);
		
	}
	
	private static void getResults(int minPredLength, int maxPredLength) {
		Query query = session.getNamedQuery("get_prediction_accuracy_within_length");
		query.setParameter("maxPredLength", maxPredLength);
		query.setParameter("minPredLength", minPredLength);
		
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
		
		System.out.printf("Results within prediction length %d-%d min: [%d, %d, %d, %d, %d] (mean %d) (abs mean: %d) (within min: %g) (rmse: %g) \n", 
				minPredLength/60, maxPredLength/60, min, q1, median, q3, max, avg, absAvg, nWithinMin*100/(float)n, rmse);
	}
	
	
	private static void addPredictionAccuracy(Date start, Date end) {
		int n = session.getNamedQuery("create_prediction_accuracy")
				.setParameter("start", start)
				.setParameter("end", end)
				.executeUpdate();
		System.out.println("updated " + n + " records");
	}

	// Adapted from GtfsFileProcessor. May need to add setTimezone in the future,
	// but actually maybe it doesn't matter for playback.
	private static void setupGtfs(String gtfsDirectoryName) {
		TitleFormatter titleFormatter = new TitleFormatter(null, true);
		boolean shouldStoreNewRevs = true, shouldDeleteRevs = false;
		GtfsData gtfsData = new GtfsData(1,
				null,
				null,
				shouldStoreNewRevs,
				shouldDeleteRevs,
				AgencyConfig.getAgencyId(),
				gtfsDirectoryName,
				null,
				pathOffsetDistance,
				maxStopToPathDistance,
				maxDistanceForEliminatingVertices,
				defaultWaitTimeAtStopMsec,
				maxSpeedKph,
				maxTravelTimeSegmentLength,
				false,
				titleFormatter/*,
				200.0,
				true*/);
		gtfsData.processData();
	}
	
	private static void updateTravelTimes() {
		session = HibernateUtils.getSession();
		Date beginTime = (Date) session.createCriteria(ArrivalDeparture.class)
			.setProjection(Projections.min("avlTime")).list().get(0);
		session.close();
		Date endTime = new Date(beginTime.getTime() + Time.MS_PER_DAY);
		System.out.println("Running update travel times from " + beginTime + " to " + endTime);
		UpdateTravelTimes.manageSessionAndProcessTravelTimes("1", null,
				beginTime, endTime);
	}
	
	private static void checkPredictionsCache() {
		// Clear predictions cache
		Core.getInstance().setSystemTime(System.currentTimeMillis());
		Core.getInstance().getTimeoutHandlerModule().handlePossibleTimeouts();
		// Any predictions left?
		List<IpcPredictionsForRouteStopDest> preds = PredictionDataCache.getInstance().getAllPredictions(Integer.MAX_VALUE, Long.MAX_VALUE);
		for (IpcPredictionsForRouteStopDest pred : preds) {
			System.out.println(pred.toString());
		}
		
	}


}
