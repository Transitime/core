package org.transitclock.applications;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.ApcMatch;
import org.transitclock.avl.ApcMatcher;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.Time;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load External APC data and map to existing ArrivalDepartures.
 *
 * This was written more as a one-off for a data fix, but could be
 * refactored to be more useful.
 */
public class LoadApcData {

  private static final Logger logger =
          LoggerFactory.getLogger(LoadApcData.class);
  private static final int MESSAGE_ID = 0;
  private static final int ALIGHTINGS = 1;
  private static final int ARRIVAL = 2;
  private static final int BOARDINGS = 3;
  private static final int DEPARTURE = 4;
  private static final int DOOR_CLOSE = 5;
  private static final int DOOR_OPEN = 6;
  private static final int DRIVER_ID = 7;
  private static final int LAT = 8;
  private static final int LON = 9;
  private static final int ODO = 10;
  private static final int SERVICE_DATE = 11;
  private static final int TIME = 12;
  private static final int VEHICLE_ID = 13;

  private String filename;

  static {
    ConfigFileReader.processConfig();;
  }


  public LoadApcData(String filename) {
    this.filename = filename;
  }

  private static void cleanup(Session session) {
    logger.info("deleting ApcReports....(be patient)");
    int numUpdates = 0;
    String hql = "DELETE ApcReport";
    numUpdates = session.createQuery(hql).executeUpdate();
    logger.info("deleted {} ApcReports", numUpdates);
  }

  private static Map<String, List<ApcParsedRecord>> groupRecordsByDate(List<ApcParsedRecord> records) {
    Map<String, List<ApcParsedRecord>> groups = new HashMap<>();
    for (ApcParsedRecord apc : records) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      String key = sdf.format(apc.getTime());
      if (groups.containsKey(key)) {
        groups.get(key).add(apc);
      } else {
        ArrayList<ApcParsedRecord> list = new ArrayList<>();
        list.add(apc);
        groups.put(key, list);
      }
    }
    return groups;
  }

  private static List<ArrivalDeparture> findArrivalDeparturesForApc(Session session, List<ApcParsedRecord> apcRecords) {
    Criteria criteria = session.createCriteria(ArrivalDeparture.class);
    ApcParsedRecord firstRecord = apcRecords.get(0);
    ApcParsedRecord lastRecord = apcRecords.get(apcRecords.size()-1);
    long window = 1 * Time.MS_PER_HOUR;

    Date beginTime = new Date(firstRecord.getTime()-window);
    Date endTime = new Date(lastRecord.getTime()+window);
    logger.info("finding A/Ds from {} to {}", beginTime, endTime);
    return StopArrivalDepartureCacheInterface.createArrivalDeparturesCriteria(criteria,
            beginTime, endTime);
  }

  private List<ApcParsedRecord> load() throws Exception {
    List<ApcParsedRecord> records = new ArrayList<>();
    String fileContents = getStreamAsString(new FileInputStream(filename));
    for (String line : fileContents.split("\n")) {
      ApcParsedRecord record = toRecord(line);
      if (record != null) {
        records.add(record);
      }
    }
    return records;
  }

  private ApcParsedRecord toRecord(String line) {
    //"select messageId, alightings, arrival, boardings, departure, doorClose, doorOpen, driverId, lat, lon, odo, serviceDate, time, vehicleId from ApcReport"
    ApcParsedRecord r = new ApcParsedRecord(
            get(line, MESSAGE_ID),
            getDateToLong(line, TIME),
            getLong(line, SERVICE_DATE),
            get(line, DRIVER_ID),
            getInt(line, ODO),
            get(line, VEHICLE_ID),
            getInt(line, BOARDINGS),
            getInt(line, ALIGHTINGS),
            getInt(line, DOOR_OPEN),
            getInt(line, DOOR_CLOSE),
            getInt(line, ARRIVAL),
            getInt(line, DEPARTURE),
            getDouble(line, LAT),
            getDouble(line, LON)
    );
    return r;
  }

  private double getDouble(String line, int i) {
    return Double.parseDouble(line.split("\t")[i]);
  }

  private int getInt(String line, int i) {
    return Integer.parseInt(line.split("\t")[i]);
  }

  private long getDateToLong(String line, int i) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    try {
      return sdf.parse(line.split("\t")[i]).getTime();
    } catch (ParseException e) {
      logger.error("invalid date {} at index {} for line {}",
              line.split("\t")[i],
              i,
              line);
    }
    return -1;
  }

  private String get(String line, int i) {
    return line.split("\t")[i];
  }
  private Long getLong(String line, int i) {
    return Long.parseLong(line.split("\t")[i]);
  }

  public static String getStreamAsString(InputStream inputStream) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    byte[] byteArray = buffer.toByteArray();

    String text = new String(byteArray, StandardCharsets.UTF_8);
    return text;
  }


  public static void main(String[] args) {
    logger.info("Starting loadApc with file " + args[0]);

    LoadApcData loader = new LoadApcData(args[0]);
    Session session = null;
    Transaction tx = null;
    try {
      String agencyId = AgencyConfig.getAgencyId();

      int configRev = ActiveRevisions.get(agencyId).getConfigRev();
      logger.info("configRev = {} for agencyId = {}", configRev, agencyId);
      session = HibernateUtils.getSession(agencyId);
      tx = session.beginTransaction();

      List<ApcParsedRecord> records = loader.load();
      logger.info("parsed {} records", records.size());
      System.out.println("loaded " + records.size() + " records");

      cleanup(session);

      Map<String, List<ApcParsedRecord>> recordsByDate = groupRecordsByDate(records);

      List<String> sortedKeys = new ArrayList<>(recordsByDate.keySet());
      Collections.sort(sortedKeys);

      List<ApcReport> reports = new ArrayList<>();

      for (String key : sortedKeys) {

        List<ApcParsedRecord> subset = recordsByDate.get(key);
        logger.info("reference day is {} with {} apc records", key, subset.size());
        int total = 0;
        int matched = 0;

        List<ArrivalDeparture> arrivalDepartureList = findArrivalDeparturesForApc(session, subset);
        logger.info("retrieved {} A/Ds", arrivalDepartureList.size());
        ApcMatcher matcher = new ApcMatcher(arrivalDepartureList);
        List<ApcMatch> matches = matcher.match(records);
        logger.info("matched {} records", matches.size());

        for (ApcMatch match : matches) {
          ApcReport report = match.getApc().toApcReport();
          reports.add(report);
          if (report.getArrivalDeparture() != null)
            matched++;
          total++;
        }
        if (matched != 0) {
          System.out.println("matched " + matched + " out of " + total
                  + " (" + (matched / total) + ")");
          logger.info("matched " + matched + " out of " + total
                  + " (" + (matched / total) + ")");
        } else {
          logger.info("no matched records....");
        }
      }

      logger.info("writing to database...");

      int i = 0;
      for (ApcReport report : reports) {
        i++;
        session.save(report);
        if (i % 1000 == 0) {
          logger.info("flushing {}", i);
          session.flush();
        }
      }
      logger.info("committing");
      tx.commit();
      logger.info("exiting with {} reports of {} ready for persistence", reports.size(), records.size());


    } catch (Exception any) {
      logger.error("failed with {}", any, any);
      System.out.println("failed!");
      any.printStackTrace();
      if (tx != null)
        tx.rollback();
      System.exit(1);
    } finally {
      session.close();
    }

    logger.info("complete!");
    System.exit(0);
  }

}
