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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;
import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.ipc.data.Prediction;

/**
 * For persisting a prediction.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate 
@Table(name="Predictions") 
public class DbPrediction implements Serializable {
		
	// Need an ID but using a regular column doesn't really make
	// sense. So use an auto generated one.
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	// The revision of the configuration data that was being used
	@Column 
	final int configRev;

	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date predictionTime;
	
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date creationTime;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String vehicleId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;
	
	@Column
	private final boolean affectedByWaitStop;
	
	@Column
	private final boolean isArrival;

	// Needed because Hibernate objects must be serializable
	private static final long serialVersionUID = 3966430062434375435L;

	/********************** Member Functions **************************/

	/**
	 * Constructor called when creating a DbPrediction object to be stored
	 * in database.
	 * 
	 * @param predictionTime
	 * @param creationTime
	 * @param vehicleId
	 * @param stopId
	 * @param tripId
	 * @param routeId
	 * @param affectedByWaitStop
	 * @param isArrival
	 */
	public DbPrediction(long predictionTime, long creationTime, 
			String vehicleId, String stopId, String tripId, String routeId, 
			boolean affectedByWaitStop, boolean isArrival) {
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		this.predictionTime = new Date(predictionTime);
		this.creationTime = new Date(creationTime);
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		this.tripId = tripId;
		this.routeId = routeId;
		this.affectedByWaitStop = affectedByWaitStop;
		this.isArrival = isArrival;
	}
	
	public DbPrediction(Prediction prediction) {
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		this.predictionTime = new Date(prediction.getTime());
		this.creationTime = new Date(prediction.getCreationTime());
		this.vehicleId = prediction.getVehicleId();
		this.stopId = prediction.getStopId();
		this.tripId = prediction.getTripId();
		this.routeId = prediction.getRouteId();
		this.affectedByWaitStop = prediction.isAffectedByWaitStop();
		this.isArrival = prediction.isArrival();	
	}
	
	/**
	 * Hibernate requires a no-arg constructor for reading objects
	 * from database.
	 */
	protected DbPrediction() {
		this.configRev = -1;
		this.predictionTime = null;
		this.creationTime = null;
		this.vehicleId = null;
		this.stopId = null;
		this.tripId = null;
		this.routeId = null;
		this.affectedByWaitStop = false;
		this.isArrival = false;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (affectedByWaitStop ? 1231 : 1237);
		result = prime * result
				+ ((creationTime == null) ? 0 : creationTime.hashCode());
		result = prime * result + configRev;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (isArrival ? 1231 : 1237);
		result = prime * result
				+ ((predictionTime == null) ? 0 : predictionTime.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result
				+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
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
		DbPrediction other = (DbPrediction) obj;
		if (affectedByWaitStop != other.affectedByWaitStop)
			return false;
		if (creationTime == null) {
			if (other.creationTime != null)
				return false;
		} else if (!creationTime.equals(other.creationTime))
			return false;
		if (configRev != other.configRev)
			return false;
		if (id != other.id)
			return false;
		if (isArrival != other.isArrival)
			return false;
		if (predictionTime == null) {
			if (other.predictionTime != null)
				return false;
		} else if (!predictionTime.equals(other.predictionTime))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
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

	@Override
	public String toString() {
		return "DbPrediction [" 
				+ "predictionTime=" + predictionTime
				+ ", creationTime=" + creationTime 
				+ ", vehicleId=" + vehicleId
				+ ", stopId=" + stopId 
				+ ", tripId=" + tripId 
				+ ", routeId=" + routeId 
				+ ", affectedByWaitStop=" + affectedByWaitStop
				+ ", isArrival=" + isArrival 
				+ "]";
	}

	public Date getPredictionTime() {
		return predictionTime;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public String getStopId() {
		return stopId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getRouteId() {
		return routeId;
	}

	public boolean isAffectedByWaitStop() {
		return affectedByWaitStop;
	}

	public boolean isArrival() {
		return isArrival;
	}
}
