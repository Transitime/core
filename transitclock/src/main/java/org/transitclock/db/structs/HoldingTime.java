package org.transitclock.db.structs;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
@Entity @DynamicUpdate 
@Table(name="HoldingTimes",
       indexes = { @Index(name="HoldingTimeIndex", 
                   columnList="creationTime" ) } )

/**
 * For persisting a holding time recommendation. 
 *
 * @author Sean Óg Crudden
 *
 */
public class HoldingTime implements Serializable {

	@Override
	public String toString() {
		return "HoldingTime [id=" + id + ", configRev=" + configRev + ", holdingTime=" + holdingTime + ", creationTime="
				+ creationTime + ", vehicleId=" + vehicleId + ", stopId=" + stopId + ", tripId=" + tripId + ", routeId="
				+ routeId + ", arrivalTime=" + arrivalTime + ", arrivalPredictionUsed=" + arrivalPredictionUsed
				+ ", arrivalUsed=" + arrivalUsed + ", hasD1=" + hasD1 + ", numberPredictionsUsed="
				+ numberPredictionsUsed + "]";
	}

	public int getNumberPredictionsUsed() {
		return numberPredictionsUsed;
	}

	public void setNumberPredictionsUsed(int numberPredictionsUsed) {
		this.numberPredictionsUsed = numberPredictionsUsed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (arrivalPredictionUsed ? 1231 : 1237);
		result = prime * result + ((arrivalTime == null) ? 0 : arrivalTime.hashCode());
		result = prime * result + (arrivalUsed ? 1231 : 1237);
		result = prime * result + configRev;
		result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
		result = prime * result + ((holdingTime == null) ? 0 : holdingTime.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoldingTime other = (HoldingTime) obj;
		if (arrivalPredictionUsed != other.arrivalPredictionUsed)
			return false;
		if (arrivalTime == null) {
			if (other.arrivalTime != null)
				return false;
		} else if (!arrivalTime.equals(other.arrivalTime))
			return false;
		if (arrivalUsed != other.arrivalUsed)
			return false;
		if (configRev != other.configRev)
			return false;
		if (creationTime == null) {
			if (other.creationTime != null)
				return false;
		} else if (!creationTime.equals(other.creationTime))
			return false;
		if (holdingTime == null) {
			if (other.holdingTime != null)
				return false;
		} else if (!holdingTime.equals(other.holdingTime))
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

	private static final long serialVersionUID = -8000018800462712153L;

	@Id
	@GeneratedValue(strategy= GenerationType.AUTO, generator="native")
	@GenericGenerator(name = "native", strategy = "native")
	private long id;
	
	// The revision of the configuration data that was being used
	@Column 
	private final int configRev;
	
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date holdingTime;	

	// The time the AVL data was processed and the prediction was created.
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
	@Temporal(TemporalType.TIMESTAMP)
	private final Date arrivalTime;
	
	public Date getArrivalTime() {
		return arrivalTime;
	}

	@Column
	private boolean arrivalPredictionUsed;
	
	@Column
	private boolean arrivalUsed;	
	
	@Column
	private boolean hasD1;
	
	@Column 
	int numberPredictionsUsed;
		
	
	public boolean isHasD1() {
		return hasD1;
	}

	public void setHasD1(boolean hasD1) {
		this.hasD1 = hasD1;
	}

	public Date getHoldingTime() {
		return holdingTime;
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
	
	public boolean isArrivalPredictionUsed() {
		return arrivalPredictionUsed;
	}
	
	public boolean isArrivalUsed() {
		return arrivalUsed;
	}
	public HoldingTime() {
		this.configRev = -1;
		this.holdingTime = null;
		this.creationTime = null;
		this.vehicleId = null;
		this.stopId = null;
		this.tripId = null;
		this.routeId = null;	
		arrivalPredictionUsed=false;
		arrivalUsed=false;
		arrivalTime = null;		
		hasD1 = false;
		numberPredictionsUsed=-1;
	}

	public HoldingTime(Date holdingTime, Date creationTime, String vehicleId, String stopId, String tripId,
			String routeId, boolean arrivalPredictionUsed, boolean arrivalUsed,  Date arrivalTime, boolean hasD1, int numberPredictionsUsed) {
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		this.holdingTime = holdingTime;
		this.creationTime = creationTime;
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		this.tripId = tripId;
		this.routeId = routeId;
		this.arrivalPredictionUsed=arrivalPredictionUsed;
		this.arrivalTime=arrivalTime;
		this.arrivalUsed=arrivalUsed;		
		this.hasD1=hasD1;
		this.numberPredictionsUsed=numberPredictionsUsed;
		
	}
	public HoldingTime(HoldingTime holdingTimeToCopy, Date holdingTime) {
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		this.holdingTime =holdingTime;
		this.creationTime = holdingTimeToCopy.creationTime;
		this.vehicleId = holdingTimeToCopy.vehicleId;
		this.stopId = holdingTimeToCopy.stopId;
		this.tripId = holdingTimeToCopy.tripId;
		this.routeId = holdingTimeToCopy.routeId;
		this.arrivalPredictionUsed=holdingTimeToCopy.arrivalPredictionUsed;
		this.arrivalTime=holdingTimeToCopy.arrivalTime;
		this.arrivalUsed=holdingTimeToCopy.arrivalUsed;		
		this.hasD1=holdingTimeToCopy.hasD1;
		this.numberPredictionsUsed=holdingTimeToCopy.numberPredictionsUsed;
	}
	public Date getTimeToLeave(Date currentTime)
	{
		if(currentTime.after(holdingTime))
		{
			return currentTime;
		}else
		{
			return holdingTime;
		}		
	}
	public boolean leaveStop(Date currentTime)
	{
		if(holdingTime.before(currentTime))
		{
			return true;
		}else
		{
			return false;
		}		
	}

}
