package org.transitclock.applications;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.core.RunTimeServiceUtils;
import org.transitclock.core.RunTimesServiceUtilsImpl;
import org.transitclock.core.reporting.*;
import org.transitclock.core.travelTimes.DataFetcher;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Entry point (Application) for updating run times for reports.
 *
 * Follows pattern of UpdateTravelTimes, invoke it the same way.
 */
public class UpdateRunTimes {

  public static double DEFAULT_CLAMPING_SPEED = 97.0;

  static {
    ConfigFileReader.processConfig();
  }

  private static final Logger logger =
          LoggerFactory.getLogger(UpdateRunTimes.class);

  private String agencyId;
  private Date beginTime;
  private Date endTime;
  private Double clampingSpeed;
  private RunTimeWriter writer;

  public UpdateRunTimes(String agencyId, Date beginTime, Date endTime, Double clampingSpeed) {
    this.agencyId = agencyId;
    this.beginTime = beginTime;
    this.endTime = endTime;
    this.clampingSpeed = clampingSpeed;
    this.writer = new RunTimeWriterImpl();
  }

  public void run() {
    IntervalTimer timer = new IntervalTimer();

    // Get list of a/d
    DataFetcher dataFetcher = new DataFetcher(agencyId, null);
    Map<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> arrivalsDeparturesMap =
            dataFetcher.readArrivalsDepartures(agencyId, beginTime, endTime);
    RunTimeCache cache = new RunTimeCacheImpl();

    // Get a database session
    Session session = HibernateUtils.getSession(agencyId);
    try {
      // do work
      RunTimeServiceUtils serviceUtils = new RunTimesServiceUtilsImpl(session);
      RunTimeLoader loader = new RunTimeLoader(writer, cache, clampingSpeed, serviceUtils);
      writer.cleanupFromPreviousRun(agencyId, beginTime, endTime);
      loader.run(agencyId, arrivalsDeparturesMap);
    } catch (Exception e) {
      logger.error("Unexpected exception occurred", e);
      throw e;
    } finally {
      // Close up db connection
      session.close();
    }
    logger.info("Done processing Run Times in {} ms. Changes successfully "
            + "committed to database. ", timer.elapsedMsecStr());
    HibernateUtils.clearSessionFactory();
  }

  public static void main(String[] args) {
    logger.info("Starting update Run Times");
    String agencyId = AgencyConfig.getAgencyId();

    String startDateStr = args[0];
    String endDateStr = args.length > 1 ? args[1] : startDateStr;

    logger.info("Starting Date {}", startDateStr);
    logger.info("End Date {}", endDateStr);

    Double clampingSpeed = args.length > 2 ? Double.parseDouble(args[2]) : DEFAULT_CLAMPING_SPEED;

    // Set the timezone for the application. Must be done before
    // determine begin and end time so that get the proper time of day.
    int configRev = ActiveRevisions.get(agencyId).getConfigRev();
    TimeZone timezone =
            Agency.getAgencies(agencyId, configRev).get(0).getTimeZone();
    TimeZone.setDefault(timezone);

    // Determine beginTime and endTime
    Date beginTime = null;
    Date endTime = null;
    try {
      logger.info("Parse Date");
      beginTime = Time.parseDate(startDateStr);
      endTime = new Date(Time.parseDate(endDateStr).getTime() +
              Time.MS_PER_DAY);
    } catch (ParseException e) {
      logger.error("Problem parsing date", e);
      e.printStackTrace();
      System.exit(-1);
    }

    // Log params used right at top of log file
    logger.info("Processing travel times for beginTime={} endTime={}",
            startDateStr, endDateStr);

    UpdateRunTimes runner = new UpdateRunTimes(agencyId, beginTime, endTime, clampingSpeed);
    try {
      runner.run();
    } finally {
      // force daemon threads to exit
      System.exit(0);
    }
  }
}
