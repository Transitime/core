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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.jcip.annotations.Immutable;

import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For persisting the vehicle state for the vehicle. Can be joined with
 * AvlReport table in order to get additional info for each historic AVL report.
 *
 * @author SkiBu Smith
 *
 */
@Immutable // From jcip.annoations
@Entity 
@DynamicUpdate 
@Table(name="VehicleStates",
       indexes = { @Index(name="VehicleStateAvlTimeIndex", columnList="avlTime" ),
			   		@Index(name="VehicleStateRouteIndex", columnList="routeShortName" )} )
public class VehicleState implements Serializable {
	// vehicleId is an @Id since might get multiple AVL reports
	// for different vehicles with the same avlTime but need a unique
	// primary key.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String vehicleId;
	
	// Need to use columnDefinition to explicitly specify that should use 
	// fractional seconds. This column is an Id since shouldn't get two
	// AVL reports for the same vehicle for the same avlTime.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	@Id
	private final Date avlTime;

	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String blockId;

	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String tripId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String tripShortName;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String routeId;
	
	private static final int ROUTE_SHORT_NAME_MAX_LENGTH = 80;
	@Column(length=ROUTE_SHORT_NAME_MAX_LENGTH)
	private String routeShortName;
	
	// Positive means vehicle early, negative means vehicle late
	@Column
	private final Integer schedAdhMsec;
	
	// A String representing the schedule adherence
	private static final int SCHED_ADH_MAX_LENGTH = 50;
	@Column(length=SCHED_ADH_MAX_LENGTH)
	private final String schedAdh;
	
	@Column
	private final Boolean schedAdhWithinBounds;

	@Column
	private final Boolean isDelayed;

	@Column
	private final Boolean isLayover;

	@Column
	private final Boolean isPredictable;

	@Column
	private final Boolean isWaitStop;

	@Column
	private final Boolean isForSchedBasedPreds;
	
	// Needed because serializable due to Hibernate requirement
	private static final long serialVersionUID = 4621934279490446774L;

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param vehicleState
	 */
	public VehicleState(org.transitclock.core.VehicleState vs) {
		this.vehicleId =
				truncate(vs.getVehicleId(), HibernateUtils.DEFAULT_ID_SIZE);
		this.avlTime =
				vs.getAvlReport() == null ? null : vs.getAvlReport().getDate();
		this.blockId = vs.getBlock() == null ? null : vs.getBlock().getId();
		this.tripId =
				vs.getTrip() == null ? null : truncate(vs.getTrip().getId(),
						HibernateUtils.DEFAULT_ID_SIZE);
		this.tripShortName =
				vs.getTrip() == null ? null : truncate(vs.getTrip()
						.getShortName(), HibernateUtils.DEFAULT_ID_SIZE);
		this.routeId = vs.getRouteId();
		this.routeShortName =
				truncate(vs.getRouteShortName(), ROUTE_SHORT_NAME_MAX_LENGTH);
		this.schedAdhMsec =
				vs.getRealTimeSchedAdh() == null ? null : vs
						.getRealTimeSchedAdh().getTemporalDifference();
		this.schedAdh =
				vs.getRealTimeSchedAdh() == null ? null
						: truncate(vs.getRealTimeSchedAdh().toString(),
								SCHED_ADH_MAX_LENGTH);
		this.schedAdhWithinBounds =
				vs.getRealTimeSchedAdh() == null ? null : vs
						.getRealTimeSchedAdh().isWithinBounds();
		this.isDelayed = vs.isDelayed();
		this.isLayover = vs.isLayover();
		this.isPredictable = vs.isPredictable();
		this.isWaitStop = vs.isWaitStop();
		this.isForSchedBasedPreds = vs.isForSchedBasedPreds();
	}
	
	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private VehicleState() {
		this.vehicleId = null;
		this.avlTime = null;		
		this.blockId = null;
		this.tripId = null;
		this.routeId = null;
		this.routeShortName = null;		
		this.schedAdhMsec = null;
		this.schedAdh = null;
		this.schedAdhWithinBounds = null;
		this.isDelayed = null;
		this.isLayover = null;
		this.isPredictable = null;
		this.isWaitStop = null;
		this.isForSchedBasedPreds = null;
	}

	/**
	 * For making sure that members don't get a value that is longer than
	 * allowed. Truncates string to maxLength if it is too long. This way won't
	 * get a db error if try to store a string that is too long.
	 * 
	 * @param original
	 *            the string to possibly be truncated
	 * @param maxLength
	 *            max length string can have in db
	 * @return possibly truncated version of the original string
	 */
	private String truncate(String original, int maxLength) {
		if (original == null || original.length() <= maxLength) 
			return original;
		
		return original.substring(0, maxLength);
	}
	
	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((avlTime == null) ? 0 : avlTime.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result =
				prime * result
						+ ((isDelayed == null) ? 0 : isDelayed.hashCode());
		result =
				prime
						* result
						+ ((isForSchedBasedPreds == null) ? 0
								: isForSchedBasedPreds.hashCode());
		result =
				prime * result
						+ ((isLayover == null) ? 0 : isLayover.hashCode());
		result =
				prime
						* result
						+ ((isPredictable == null) ? 0 : isPredictable
								.hashCode());
		result =
				prime * result
						+ ((isWaitStop == null) ? 0 : isWaitStop.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result =
				prime
						* result
						+ ((routeShortName == null) ? 0 : routeShortName
								.hashCode());
		result =
				prime * result + ((schedAdh == null) ? 0 : schedAdh.hashCode());
		result =
				prime
						* result
						+ ((schedAdhMsec == null) ? 0 : schedAdhMsec.hashCode());
		result =
				prime
						* result
						+ ((schedAdhWithinBounds == null) ? 0
								: schedAdhWithinBounds.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result =
				prime * result
						+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
		return result;
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleState other = (VehicleState) obj;
		if (avlTime == null) {
			if (other.avlTime != null)
				return false;
		} else if (!avlTime.equals(other.avlTime))
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (isDelayed == null) {
			if (other.isDelayed != null)
				return false;
		} else if (!isDelayed.equals(other.isDelayed))
			return false;
		if (isForSchedBasedPreds == null) {
			if (other.isForSchedBasedPreds != null)
				return false;
		} else if (!isForSchedBasedPreds.equals(other.isForSchedBasedPreds))
			return false;
		if (isLayover == null) {
			if (other.isLayover != null)
				return false;
		} else if (!isLayover.equals(other.isLayover))
			return false;
		if (isPredictable == null) {
			if (other.isPredictable != null)
				return false;
		} else if (!isPredictable.equals(other.isPredictable))
			return false;
		if (isWaitStop == null) {
			if (other.isWaitStop != null)
				return false;
		} else if (!isWaitStop.equals(other.isWaitStop))
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
		if (schedAdh == null) {
			if (other.schedAdh != null)
				return false;
		} else if (!schedAdh.equals(other.schedAdh))
			return false;
		if (schedAdhMsec == null) {
			if (other.schedAdhMsec != null)
				return false;
		} else if (!schedAdhMsec.equals(other.schedAdhMsec))
			return false;
		if (schedAdhWithinBounds == null) {
			if (other.schedAdhWithinBounds != null)
				return false;
		} else if (!schedAdhWithinBounds.equals(other.schedAdhWithinBounds))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		return true;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public Date getAvlTime() {
		return avlTime;
	}

	public String getBlockId() {
		return blockId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public Integer getSchedAdhMsec() {
		return schedAdhMsec;
	}

	public String getSchedAdh() {
		return schedAdh;
	}

	public Boolean getSchedAdhWithinBounds() {
		return schedAdhWithinBounds;
	}

	public Boolean getIsDelayed() {
		return isDelayed;
	}

	public Boolean getIsLayover() {
		return isLayover;
	}

	public Boolean getIsPredictable() {
		return isPredictable;
	}

	public Boolean getIsWaitStop() {
		return isWaitStop;
	}

	public Boolean getIsForSchedBasedPreds() {
		return isForSchedBasedPreds;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public String toString() {
		return "VehicleState [" 
				+ "vehicleId=" + vehicleId 
				+ ", avlTime=" + avlTime
				+ ", blockId=" + blockId 
				+ ", tripId=" + tripId 
				+ ", routeId=" + routeId 
				+ ", routeShortName=" + routeShortName
				+ ", schedAdhMsec=" + schedAdhMsec 
				+ ", schedAdh=" + schedAdh
				+ ", schedAdhWithinBounds=" + schedAdhWithinBounds
				+ ", isDelayed=" + isDelayed 
				+ ", isLayover=" + isLayover
				+ ", isPredictable=" + isPredictable 
				+ ", isWaitStop=" + isWaitStop 
				+ ", isForSchedBasedPreds=" + isForSchedBasedPreds
				+ "]";
	}


}
