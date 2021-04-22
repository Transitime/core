package org.transitclock.avl;


import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.Time;

/**
 * APC record parsed from source data.  If validates it will
 * be converted into an ApcReport.
 */
public class ApcParsedRecord {
  private final String messageId;
  private final long time;
  private final long serviceDate;
  private final String driverId;
  private final int odo;
  private final String vehicleId;
  private final int boardings;
  private final int alightings;
  private final int doorOpen;
  private final int doorClose;
  private final int departure;
  private final int arrival;
  private final double lat;
  private final double lon;
  private ArrivalDeparture arrivalDeparture;

  public String getMessageId() {
    return messageId;
  }

  public long getTime() {
    return time;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public String getDriverId() {
    return driverId;
  }

  public int getOdo() {
    return odo;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public int getBoardings() {
    return boardings;
  }

  public int getAlightings() {
    return alightings;
  }

  public int getDoorOpen() {
    return doorOpen;
  }

  public int getDoorClose() {
    return doorClose;
  }

  public int getDeparture() {
    return departure;
  }

  public int getArrival() {
    return arrival;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public ArrivalDeparture getArrivalDeparture() {
    return arrivalDeparture;
  }

  public void setArrivalDeparture(ArrivalDeparture arrivalDeparture) {
    this.arrivalDeparture = arrivalDeparture;
  }

  public long getDoorOpenEpoch() {
    return getEpochTime(doorOpen);
  }

  public long getDoorCloseEpoch() {
    return getEpochTime(doorClose);
  }
  public long getDepartureEpoch() {
    return getEpochTime(departure);
  }
  public long getArrivalEpoch() {
    return getEpochTime(arrival);
  }

  private long getEpochTime(int secondsIntoDay) {
    return serviceDate + secondsIntoDay * Time.SEC_IN_MSECS;
  }



  private ApcParsedRecord() {
    messageId = null;
    time = -1;
    serviceDate = 1;
    driverId = null;
    odo = -1;
    vehicleId = null;
    boardings = -1;
    alightings = -1;
    doorOpen = -1;
    doorClose = -1;
    arrival = -1;
    departure = -1;
    lat = -1.0;
    lon = -1.0;
    arrivalDeparture = null;
  }

  public ApcParsedRecord(String messageId,
                         long time,
                         long serviceDate,
                         String driverId,
                         int odo,
                         String vehicleId,
                         int boardings,
                         int alightings,
                         int doorOpen,
                         int doorClose,
                         int arrival,
                         int departure,
                         double lat,
                         double lon) {
    this.messageId = messageId;
    this.time = time;
    this.serviceDate = serviceDate;
    this.driverId = driverId;
    this.odo = odo;
    this.vehicleId = vehicleId;
    this.boardings = boardings;
    this.alightings = alightings;
    this.doorOpen = doorOpen;
    this.doorClose = doorClose;
    this.arrival = arrival;
    this.departure = departure;
    this.lat = lat;
    this.lon = lon;
    this.arrivalDeparture = null;
  }



}
