package org.transitclock.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * GTFS Extension for Vehicle Configuration data.
 */
public class GtfsVehicle extends CsvBase {

  private final String vehicleId; // 2050
  private final String vehicleDescription; // 40LFBUS
  private final int seatedCapacity; // 38
  private final int standingCapacity; // 38
  private final int doorCount; // 2
  private final String doorWidth; // normal
  private final int lowFloor; // 1
  private final int bikeCapacity; // 2
  private final String wheelchairAccess; // ramp

  public GtfsVehicle(String vehicleId, String vehicleDescription, int seatedCapacity,
                     int standingCapacity, int doorCount, String doorWidth,
                     int lowFloor, int bikeCapacity, String wheelchairAccess) {
    this.vehicleId = vehicleId;
    this.vehicleDescription = vehicleDescription;
    this.seatedCapacity = seatedCapacity;
    this.standingCapacity = standingCapacity;
    this.doorCount = doorCount;
    this.doorWidth = doorWidth;
    this.lowFloor = lowFloor;
    this.bikeCapacity = bikeCapacity;
    this.wheelchairAccess = wheelchairAccess;
  }

  public GtfsVehicle(CSVRecord record, boolean supplemental, String filename) {
    super(record, supplemental, filename);
    vehicleId = getRequiredUnlessSupplementalValue(record, "vehicle_id");
    vehicleDescription = getRequiredUnlessSupplementalValue(record, "vehicle_description");
    seatedCapacity = getIntValueWithDefault(record, "seated_capacity", -999);
    standingCapacity = getIntValueWithDefault(record, "standing_capacity", -999);
    doorCount = getIntValueWithDefault(record, "door_count", -999);
    doorWidth = getOptionalValue(record, "door_width");
    lowFloor = getIntValueWithDefault(record, "low_floor", -999);
    bikeCapacity = getIntValueWithDefault(record, "bike_capacity", -999);
    wheelchairAccess = getOptionalValue(record, "wheelchair_access");
  }

  public String getVehicleId() { return vehicleId; }
  public String getVehicleDescription() { return vehicleDescription; }
  public int getSeatedCapacity() { return seatedCapacity; }
  public int getStandingCapacity() { return standingCapacity; }
  public int getDoorCount() { return doorCount; }
  public String getDoorWidth() { return doorWidth; }
  public int getLowFloor() { return lowFloor; }
  public int getBikeCapacity() { return bikeCapacity; }
  public String getWheelchairAccess() { return wheelchairAccess; }

}
