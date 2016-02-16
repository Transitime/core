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
package org.transitime.db.structs;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;

/**
 * For storing a measured arrival time so that can see if measured arrival time
 * via GPS is accurate.
 * 
 * @author Michael Smith
 *
 */
@Entity 
@DynamicUpdate
@Table(name="MeasuredArrivalTimes",
indexes = { @Index(name="MeasuredArrivalTimesIndex", 
               columnList="time" ) } )
public class MeasuredArrivalTime implements Serializable {
	@Id 
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private final Date time;

	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String stopId;

	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String routeId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String routeShortName;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String directionId;
		
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String headsign;

	private static final long serialVersionUID = 8311644168019398357L;

	/**
	 * Constructor called when creating an MeasuredArrivalTime object to be
	 * stored in db.
	 * 
	 * @param time
	 * @param stopId
	 * @param routeId
	 * @param routeShortName
	 * @param directionId
	 * @param headsign
	 */
	public MeasuredArrivalTime(Date time, String stopId, String routeId,
			String routeShortName, String directionId, String headsign) {
		this.time = time;
		this.stopId = stopId;
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.directionId = directionId;
		this.headsign = headsign;
	}

	/**
	 * Hibernate requires a no-arg constructor for reading objects
	 * from database.
	 */
	protected MeasuredArrivalTime() {
		this.time = null;
		this.stopId = null;
		this.routeId = null;
		this.routeShortName = null;
		this.directionId = null;
		this.headsign = null;
	}

	/**
	 * Returns the SQL to save the object into database. Usually Hibernate is
	 * used because such data is stored by the core system. But
	 * MeasuredArrivalTime objects are written by the website, which doesn't use
	 * Hibernate to write objects since it has to be able to talk with any db.
	 * 
	 * @return SQL to store the object
	 */
	public String getUpdateSql() {
		return "INSERT INTO MeasuredArrivalTimes ("
				+ "time, stopId, routeId, routeShortName, directionId, headsign) "
				+ "VALUES('" + time + "', '" 
				+ stopId + "', '"
				+ routeId + "', '"
				+ routeShortName + "', '"
				+ directionId + "', '"
				+ headsign + "'"
				+ ");";
	}
	
	/**
	 * Because using a composite Id Hibernate wants this member.
	 */	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
				prime * result
						+ ((directionId == null) ? 0 : directionId.hashCode());
		result =
				prime * result + ((headsign == null) ? 0 : headsign.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result =
				prime
						* result
						+ ((routeShortName == null) ? 0 : routeShortName
								.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeasuredArrivalTime other = (MeasuredArrivalTime) obj;
		if (directionId == null) {
			if (other.directionId != null)
				return false;
		} else if (!directionId.equals(other.directionId))
			return false;
		if (headsign == null) {
			if (other.headsign != null)
				return false;
		} else if (!headsign.equals(other.headsign))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (routeShortName == null) {
			if (other.routeShortName != null)
				return false;
		} else if (!routeShortName.equals(other.routeShortName))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MeasuredArrivalTime ["
				+ "time=" + time 
				+ ", stopId=" + stopId
				+ ", routeId=" + routeId 
				+ ", routeShortName=" + routeShortName
				+ ", directionId=" + directionId 
				+ ", headsign=" + headsign
				+ "]";
	}
	
	
}
