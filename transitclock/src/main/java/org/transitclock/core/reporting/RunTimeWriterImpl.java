package org.transitclock.core.reporting;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.utils.Time;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Database operations for RunTimeLoader.
 */
public class RunTimeWriterImpl implements RunTimeWriter{

  private static final int BATCH_SIZE = 100;
  // For when cannot connect to data the length of time in msec between retries
  private static final long TIME_BETWEEN_RETRIES = 1 * 1000; //msec

  private static final int QUEUE_SIZE = 1000000;

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeWriterImpl.class);

  public RunTimeWriterImpl() {}

  /**
   * write the validated contents of the cache out to the database via
   * the session param.
   */
  @Override
  public void writeToDatabase(String agencyId, RunTimeCache cache) {
    ArrayBlockingQueue<RunTimesForRoutes> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    queue.addAll(cache.getAll());

    while(queue.size() > 0){
      List<RunTimesForRoutes> buffer = new ArrayList<>(BATCH_SIZE);
      queue.drainTo(buffer, BATCH_SIZE);
      writeBatchToDatabase(agencyId, cache, buffer);
      Time.sleep(10);
    }
  }

  private void writeBatchToDatabase(String agencyId, RunTimeCache cache, List<RunTimesForRoutes> buffer){
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtils.getSession(agencyId);
      tx = session.beginTransaction();
      for (RunTimesForRoutes rt : buffer) {
        if (!cache.isValid(rt)) {
          logger.error("dropping {} as its invalid", rt);
          continue;
        }
        if (cache.containsDuplicateStops(rt)) {
          logger.error("filtered {} as it contains duplicates", rt);
          rt = cache.deduplicate(rt);
        }

        session.save(rt);
      }
      tx.commit();
      session.close();
    } catch (HibernateException e) {
      e.printStackTrace();

      // Rollback the transaction since it likely was not committed.
      // Otherwise can get an error when using Postgres "ERROR:
      // current transaction is aborted, commands ignored until end of
      // transaction block".
      try {
        if (tx != null)
          tx.rollback();
      } catch (HibernateException e2) {
        logger.error(
                "Error rolling back transaction after processing "
                        + "batch of data via DataDbLogger.", e2);
      }

      // Close session here so that can process the objects
      // individually
      // using a new session.
      try {
        if (session != null)
          session.close();
      } catch (HibernateException e2) {
        logger.error("Error closing session after processing "
                + "batch of data via DataDbLogger.", e2);
      }


      // Write each object individually so that the valid ones will be
      // successfully written.
      for (RunTimesForRoutes rt : buffer) {
        boolean shouldKeepTrying = false;
        do {
          try {
            processSingleObject(agencyId, rt);
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
            Throwable cause2 = HibernateUtils.getRootCause(e2);
            logger.error(e2.getClass().getSimpleName() + " when individually writing runTime " +
                    rt + ". " +
                    (shouldKeepTrying ? "Will keep trying. " : "") +
                    "msg=" + cause2.getMessage());
          }
        } while (shouldKeepTrying);
      }
    }
  }

  private void processSingleObject(String agencyId, RunTimesForRoutes runTimeForRoutes) {
    Session session = null;
    try {
      session = HibernateUtils.getSession(agencyId);
      Transaction tx = session.beginTransaction();
      logger.debug("Individually saving runTime {}", runTimeForRoutes);
      session.save(runTimeForRoutes);
      tx.commit();
    } finally {
      if (session != null)
        session.close();
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
   * Delete the existing RunTimes for the current configRev
   */
  @Override
  public int cleanupFromPreviousRun(String agencyId, Date beginDate, Date endDate) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtils.getSession(agencyId);
      tx = session.beginTransaction();

      int configRev = ActiveRevisions.get(agencyId).getConfigRev();
      // delete run times for configRev
      logger.info("deleting RunTimesForStops....(be patient)");
      int numUpdates = 0;
      String hql = "DELETE FROM RunTimesForStops WHERE startTime >= :beginDate AND startTime < :endDate" ;

      Query query = session.createSQLQuery(hql);
      query.setTimestamp("beginDate", beginDate);
      query.setTimestamp("endDate", endDate);
      numUpdates = query.executeUpdate();

      logger.info("deleting RunTimesForRoutes....(be patient)");
      hql = "DELETE RunTimesForRoutes WHERE startTime >= :beginDate AND startTime < :endDate";
      query = session.createQuery(hql);
      query.setTimestamp("beginDate", beginDate);
      query.setTimestamp("endDate", endDate);
      numUpdates += query.executeUpdate();

      tx.commit();

      logger.info("deleted {} RunTimesForStops/Routes", numUpdates);
      return numUpdates;
    } catch (Exception e) {
      if (tx != null)
        tx.rollback();
      logger.error("Unexpected exception occurred when cleaning up previous runtimes", e);
      throw e;
    } finally {
      // Close up db connection
      session.close();
    }
  }

}
