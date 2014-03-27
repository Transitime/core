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
package org.transitime.db.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;
import org.transitime.logging.Markers;
import org.transitime.utils.Time;
import org.transitime.utils.threading.NamedThreadFactory;

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
 * Therefore when running in playback mode set doNotStoreToDb to true
 * when calling getDataDbLogger().
 * 
 * @author SkiBu Smith
 *
 */
public class DataDbLogger {
	
	// For when cannot connect to data the length of time in msec between retries
	private static final long TIME_BETWEEN_RETRIES = 2 * Time.MS_PER_SEC;
	
	private static final int QUEUE_CAPACITY = 100000;
	
	// The queue capacity levels when an error message should be e-mailed out. 
	// The max value should be 1.0. 
	private final double levels[] = { 0.5, 0.8, 1.00 };
	
	// The queue that objects to be stored are placed in
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(QUEUE_CAPACITY);
	
	// When running in playback mode where getting AVLReports from database
	// instead of from an AVL feed, then debugging and don't want to store
	// derived data into the database because that would interfere with the
	// derived data that was already stored in real time.
	private final boolean doNotStoreToDb;
	
	// For keeping track of index into levels, which level of capacity of
	// queue being used. When level changes then an e-mail is sent out warning
	// the operators.
	private double indexOfLevelWhenMessageLogged = 0;
	
	// For keeping track of maximum capacity of queue that was used. 
	// Used for logging when queue use is going down.
	private double maxQueueLevel = 0.0;
	
	// This is a singleton class that only returns a single object per projectId.
	private static Map<String, DataDbLogger> dataDbLoggerMap = 
			new HashMap<String, DataDbLogger>(1);
	
	// So can access projectId for logging messages
	private String projectId;
	
	// The Session for writing data to db
	private SessionFactory sessionFactory;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(DataDbLogger.class);

	/********************** Member Functions **************************/

	/**
	 * Factory method. Returns the singleton db logger for the specified
	 * projectId.
	 * 
	 * @param projectId Id of database to be written to
	 * @param doNotStoreToDb Specifies if in playback mode and shouldn't
	 * write data to db.
	 * @return The DataDbLogger for the specified projectId
	 */
	public static DataDbLogger getDataDbLogger(String projectId, boolean doNotStoreToDb) {
		synchronized (dataDbLoggerMap) {
			DataDbLogger logger = dataDbLoggerMap.get(projectId);
			if (logger == null) {
				logger = new DataDbLogger(projectId, doNotStoreToDb);
				dataDbLoggerMap.put(projectId, logger);
			}
			return logger;
		}
	}
	
	/**
	 * Constructor. Private so that factory method getDataDbLogger()
	 * has to be used. Starts up separate thread that actually
	 * reads from queue and stores the data.
	 * 
	 * @param projectId Id of database to be written to
	 * @param doNotStoreToDb Specifies if in playback mode and shouldn't
	 * write data to db.
	 */
	private DataDbLogger(String projectId, boolean doNotStoreToDb) {
		this.projectId = projectId;
		this.doNotStoreToDb = doNotStoreToDb;
		
		// Create the reusable heavy weight session factory
		sessionFactory = HibernateUtils.getSessionFactory(projectId);
		
		// Start up separate thread that reads from the queue and
		// actually stores the data
		NamedThreadFactory threadFactory = new NamedThreadFactory(getClass().getSimpleName());
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
		executor.execute(new Runnable() {
			public void run() {
				processData();
				}
			});

	}
	
	/**
	 * Returns how much capacity of the queue is being used up. 
	 * 
	 * @return a value between 0.0 and 1.0 indicating how much of queue being used
	 */
	public double queueLevel() {
		int remainingCapacity = queue.remainingCapacity();
		int totalCapacity = queue.size() + remainingCapacity;
		double level = 1.0  - (double) remainingCapacity / totalCapacity;
		return level;
	}
	
	/**
	 * Returns the index into levels that the queue capacity is at.
	 * For determining if should send e-mail warning message.
	 * 
	 * @param queueLevel
	 * @return
	 */
	private int indexOfLevel(double queueLevel) {
		for (int i=0; i<levels.length; ++i) {
			if (queueLevel < levels[i])
				return i;
		}
		// Must be level of 1.0 so return full size of levels array
		return levels.length;
	}
	
	/**
	 * Adds an object to be saved in the database to the queue.
	 * If queue is getting filled up then an e-mail will be sent
	 * out indicating there is a problem. The queue levels at which
	 * an e-mail is sent out is specified by levels. If queue has
	 * reached capacity then an error message is logged.
	 * 
	 * @param o The object that should be logged to the database
	 * @return True if object added to queue. False if queue was full.
	 */
	public boolean add(Object o) {	
		// If in playback mode then don't want to store the
		// derived data because it would interfere with the
		// derived data already stored when was running in real time.
		if (doNotStoreToDb)
			return true;
		
		// Add the object to the queue
		boolean success = queue.offer(o);

		double level = queueLevel();
		int levelIndex = indexOfLevel(level);
		// If reached a new level then output message e-mail to warn users
		if (levelIndex > indexOfLevelWhenMessageLogged) {
			indexOfLevelWhenMessageLogged = levelIndex;
			String message = success ?
					"DataDbLogger queue filling up " +
					" for projectId=" + projectId +". It is now at " + 
					String.format("%.1f", level*100) + "% capacity with " + 
					queue.size() + " elements already in the queue."
					:
					"DataDbLogger queue is now completely full for projectId=" + 
					projectId + ". LOSING DATA!!!"; 
			logger.error(Markers.email(), message);
		}
		
		// If losing data then log such
		if (!success) {
			logger.error("DataDbLogger queue is now completely full for " +
					"projectId=" + projectId + ". LOSING DATA!!! Failed to " +
					"store object=[" + o + "]");
		}
		
		// Keep track of max queue level so can log it when queue level 
		// is decreasing again.
		if (level > maxQueueLevel)
			maxQueueLevel = level;
		
		// Return whether was successful in adding object to queue
		return success;
	}
	
	/**
	 * Gets the next object from the head of the queue, waiting if
	 * necessary until an object becomes available. If the capacity
	 * level drops significantly from when last logged then that
	 * info is logged to indicate that the situation is getting better. 
	 * When the queue level drops down below 10% of a specified level
	 * then an e-mail mail message is sent out indicating such. That way
	 * a supervisor can see that the queue is being cleared out.
	 * @return The object to be stored in the database
	 */
	private Object get() {
		// Get the next object from the head of the queue
		Object o = null;
		do {
			try {
				o = queue.take();
			} catch (InterruptedException e) {
				// If interrupted simply try again
			}
		} while (o == null);
		
		// Log if went below a capacity level
		// See if queue dropped to 10% less than the previously logged level.
		// Use a margin of 10% so that don't get flood of messages if queue
		// oscillating around a level.
		double level = queueLevel();
		int levelIndexIncludingMargin = indexOfLevel(level + 0.10);
		if (levelIndexIncludingMargin < indexOfLevelWhenMessageLogged) {
			logger.error(Markers.email(), "DataDbLogger queue emptying out somewhat " +
					" for projectId=" + projectId +". It is now at " + 
					String.format("%.1f", level*100) + "% capacity with " + queue.size() + 
					" elements already in the queue. The maximum capacity was " +
					String.format("%.1f", maxQueueLevel*100) + "%.");
			indexOfLevelWhenMessageLogged = levelIndexIncludingMargin;
			
			// Reset the maxQueueLevel so can determine what next peak is
			maxQueueLevel = level;
		}

		// Return the result
		return o;
	}
	
	/**
	 * Returns whether queue has any elements in it that should be stored.
	 * @return true if queue has data that should be stored to db
	 */
	private boolean queueHasData() {
		return !queue.isEmpty();
	}
	
	/**
	 * Store just a single object into data. This is slower than batching a few
	 * at a time. Should be used when the batching encounters an exception. This
	 * way can still store all of the good data from a batch.
	 * 
	 * @param o
	 */
	private void processSingleObject(Object objectToBeStored) {
		Session session = null;
		try {
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			logger.debug("Individually saving object {}", objectToBeStored);
			session.save(objectToBeStored);
			tx.commit();
		} finally {
			session.close();
		}
	}
	
	/**
	 * Determines highest level cause of exception. Useful
	 * for determine the root cause of the exception so that
	 * appropriate error message can be displayed.
	 * @param e
	 * @return
	 */
	private Throwable getRootCause(Exception e) {
		Throwable prev =  e;
		while (true) {
			Throwable next = prev.getCause();
			if (next == null) 
				return prev;
			else
				prev = next;
		}
	}
	
	/**
	 * Returns true if the exception indicates that there is a problem connecting
	 * to the database as opposed to with the SQL.
	 * 
	 * @param e
	 * @return
	 */
	private boolean shouldKeepTryingBecauseConnectionException(HibernateException e) {
		// Need to know if it is a problem with the database not
		// being accessible or if there is a problem with the SQL/data.
		// If there is a problem accessibility of the database then
		// want to keep trying writing the old data. But if it is
		// a problem with the SQL/data then only want to try to write
		// the good data from the batch a single time to make sure 
		// all good data is written.
		// From javadocs for for org.hivernate.exception at
		// http://docs.jboss.org/hibernate/orm/3.5/javadocs/org/hibernate/exception/package-frame.html 
		// can see that there are a couple of different exception types. 
		// From looking at documentation and testing found out that 
		// bad SQL is indicated by 
		//   ConstraintViolationException
		//   DataException
		//   SQLGrammarException
		// Appears that for bad connection could get:
		//   JDBCConnectionException (was not able to verify experimentally)
		//   GenericJDBCException    (obtained when committing transaction with db turned off)
		// So if exception is JDBCConnectionException or JDBCGenericException
		// then should keep retrying until successful.
		boolean keepTryingTillSuccessfull = e instanceof JDBCConnectionException ||
				                            e instanceof GenericJDBCException;
		return keepTryingTillSuccessfull;
	}
	
	/**
	 * Process a batch of data, as specified by BATCH_SIZE member. The goal
	 * is to batch a few db writes together to reduce load on network and on
	 * db machines. There this method will try to store multiple objects
	 * from the queue at once, up to the BATCH_SIZE.
	 * 
	 * If there is an exception with an object being written then the
	 * batch of objects will be written individually so that all of the
	 * good data will still be stored.
	 * 
	 *  When looked at Hibernate documentation on batch writing there is
	 *  mention of using:
	 *      if (++batchingCounter % BATCH_SIZE == 0) {
	 *        session.flush();
	 *        session.clear();
	 *      }
	 * But the above doesn't commit the data to the db until the transaction
	 * commit is done. Therefore the need here isn't true Hibernate batch
	 * processing. Instead, need to use a transaction for each batch.
	 */
	private void processBatchOfData() {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		// Create an array for holding what is being written to db. If there
		// is an exception with one of the objects, such as a constraint violation,
		// then can try to write the objects one at a time to make sure that the
		// the good ones are written. This way don't lose any good data even if
		// an exception occurs while batching data.
		List<Object> objectsForThisBatch = new ArrayList<Object>(HibernateUtils.BATCH_SIZE);
		
		try {			
			int batchingCounter = 0;
			do {	
				// Get the object to be stored from the queue
				Object objectToBeStored = get();
				
				objectsForThisBatch.add(objectToBeStored);
				
				// Write the data to the session. This doesn't yet
				// actually write the data to the db though. That is only
				// done when the session is flushed or committed.
				logger.debug("DataDbLogger batch saving object={}", 
						objectToBeStored);
				session.save(objectToBeStored);
			} while (queueHasData() && ++batchingCounter < HibernateUtils.BATCH_SIZE);
			
			tx.commit();
			session.close();
		} catch (HibernateException e) {
			Throwable cause = getRootCause(e);
			// If it is a SQLGrammarException then also log the SQL to
			// help in debugging.
			String additionaInfo = e instanceof SQLGrammarException ?
					" SQL=\"" + ((SQLGrammarException) e).getSQL() + "\"" 
					: "";
			logger.error(e.getClass().getSimpleName() + " for database for " +
					"project=" + projectId + " when batch writing objects: " + 
					cause.getMessage() + ". Will try to write each object " +
					"from batch individually." + additionaInfo);		
			
			// Close session here so that can process the objects individually
			// using a new session.
			session.close();
							
			// Write each object individually so that the valid ones will be
			// successfully written.
			for (Object o : objectsForThisBatch) {
				boolean shouldKeepTrying = false;
				do {
					try {
						processSingleObject(o);
						shouldKeepTrying = false;
					} catch (HibernateException e2) {
						// Need to know if it is a problem with the database not
						// being accessible or if there is a problem with the SQL/data.
						// If there is a problem accessibility of the database then
						// want to keep trying writing the old data. But if it is
						// a problem with the SQL/data then only want to try to write
						// the good data from the batch a single time to make sure 
						// all good data is written.
						if (shouldKeepTryingBecauseConnectionException(e2)) {
							shouldKeepTrying = true;
							logger.error("Encountered database connection " +
									"exception so will sleep for {} msec and " +
									"will then try again.", TIME_BETWEEN_RETRIES);
							Time.sleep(TIME_BETWEEN_RETRIES);
						}
						
						// Output message on what is going on
						Throwable cause2 = getRootCause(e2);
						logger.error(e2.getClass().getSimpleName() + " when individually writing object " +
								o + ". " + 
								(shouldKeepTrying?"Will keep trying. " : "") +
								"msg=" + cause2.getMessage()); 
					}
				} while (shouldKeepTrying);
			}
		}
	}
	
	/**
	 * This is the main method for processing data. It simply keeps on calling
	 * processBatchOfData() so that data is batched as efficiently as possible.
	 * Exceptions are caught such that this method will continue to run
	 * indefinitely.
	 */
	private void processData() {
		while (true) {
			try {
				logger.debug("DataDbLogger.processData() processing batch of " +
						"data to be stored in database.");
				processBatchOfData();
			} catch (Exception e) {
				logger.error("Error writing data to database via DataDbLogger. " +
						"Look for ERROR in log file to see if the database classes " +
						"were configured correctly. Error: "
						+ e);
				
				// Don't try again right away because that would be wasteful
				Time.sleep(TIME_BETWEEN_RETRIES);
			}
		}
	}
	
	/**
	 * Just for doing some testing
	 * @param args
	 */
	public static void main(String[] args) {
		DataDbLogger logger = getDataDbLogger("test", false);

		long initialTime = (System.currentTimeMillis()  / 1000) * 1000;

		for (int i=0; i<25; ++i)
			logger.add(new AvlReport("test", initialTime + i, 1.23, 4.56));
		
		// This one should cause constraint problem with the second batch.
		// Need to not retry for such an exception
		logger.add(new AvlReport("test", initialTime, 1.23, 4.56)); 
		
		for (int i=HibernateUtils.BATCH_SIZE; i<2*HibernateUtils.BATCH_SIZE;++i)
			logger.add(new AvlReport("test", initialTime+i, 1.23, 4.56));

				
		// Wait for all data to be processed
		while(logger.queueHasData())
			Time.sleep(1000);
	}

}
