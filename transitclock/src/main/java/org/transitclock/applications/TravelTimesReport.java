package org.transitclock.applications;

import com.google.common.collect.ArrayListMultimap;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.TravelTimesForStopPath;
import org.transitclock.db.structs.TravelTimesForTrip;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * output some statistics on travel times.  Correct the most eggregiosu via -Drepair=true
 */
public class TravelTimesReport {

    private static final Logger logger =
            LoggerFactory.getLogger(TravelTimesReport.class);

    public static final String REPORT_CMD = "report";
    public static final String SHOW_TRIP_CMD = "show_trip";

    public static final String AUTO_CORRECT = "repair";

    private static final String ZERO_STOP_PATH_LENGTH = "Zero Stop Path Length";
    private static final String NEGATIVE_STOP_PATH_LENGTH = "Negative Stop Path Length";
    private static final String LARGE_STOP_PATH_LENGTH = "Large Stop Path Length";
    private static final String NEGATIVE_STOP_TIME = "Negative Stop Time";
    private static final String LARGE_STOP_TIME = "Large Stop Time";

    private static IntegerConfigValue MAX_STOP_PATH_TRAVEL_TIME = new IntegerConfigValue("maxStopPathTravelTime",
            20 * 60 * 1000,
            "Logical maximum time it can take to traverse a segment");

    private static IntegerConfigValue MAX_STOP_PATH_LENGTH = new IntegerConfigValue("maxStopPathLength",
            300,
            "max expected segment length in meters");

    private static IntegerConfigValue MAX_STOP_TIME = new IntegerConfigValue("maxStopTime",
            20 * 60 * 1000,
            "max expected stop time");

    private static final String EMPTY_STOP_PATHS = "Empty Stop Paths";
    private static final String NULL_STOP_PATH_TRAVEL_TIME = "Null Stop Path Travel Time";
    private static final String ZERO_STOP_PATH_TRAVEL_TIME = "Zero Stop Path Travel Time";
    private static final String NEGATIVE_STOP_PATH_TRAVEL_TIME = "Negative Stop Path Travel Time";
    private static final String LARGE_STOP_PATH_TRAVEL_TIME = "Large Stop Path Travel Time";

    private String agencyId;
    private int configRev;
    private int travelTimesRev;
    private int stopPathCount = 0;

    public void setAgencyId(String id) {
        this.agencyId = id;
    }

    public void setConfigRev(int revision) {
        this.configRev = revision;
    }

    public void setTravelTimesRev(int rev) {
        this.travelTimesRev = rev;
    }

    ArrayListMultimap<String, String> errorsByType = ArrayListMultimap.create();
    ArrayListMultimap<String, Object> errorsById = ArrayListMultimap.create();

    public TravelTimesReport() {

    }

    public void printTravelTimesForTrip(String tripId) {
        Session session = HibernateUtils.getSession(agencyId);
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<TravelTimesForTrip> travelTimesForTrips = loadTravelTimes(session);
            logger.info("found {} travel times", travelTimesForTrips.size());
            if (travelTimesForTrips.isEmpty()) {
                logger.error("loadTravelTimes failed for configuration");
            }
            boolean found = false;
            for (TravelTimesForTrip travelTimesForTrip : travelTimesForTrips) {
                //logger.info("checking trip {}", travelTimesForTrip.getTripCreatedForId());
                if (travelTimesForTrip.getTripCreatedForId().equals(tripId)) {
                    found = true;
                    for (TravelTimesForStopPath travelTimesForStopPath : travelTimesForTrip.getTravelTimesForStopPaths()) {
                        logger.info("{},{},{},{},{},{},{}",
                                SHOW_TRIP_CMD,
                                travelTimesRev,
                                travelTimesForStopPath.getStopPathId(),
                                travelTimesForStopPath.getHowSet(),
                                travelTimesForStopPath.getTravelTimeSegmentLength(),
                                travelTimesForStopPath.getStopTimeMsec(),
                                prettyPrint(travelTimesForStopPath.getTravelTimesMsec()));
                    }

                }
            }
            if (!found) {
                logger.error("trip {} not found for configuration rev {}", tripId, travelTimesRev);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logger.error("exception with transaction", e);
        } finally {
            session.close();
            HibernateUtils.clearSessionFactory();
        }

    }

    private String prettyPrint(List<Integer> travelTimesMsec) {
        StringBuffer sb = new StringBuffer();
        for (Integer msec : travelTimesMsec) {
            sb.append(msec).append(",");
        }
        return sb.substring(0, sb.length()-1);
    }

    public void runReport() {
        Session session = HibernateUtils.getSession(agencyId);
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<TravelTimesForTrip> travelTimesForTrips = loadTravelTimes(session);
            addSystemStat("Total Travel Times", travelTimesForTrips.size());
            boolean dirty = processTravelTimes(travelTimesForTrips);
            addSystemStat("Total Stop Paths", stopPathCount);
            //logger.info("processed {} stop paths", stopPathCount);
            logResults();
            if (dirty) {
                logger.info("Flushing data to database...");
                session.flush();
                logger.info("Done flushing");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logger.error("exception with transaction", e);
        } finally {
            session.close();
            HibernateUtils.clearSessionFactory();
        }
        }

    private void addSystemStat(String key, int value) {
        logger.info("{},{},{},{},{}", "STAT", travelTimesRev, "SYSTEM", key, value);
    }

    private void logResults() {
        Set<String> errorTypes = this.errorsById.asMap().keySet();
        for (String errorType : errorTypes) {
            String howSet = errorType.split("_")[0];
            String type = errorType.split("_")[1];
            logger.info("{},{},{},{},{}", "STAT", travelTimesRev, howSet, type, errorsById.get(errorType).size());
        }
    }

    private boolean processTravelTimes(List<TravelTimesForTrip> travelTimesForTrips) {
        boolean dirty = false;
        for (TravelTimesForTrip travelTimesForTrip : travelTimesForTrips) {
            String howSet = getHowSet(travelTimesForTrip);
            if (travelTimesForTrip.getTravelTimesForStopPaths().isEmpty()) {
                addError(EMPTY_STOP_PATHS, travelTimesForTrip.getId(), howSet,
                        travelTimesForTrip.getId(), travelTimesForTrip.getConfigRev(), travelTimesForTrip.getTravelTimeRev());
                continue;
            }

            TravelTimesForStopPath firstStopPath = travelTimesForTrip.getTravelTimesForStopPath(0);

            dirty |= validateStopPath(firstStopPath, howSet);
            stopPathCount++;
            for (int stopPathIndex = 1; stopPathIndex < travelTimesForTrip.getTravelTimesForStopPaths().size(); stopPathIndex++ ) {
                validateStopPath(travelTimesForTrip.getTravelTimesForStopPath(stopPathIndex), howSet);
                stopPathCount++;
            }
        }
        return dirty;
    }

    private String getHowSet(TravelTimesForTrip travelTimesForTrip) {
        if (travelTimesForTrip == null)
            return "unknown";
        if (travelTimesForTrip.getTravelTimesForStopPaths().isEmpty())
            return "unknown";
        return travelTimesForTrip.getTravelTimesForStopPath(0).getHowSet().toString();
    }

    private boolean validateStopPath(TravelTimesForStopPath stopPath, String howSet) {
        boolean dirty = false;

        int id = stopPath.getInternalId();
        String stopPathId = stopPath.getStopPathId();
        int configRev = stopPath.getConfigRev();
        int ttrev = stopPath.getTravelTimesRev();

        if (stopPath.getTravelTimeSegmentLength() == 0.0)
            addError(ZERO_STOP_PATH_LENGTH, stopPathId, howSet, id, configRev, ttrev);

        if (stopPath.getTravelTimeSegmentLength() < 0)
            addError(NEGATIVE_STOP_PATH_LENGTH, stopPathId, howSet, id, configRev, ttrev);

        if (stopPath.getTravelTimeSegmentLength() > MAX_STOP_PATH_LENGTH.getValue())
            addError(LARGE_STOP_PATH_LENGTH, stopPathId, howSet, id, configRev, ttrev);

        if (stopPath.getStopTimeMsec() < 0)
            addError(NEGATIVE_STOP_TIME, stopPathId, howSet, id, configRev, ttrev);

        if (stopPath.getStopTimeMsec() > MAX_STOP_TIME.getValue()) {
            addError(LARGE_STOP_TIME, stopPathId, howSet, id, configRev, ttrev);
        }


        int ttmIndex = 0;
        List<Integer> invalidTravelTimesIndicies = new ArrayList<>();
        for (Integer ttm : stopPath.getTravelTimesMsec()) {
            if (ttm == null) {
                addError(NULL_STOP_PATH_TRAVEL_TIME, stopPath.getStopPathId(), howSet, id, configRev, ttrev);
                continue;
            }
            if (ttm == 0)
                addError(ZERO_STOP_PATH_TRAVEL_TIME, stopPath.getStopPathId(), howSet, id, configRev, ttrev);

            if (ttm < 0)
                addError(NEGATIVE_STOP_PATH_TRAVEL_TIME, stopPath.getStopPathId(), howSet, id, configRev, ttrev);

            if (ttm > MAX_STOP_PATH_TRAVEL_TIME.getValue()) {
                addError(LARGE_STOP_PATH_TRAVEL_TIME, stopPath.getStopPathId(), howSet, id, configRev, ttrev);
                if (repair) {
                    invalidTravelTimesIndicies.add(ttmIndex);
                }

            }
            ttmIndex++;
        }

        // TravelTimesForStopPath is final -- the only repair we can make is to the list of travel times
        if (repair() && !invalidTravelTimesIndicies.isEmpty()) {
            logger.error("ACTION,stopPath,{},travelTimes post,{}", stopPath.getStopPathId(), stopPath.getTravelTimesMsec());
            for (Integer badIndex : invalidTravelTimesIndicies) {
                dirty = true;
                stopPath.getTravelTimesMsec().set(badIndex, 0);
            }
            logger.error("ACTION,stopPath,{},travelTimes post,{}", stopPath.getStopPathId(), stopPath.getTravelTimesMsec());
        }
        return dirty;
    }

    private void addError(String errorType, Object natrualKey, String howSet, Integer id, Integer configRev, Integer ttrev) {
        if (ttrev != null)
            logger.error("DETAILS,"+ howSet + "_" + errorType + "," + natrualKey + "," + id
                    + "," + configRev  + "," + ttrev);
        this.errorsById.put(howSet + "_" + errorType, id);
    }

    private List<TravelTimesForTrip> loadTravelTimes(Session session) {
        List<TravelTimesForTrip> allTravelTimes = session.createCriteria(TravelTimesForTrip.class)
                .add(Restrictions.eq("travelTimesRev", travelTimesRev))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
        return allTravelTimes;
    }

    Boolean repair = null;
    public boolean repair() {
        if (repair == null) {
            repair = "true".equalsIgnoreCase(System.getProperty(AUTO_CORRECT));
        }
        return repair;
    }

    // usage:  CMD {travelTimeRevStart} {travelTimesRevStop} [ report_args ]
    public static void main(String[] args) {
        logger.info("Starting travel times report");

        String cmd = REPORT_CMD;

        Integer startTravelTimesRev = null;
        Integer maxTravelTimesRev = null;


        cmd = args[0];
        startTravelTimesRev = Integer.parseInt(args[1]);
        maxTravelTimesRev = Integer.parseInt(args[2]);
        logger.info("loading travel times for rev {}", startTravelTimesRev);

        ConfigFileReader.processConfig();

        String agencyId = AgencyConfig.getAgencyId();
        int configRev = ActiveRevisions.get(agencyId).getConfigRev();

        if (startTravelTimesRev == null || maxTravelTimesRev == null) {
            // run for last rev
            startTravelTimesRev = ActiveRevisions.get(agencyId).getTravelTimesRev();
            maxTravelTimesRev = ActiveRevisions.get(agencyId).getTravelTimesRev();
            logger.info("liading travel times for latest rev {}", startTravelTimesRev);
        }

        int i = startTravelTimesRev;
        while (i <= maxTravelTimesRev) {
            logger.info("loading travelTimeRev {}", i);
            TravelTimesReport ttr = new TravelTimesReport();
            ttr.setAgencyId(agencyId);
            ttr.setConfigRev(configRev);
            ttr.setTravelTimesRev(i);
            if (REPORT_CMD.equals(cmd)) {
                ttr.runReport();
            } else if (SHOW_TRIP_CMD.equals(cmd)) {
                ttr.printTravelTimesForTrip(args[3].trim());
            } else {
                logger.error("misunderstood command {}", cmd);
            }
            i++;
        }
        logger.info("travel times report complete!");
    }


}
