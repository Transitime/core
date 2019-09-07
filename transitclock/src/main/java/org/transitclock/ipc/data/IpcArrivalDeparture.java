package org.transitclock.ipc.data;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.beanutils.BeanUtils;
import org.transitclock.core.TemporalDifference;
import org.transitclock.db.structs.ArrivalDeparture;
/**
 * For IPC for obtaining arrival and departure events for a stop that are in the cache.
 *
 * @author Sean Og Crudden
 *
 */
public class IpcArrivalDeparture implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8916143683528781201L;
	
	@XmlAttribute
	private String vehicleId;
	@XmlAttribute
	private Date time;
	@XmlAttribute
	private String stopId;
	@XmlAttribute
	private int gtfsStopSeq;
	@XmlAttribute
	private boolean isArrival;
	@XmlAttribute
	private String tripId;
	@XmlAttribute
	private transient Date avlTime;
	@XmlAttribute
	private transient TemporalDifference scheduledAdherence;
	@XmlAttribute
	private transient String blockId;
	@XmlAttribute
	private transient String routeId;
	@XmlAttribute
	private transient String routeShortName;
	@XmlAttribute
	private transient String serviceId;
	@XmlAttribute
	private String directionId;
	@XmlAttribute
	private transient int tripIndex;
	@XmlAttribute
	private int stopPathIndex;
	@XmlAttribute
	private transient float stopPathLength;
	@XmlAttribute
	private Date freqStartTime;
	
	public IpcArrivalDeparture(ArrivalDeparture arrivalDepature) throws Exception {
		
		this.vehicleId=arrivalDepature.getVehicleId();
		this.time=new Date(arrivalDepature.getTime());
		this.avlTime=arrivalDepature.getAvlTime();
		this.routeId=arrivalDepature.getRouteId();
		this.tripId=arrivalDepature.getTripId();
		this.isArrival=arrivalDepature.isArrival();
		this.stopId=arrivalDepature.getStopId();
		this.stopPathIndex=arrivalDepature.getStopPathIndex();
		
		this.scheduledAdherence=arrivalDepature.getScheduleAdherence();
		this.freqStartTime=arrivalDepature.getFreqStartTime();
		this.directionId=arrivalDepature.getDirectionId();
		this.blockId=arrivalDepature.getBlockId();
		this.serviceId=arrivalDepature.getServiceId();
	}
	
	
	

	public String getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	public int getGtfsStopSeq() {
		return gtfsStopSeq;
	}
	public void setGtfsStopSeq(int gtfsStopSeq) {
		this.gtfsStopSeq = gtfsStopSeq;
	}
	public boolean isArrival() {
		return isArrival;
	}
	public void setArrival(boolean isArrival) {
		this.isArrival = isArrival;
	}
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	public Date getAvlTime() {
		return avlTime;
	}
	public void setAvlTime(Date avlTime) {
		this.avlTime = avlTime;
	}

	public String getBlockId() {
		return blockId;
	}
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getRouteShortName() {
		return routeShortName;
	}
	public void setRouteShortName(String routeShortName) {
		this.routeShortName = routeShortName;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getDirectionId() {
		return directionId;
	}
	public void setDirectionId(String directionId) {
		this.directionId = directionId;
	}
	public int getTripIndex() {
		return tripIndex;
	}
	public void setTripIndex(int tripIndex) {
		this.tripIndex = tripIndex;
	}
	public int getStopPathIndex() {
		return stopPathIndex;
	}
	public void setStopPathIndex(int stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}
	public float getStopPathLength() {
		return stopPathLength;
	}
	public void setStopPathLength(float stopPathLength) {
		this.stopPathLength = stopPathLength;
	}

	public boolean isDeparture() {
		return !isArrival;
	}

	public Date getFreqStartTime() {
		return freqStartTime;
	}

	public void setFreqStartTime(Date freqStartTime) {
		this.freqStartTime = freqStartTime;
	}




	public TemporalDifference getScheduledAdherence() {
		return scheduledAdherence;
	}




	public void setScheduledAdherence(TemporalDifference scheduledAdherence) {
		this.scheduledAdherence = scheduledAdherence;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((avlTime == null) ? 0 : avlTime.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((directionId == null) ? 0 : directionId.hashCode());
		result = prime * result + ((freqStartTime == null) ? 0 : freqStartTime.hashCode());
		result = prime * result + gtfsStopSeq;
		result = prime * result + (isArrival ? 1231 : 1237);
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((routeShortName == null) ? 0 : routeShortName.hashCode());
		result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + stopPathIndex;
		result = prime * result + Float.floatToIntBits(stopPathLength);
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + tripIndex;
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
		IpcArrivalDeparture other = (IpcArrivalDeparture) obj;
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
		if (directionId == null) {
			if (other.directionId != null)
				return false;
		} else if (!directionId.equals(other.directionId))
			return false;
		if (freqStartTime == null) {
			if (other.freqStartTime != null)
				return false;
		} else if (!freqStartTime.equals(other.freqStartTime))
			return false;
		if (gtfsStopSeq != other.gtfsStopSeq)
			return false;
		if (isArrival != other.isArrival)
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
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopPathIndex != other.stopPathIndex)
			return false;
		if (Float.floatToIntBits(stopPathLength) != Float.floatToIntBits(other.stopPathLength))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (tripIndex != other.tripIndex)
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
		return "IpcArrivalDeparture [vehicleId=" + vehicleId + ", time=" + time + ", stopId=" + stopId
				+ ", gtfsStopSeq=" + gtfsStopSeq + ", isArrival=" + isArrival + ", tripId=" + tripId + ", avlTime="
				+ avlTime + ", scheduledAdherence=" + scheduledAdherence + ", blockId=" + blockId + ", routeId="
				+ routeId + ", routeShortName=" + routeShortName + ", serviceId=" + serviceId + ", directionId="
				+ directionId + ", tripIndex=" + tripIndex + ", stopPathIndex=" + stopPathIndex + ", stopPathLength="
				+ stopPathLength + ", freqStartTime=" + freqStartTime + "]";
	}


	
	
}
