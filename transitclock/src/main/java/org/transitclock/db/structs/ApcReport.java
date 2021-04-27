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
package org.transitclock.db.structs;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;

import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.Time;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * represents an automated passenger count update.
 */
@Entity(name="ApcReport")
@DynamicUpdate
@Table(name="ApcReport")
public class ApcReport implements Serializable {

  @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
  @Id
  private final String messageId;
  @Column
  private final long time;
  @Column
  private final long serviceDate;
  @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
  private final String driverId;
  @Column
  private final int odo;
  @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
  private final String vehicleId;
  @Column
  private final int boardings;
  @Column
  private final int alightings;
  @Column
  private final int doorOpen;
  @Column
  private final int doorClose;
  @Column
  private final int departure;
  @Column
  private final int arrival;
  @Column
  private final double lat;
  @Column
  private final double lon;

  // historically hibernate 1-1 mappings are not trusted to update
  // as this object is immutable we use it here safely
  @OneToOne(fetch = FetchType.EAGER)
  // hibernate link to the arrival departure the apc record corresponds to
  private final ArrivalDeparture arrivalDeparture;

  private ApcReport() {
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

  public ApcReport(String messageId,
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
                   double lon,
                   ArrivalDeparture ad) {
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
    this.arrivalDeparture = ad;
  }

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
  public long getDoorOpenEpoch() {
    return getEpochTime(doorOpen);
  }

  public int getDoorClose() {
    return doorClose;
  }
  public long getDoorCloseEpoch() {
    return getEpochTime(doorClose);
  }

  public int getDeparture() {
    return departure;
  }
  public long getDepartureEpoch() {
    return getEpochTime(departure);
  }

  public int getArrival() {
    return arrival;
  }
  public long getArrivalEpoch() {
    return getEpochTime(arrival);
  }

  public double getLat() { return lat; }

  public double getLon() { return lon; }

  private long getEpochTime(int secondsIntoDay) {
    return serviceDate + secondsIntoDay * Time.SEC_IN_MSECS;
  }

  public ArrivalDeparture getArrivalDeparture() {
    return arrivalDeparture;
  }

  public static List<ApcReport> getApcReportsFromDb(String projectId, Date beginTime, Date endTime) {
    Session session = HibernateUtils.getSession(projectId);
    String hql = "FROM ApcReport " +
            " WHERE time >= :beginDate " +
            " AND time < :endDate";
    Query query = session.createQuery(hql);
    query.setTimestamp("beginDate", beginTime);
    query.setTimestamp("endDate", endTime);

    try {
      return query.list();
    } finally {
      session.close();
    }
  }

  @Override
  public String toString() {
  return "apc["
          + "vehicleId=" + vehicleId
          + ",time=" + new Date(time)
          + ",id=" + this.messageId
          + ",ons=" + getBoardings()
          + ",offs=" + getAlightings()
  + "]";
  }

}
