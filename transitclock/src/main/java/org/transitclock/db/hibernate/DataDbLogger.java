/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.db.hibernate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.CoreConfig;
import org.transitclock.configData.DbSetupConfig;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Headway;
import org.transitclock.db.structs.Match;
import org.transitclock.db.structs.MonitoringEvent;
import org.transitclock.db.structs.Prediction;
import org.transitclock.db.structs.PredictionAccuracy;
import org.transitclock.db.structs.PredictionEvent;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.TrafficSensorData;
import org.transitclock.db.structs.VehicleConfig;
import org.transitclock.db.structs.VehicleEvent;
import org.transitclock.db.structs.VehicleState;

/**
 * DataDbLogger is for storing to the db a stream of data objects. It is intended
 * for example for storing AVL reports, vehicle matches, arrivals, departures, etc.
 * There can be quite a large volume of this type of data.
 * 
 * The database might not always be available. It could be vacuumed, restarted, 
 * moved, etc. When this happens don't want the data to be lost and don't want
 * to tie up the core predictor. Therefore a queue is used to log objects.
 * This makes the application far more robust with respect to database issues.
 * The application simply calls add(Object o) to add the object to be stored
 * to the queue. 
 * 
 * A separate thread is used to read from the queue and write the data to the 
 * database. If the queue starts filling up then error messages are e-mailed
 * to users alerting them that there is a problem. E-mail messages are also
 * sent out when the queue level is going down again.
 * 
 * A goal with this class was to make the writing to the database is 
 * efficient as possible. Therefore the objects are written in batches.
 * This reduces network traffic as well as database load. But this did
 * make handling exceptions more complicated. If there is an exception
 * with a batch then each item is individually written so that don't
 * lose any data.
 * 
 * When in playback mode then don't want to store the data because it would
 * interfere with data stored when the application was run in real time. 
 * Therefore when running in playback mode set shouldStoreToDb to true
 * when calling getDataDbLogger().
 * 
 * @author SkiBu Smith
 *
 */
public class DataDbLogger {
	

  // This is a singleton class that only returns a single object per agencyId.
  private static Map<String, DataDbLogger> dataDbLoggerMap = 
      new HashMap<String, DataDbLogger>(1);

  private DbQueue<ArrivalDeparture> arrivalDepartureQueue;
  private DbQueue<AvlReport> avlReportQueue;
  private DbQueue<VehicleConfig> vehicleConfigQueue;
  private DbQueue<Prediction> predictionQueue;
  private DbQueue<Match> matchQueue;
  private DbQueue<PredictionAccuracy> predictionAccuracyQueue;
  private DbQueue<MonitoringEvent> monitoringEventQueue;
  private DbQueue<VehicleEvent> vehicleEventQueue;
  private DbQueue<PredictionEvent> predictionEventQueue;
  private DbQueue<VehicleState> vehicleStateQueue;
  private DbQueue<TrafficSensorData> trafficSensorDataQueue;
  private DbQueue<Headway> headwayQueue;
  private DbQueue<RunTimesForRoutes> runTimesForRoutesQueue;
  private DbQueue<Object> genericQueue;
	
	private static final int QUEUE_CAPACITY = 5000000;
	
	// The queue capacity levels when an error message should be e-mailed out. 
	// The max value should be 1.0. 
	private final double levels[] = { 0.5, 0.8, 1.00 };
	
	// The queue that objects to be stored are placed in
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(QUEUE_CAPACITY);
	
	// When running in playback mode where getting AVLReports from database
	// instead of from an AVL feed, then debugging and don't want to store
	// derived data into the database because that would interfere with the
	// derived data that was already stored in real time. For that situation
	// shouldStoreToDb should be set to false.
	private final boolean shouldStoreToDb;
	
	// Used by add(). If queue filling up to 25% and shouldPauseToReduceQueue is
	// true then will pause the calling thread for a few seconds so that more
	// objects can be written out and not have the queue fill up.
	private final boolean shouldPauseToReduceQueue;
	
	// For keeping track of index into levels, which level of capacity of
	// queue being used. When level changes then an e-mail is sent out warning
	// the operators.
	private double indexOfLevelWhenMessageLogged = 0;
	
	// For keeping track of maximum capacity of queue that was used. 
	// Used for logging when queue use is going down.
	private double maxQueueLevel = 0.0;
	
	// So can access agencyId for logging messages
	private String agencyId;

	// keep track of primary key values to reduce database duplicate exceptions
	private Map<String, String> vehicleToPrimayKeyMap = new HashMap<>();

	private static final Logger logger =
			LoggerFactory.getLogger(DataDbLogger.class);

	/********************** Member Functions **************************/

	/**
	 * Factory method. Returns the singleton db logger for the specified
	 * agencyId.
	 * 
	 * @param agencyId
	 *            Id of database to be written to
	 * @param shouldStoreToDb
	 *            Specifies whether data should actually be written to db. If in
	 *            playback mode and shouldn't write data to db then set to
	 *            false.
	 * @param shouldPauseToReduceQueue
	 *            Specifies if should pause the thread calling add() if the
	 *            queue is filling up. Useful for when in batch mode and dumping
	 *            a whole bunch of data to the db really quickly.
	 * @return The DataDbLogger for the specified agencyId
	 */
	public static DataDbLogger getDataDbLogger(String agencyId,
			boolean shouldStoreToDb, boolean shouldPauseToReduceQueue) {
		synchronized (dataDbLoggerMap) {
			DataDbLogger logger = dataDbLoggerMap.get(agencyId);
			if (logger == null) {
				logger = new DataDbLogger(agencyId, shouldStoreToDb,
						shouldPauseToReduceQueue);
				dataDbLoggerMap.put(agencyId, logger);
			}
			return logger;
		}
	}
	
	/**
	 * Constructor. Private so that factory method getDataDbLogger() has to be
	 * used. Starts up separate thread that actually reads from queue and stores
	 * the data.
	 * 
	 * @param agencyId
	 *            Id of database to be written to
	 * @param shouldStoreToDb
	 *            Specifies whether data should actually be written to db. If in
	 *            playback mode and shouldn't write data to db then set to
	 *            false.
	 * @param shouldPauseToReduceQueue
	 *            Specifies if should pause the thread calling add() if the
	 *            queue is filling up. Useful for when in batch mode and dumping
	 *            a whole bunch of data to the db really quickly.
	 */
	private DataDbLogger(String agencyId, boolean shouldStoreToDb, 
			boolean shouldPauseToReduceQueue) {
		this.agencyId = agencyId;
		this.shouldStoreToDb = shouldStoreToDb;
		this.shouldPauseToReduceQueue = shouldPauseToReduceQueue;
	  arrivalDepartureQueue = new DbQueue<ArrivalDeparture>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, ArrivalDeparture.class.getSimpleName());
	  avlReportQueue = new DbQueue<AvlReport>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, AvlReport.class.getSimpleName());
	  vehicleConfigQueue = new DbQueue<VehicleConfig>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, VehicleConfig.class.getSimpleName());
		predictionQueue = new DbQueue<Prediction>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Prediction.class.getSimpleName());
	  matchQueue = new DbQueue<Match>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Match.class.getSimpleName());
	  predictionAccuracyQueue = new DbQueue<PredictionAccuracy>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, PredictionAccuracy.class.getSimpleName());
	  monitoringEventQueue = new DbQueue<MonitoringEvent>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, MonitoringEvent.class.getSimpleName());
	  vehicleEventQueue = new DbQueue<VehicleEvent>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, VehicleEvent.class.getSimpleName());
		predictionEventQueue = new DbQueue<PredictionEvent>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, PredictionEvent.class.getSimpleName());
	  vehicleStateQueue = new DbQueue<VehicleState>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, VehicleState.class.getSimpleName());
	  trafficSensorDataQueue = new DbQueue<TrafficSensorData>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, TrafficSensorData.class.getSimpleName());
	  headwayQueue = new DbQueue<Headway>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Headway.class.getSimpleName());
	  runTimesForRoutesQueue = new DbQueue<RunTimesForRoutes>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, RunTimesForRoutes.class.getSimpleName());
	  genericQueue = new DbQueue<Object>(agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Object.class.getSimpleName());
		
	}
	
	public boolean add(ArrivalDeparture ad) {
		String key = "ad_" + ad.getVehicleId();
		String hash = vehicleToPrimayKeyMap.get(key);
		if (hash != null && hash.equals(hashArrivalDeparture(ad))) {
			// we already have this value, prevent sql exception
			return false;
		}
		vehicleToPrimayKeyMap.put(key, hash);
		return arrivalDepartureQueue.add(ad);
	}
	public boolean add(AvlReport ar) {
		String key = "ar_" + ar.getVehicleId();
		String hash = vehicleToPrimayKeyMap.get(key);
		if (hash != null && hash.equals(hashAvl(ar))) {
			// we already have this value, prevent sql exception
			return false;
		}
		vehicleToPrimayKeyMap.put(key, hash);
		return avlReportQueue.add(ar);
	}
	public boolean add(VehicleConfig vc) {
	  return vehicleConfigQueue.add(vc);
	}
	public boolean add(Prediction p) {
	  return predictionQueue.add(p);
	}
    public boolean add(Match m) {
	  return matchQueue.add(m);
	}
    public boolean add(PredictionAccuracy pa) {
    	if (pa != null && pa.getArrivalDepartureTime() != null) {
			return predictionAccuracyQueue.add(pa);
		}
    	// reject null arrival/departure time
    	return false;
    }
    public boolean add(MonitoringEvent me) {
      return monitoringEventQueue.add(me);
    }
    public boolean add(VehicleEvent ve) {
      return vehicleEventQueue.add(ve);
    }
    public boolean add(PredictionEvent pe) {
		if (CoreConfig.storePredictionEventsInDatabase()) {
			return predictionEventQueue.add(pe);
		}
		return false;
		}
    public boolean add(VehicleState vs) {
		String key = "vs_" + vs.getVehicleId();
		String hash = vehicleToPrimayKeyMap.get(key);
		if (hash != null && hash.equals(hashVehicleState(vs))) {
			// we already have this value, prevent sql exception
			return false;
		}
		vehicleToPrimayKeyMap.put(key, hash);
		return vehicleStateQueue.add(vs);
	}

	public boolean add(TrafficSensorData data) {
		return trafficSensorDataQueue.add(data);
	}

	public boolean add(Headway data) {
		return headwayQueue.add(data);
	}

	public boolean add(RunTimesForRoutes data) {
		return runTimesForRoutesQueue.add(data);
	}


	/**
	 * Determines set of class names in the queue. Useful for logging
	 * error message when queue getting filled up so know what kind of
	 * objects are backing the system up.
	 * 
	 * @return Map of class names and their count of the objects in the queue
	 */
	private Map<String, Integer> getClassNamesInQueue() {
		Map<String, Integer> classNamesMap = new HashMap<String, Integer>();
		for (Object o : queue) {
			String className = o.getClass().getName();
			Integer count = classNamesMap.get(className);
			if (count == null) {
				count = new Integer(0);
				classNamesMap.put(className, count);
			}
			++count;
		}
		return classNamesMap;
	}
	
	/**
	 * Adds an object to be saved in the database to the queue. If queue is
	 * getting filled up then an e-mail will be sent out indicating there is a
	 * problem. The queue levels at which an e-mail is sent out is specified by
	 * levels. If queue has reached capacity then an error message is logged.
	 * 
	 * @param o
	 *            The object that should be logged to the database
	 * @return True if OK (object added to queue or logging disabled). False if
	 *         queue was full.
	 */
	public boolean add(Object o) {	
	  // this is now a catch-all -- most objects have an argument overriden method
	  return genericQueue.add(o);
	}
	

	// as a summary of overall queue behaviour, return the highest queue level
	public double queueLevel() {
		Double[] queueLevelsArray = {
						arrivalDepartureQueue.queueLevel(),
						avlReportQueue.queueLevel(),
						vehicleConfigQueue.queueLevel(),
						predictionQueue.queueLevel(),
						matchQueue.queueLevel(),
						predictionAccuracyQueue.queueLevel(),
						monitoringEventQueue.queueLevel(),
						vehicleEventQueue.queueLevel(),
						predictionEventQueue.queueLevel(),
						vehicleStateQueue.queueLevel(),
						trafficSensorDataQueue.queueLevel(),
						headwayQueue.queueLevel(),
						runTimesForRoutesQueue.queueLevel(),
						genericQueue.queueLevel()
		};

		List<Double> levels = Arrays.asList(queueLevelsArray);
		Collections.sort(levels);
		return levels.get(levels.size()-1);
	}
	
	// as a summary of queue sizes, return the largest queue size
	public int queueSize() {
		Integer[] sizesArray = {
						arrivalDepartureQueue.queueSize(),
						avlReportQueue.queueSize(),
						vehicleConfigQueue.queueSize(),
						predictionQueue.queueSize(),
						matchQueue.queueSize(),
						predictionAccuracyQueue.queueSize(),
						monitoringEventQueue.queueSize(),
						vehicleEventQueue.queueSize(),
						predictionEventQueue.queueSize(),
						vehicleStateQueue.queueSize(),
						trafficSensorDataQueue.queueSize(),
						headwayQueue.queueSize(),
						runTimesForRoutesQueue.queueSize(),
						genericQueue.queueSize()
		};

		List<Integer> sizes = Arrays.asList(sizesArray);
		Collections.sort(sizes);
		return sizes.get(sizes.size()-1);
	}

	private String hashAvl(AvlReport ar) {
		// primary keys minus vehicleId
		DateFormat simple =
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simple.format(ar.getDate());
	}

	private String hashArrivalDeparture(ArrivalDeparture ad) {
		// primary keys minus vehicleId
		return
						ad.getTripId() + "_"
										+ ad.getTime() + "_"
										+ ad.getStopId() + "_"
										+ ad.isArrival() + "_"
										+ ad.getGtfsStopSequence();
	}

	private String hashVehicleState(VehicleState vs) {
		// primary keys minus vehicleId
		DateFormat simple =
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simple.format(vs.getAvlTime());

	}
	
	/**
	 * Just for doing some testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DataDbLogger logger = getDataDbLogger("test", false, false);

		long initialTime = (System.currentTimeMillis()  / 1000) * 1000;

		for (int i=0; i<25; ++i)
			logger.add(new AvlReport("test", initialTime + i, 1.23, 4.56, null));
		
		// This one should cause constraint problem with the second batch.
		// Need to not retry for such an exception
		logger.add(new AvlReport("test", initialTime, 1.23, 4.56, null)); 
		
		for (int i=DbSetupConfig.getBatchSize(); i<2*DbSetupConfig.getBatchSize();++i)
			logger.add(new AvlReport("test", initialTime+i, 1.23, 4.56, null));

	}

}
