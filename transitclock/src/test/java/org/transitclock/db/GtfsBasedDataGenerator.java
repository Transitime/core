package org.transitclock.db;

import org.apache.commons.io.IOUtils;
import org.transitclock.applications.Core;
import org.transitclock.applications.GtfsFileProcessor;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.core.Indices;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.TemporalMatch;
import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.Time;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Integrate test support of loading GTFS.
 *
 * This is currently specific to ApcGenerator tests, but
 * with some refactoring it could be made generic.
 */
public class GtfsBasedDataGenerator {
  private static final int CONFIG_REV = 1;
  public static final String VEHICLE = "1234";
  public static final String DIRECTION_ID = "0";
  public static final String BLOCK_ID = "1065";
  public static final String TRIP_ID = "18369405-MAR21-MVS-BUS-Weekday-05";
  public static final String HEADWAY_TRIP_ID = "18369406-MAR21-MVS-BUS-Weekday-05";
  private static final String ROUTE_ID = "4";
  private static final String SERVICE_ID = "MAR21-MVS-BUS-Weekday-05";
  private static final String TRIP_PATTERN_ID = "tp1"; // TODO
  private static final String STOP_PATH_ID = "48930_to_17976";

  private String zipFileInClasspath;
  private String agencyId;
  public GtfsBasedDataGenerator(String zipFileInClasspath, String defaultAgencyId) {
    this.zipFileInClasspath = zipFileInClasspath;
    this.agencyId = defaultAgencyId;
  }

  public void load() throws IOException {
    String tmpGtfsFilename = System.getProperty("java.io.tmpdir") + File.separator + zipFileInClasspath;
    File targetFile = new File(tmpGtfsFilename);
    InputStream initialStream = this.getClass().getResourceAsStream(zipFileInClasspath);
    java.nio.file.Files.copy(
            initialStream,
            targetFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
    IOUtils.closeQuietly(initialStream);
    GtfsFileProcessor processor = new GtfsFileProcessor(
            null, null, null,
            tmpGtfsFilename, null,
            null, null,
            null, 0.0,
            60.0,
            3.0,
            10 * Time.MS_PER_SEC, 97.0,
            200.0,
            CONFIG_REV,
            true, false, false
    );
    processor.process();
  }

  public String getAgencyId() {
    return agencyId;
  }

  public List<ApcReport> getApcReports(long referenceTime, int tripIndex, int stopPathIndex,
                                       String stopId, String vehicleId) {
    List<ApcReport> reports = new ArrayList<>();
    for (ApcParsedRecord apc : getApcParsedRecords(referenceTime, tripIndex, stopPathIndex, stopId, vehicleId)) {
      reports.add(apc.toApcReport());
    }
    return reports;
  }

  public List<ApcParsedRecord> getApcParsedRecords(long referenceTime, int tripIndex, int stopPathIndex,
                                                   String stopId, String vehicleId) {
    List<ApcParsedRecord> records = new ArrayList<>();
    long startOfDay = Time.getStartOfDay(new Date(referenceTime));
    ApcParsedRecord apc = new ApcParsedRecord(
            new Long(System.currentTimeMillis()).toString(),
            referenceTime,
            startOfDay,
            "driverId",
            0,
            vehicleId,
            1,
            1,
            0,
            0,
            new Long((referenceTime - startOfDay)/Time.MS_PER_SEC).intValue(),
            new Long((referenceTime - startOfDay + Time.SEC_PER_MIN)/Time.MS_PER_SEC).intValue(),
            0.01,
            0.01);
    ArrivalDeparture.Builder ad = new ArrivalDeparture.Builder(VEHICLE,
            referenceTime,
            referenceTime,
            null,
            DIRECTION_ID,
            tripIndex,
            stopPathIndex,
            stopPathIndex,
            true,
            -1,
            1,
            BLOCK_ID,
            TRIP_ID,
            stopId,
            0,
            0.0f,
            ROUTE_ID,
            ROUTE_ID,
            SERVICE_ID,
            null,
            null,
            TRIP_PATTERN_ID,
            STOP_PATH_ID);
    apc.setArrivalDeparture(ad.create());
    records.add(apc);
    return records;
  }


  public Indices getIndicies(int tripIndex, int stopPathIndex) {
    Block block = getBlock();
    int segmentIndex = 0;
    Indices index = new Indices(block, tripIndex, stopPathIndex,
            segmentIndex);
    return index;
  }

  public AvlReport getAvlReport() {
    return null;
  }

  public Block getBlock() {
    return Core.getInstance().getDbConfig().getBlock(SERVICE_ID, BLOCK_ID);
  }

  public VehicleState getVehicleStateForApc(long avlTime, int tripIndex, int stopPathIndex, int scheduleDeviationSeconds) {
    VehicleState vs = new VehicleState(VEHICLE);
    SpatialMatch spatialMatch = new SpatialMatch(avlTime,
            getBlock(),
            tripIndex, stopPathIndex, 0,
            0.0, 0.0);
    TemporalMatch match = new TemporalMatch(spatialMatch, new TemporalDifference(scheduleDeviationSeconds*Time.MS_PER_SEC));
    vs.setMatch(match);
    int tripStartSeconds = spatialMatch.getTrip().getScheduleTime(0).getTime();
    long tripStartEpoch = Time.getStartOfDay(new Date(avlTime)) + tripStartSeconds * Time.MS_PER_SEC;
    vs.putTripStartTime(tripIndex, tripStartEpoch);
    vs.setLastArrivalTime(avlTime + (scheduleDeviationSeconds * Time.MS_PER_SEC));
    return vs;

  }

  public ArrivalDeparture getHeadwayArrivalDeparture(long referenceTime, int tripIndex, int stopPathIndex, int scheduleDeviationSeconds,
                                                     String headwayVehicle, String stopId) {

    ArrivalDeparture.Builder ad = new ArrivalDeparture.Builder(headwayVehicle,
            referenceTime+(scheduleDeviationSeconds*Time.MS_PER_SEC),
            referenceTime+(scheduleDeviationSeconds*Time.MS_PER_SEC),
            null,
            DIRECTION_ID,
            tripIndex,
            stopPathIndex,
            stopPathIndex,
            true,
            -1,
            1,
            BLOCK_ID,
            HEADWAY_TRIP_ID,
            stopId,
            0,
            0.0f,
            ROUTE_ID,
            ROUTE_ID,
            SERVICE_ID,
            null,
            null,
            TRIP_PATTERN_ID,
            STOP_PATH_ID);

    return ad.create();
  }
}
