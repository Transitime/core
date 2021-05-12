package org.transitclock.core.reporting;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.RunTimesForRoutes;

/**
 * Database operations for RunTimeLoader.
 */
public class RunTimeWriter {

  private static final int BATCH_SIZE = 1000;

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeWriter.class);

  public RunTimeWriter() {

  }

  /**
   * write the validated contents of the cache out to the database via
   * the session param.
   */
  public void writeToDatabase(Session session,
                              RunTimeCache cache) {
    int counter = 0;
    for (RunTimesForRoutes rt : cache.getAll()) {
      if (cache.containsDuplicateStops(rt)) {
        logger.error("filtered {} as it contains duplicates", rt);
        rt = cache.deduplicate(rt);
      }
      if (!cache.isValid(rt)) {
        logger.error("dropping {} as its invalid", rt);
        continue;
      }
      counter++;
      try {
        session.save(rt);
      } catch (Throwable t) {
        logger.error("onSave error {} for record {}",
                t, rt, t);
      }

      if (counter % BATCH_SIZE == 0) {
        logger.info("flushing at {}", counter);
        try {
          session.flush();
        } catch (Throwable t) {
          logger.error("onFlush error {} for record {}",
                  t, rt, t);
        }
        logger.info("flushed with {}", counter);
      }
    }
    session.clear(); // clear out orphans so they don't persist
  }

  /**
   * Delete the existing RunTimes for the current configRev
   */
  public int cleanupFromPreviousRun(Session session, String agencyId) {
    int configRev =  ActiveRevisions.get(agencyId).getConfigRev();
    // delete run times for configRev
    logger.info("deleting RunTimesForStops....(be patient)");
    int numUpdates = 0;
    String hql = "DELETE RunTimesForStops WHERE configRev=" + configRev;
    numUpdates = session.createQuery(hql).executeUpdate();

    logger.info("deleting RunTimesForRoutes....(be patient)");
    hql = "DELETE RunTimesForRoutes WHERE configRev=" + configRev;
    numUpdates += session.createQuery(hql).executeUpdate();

    logger.info("deleted {} RunTimesForStops/Routes", numUpdates);
    return numUpdates;
  }

}
