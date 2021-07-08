package org.transitclock.core.predictiongenerator.kalman;

import org.transitclock.db.structs.Stop;

public class VehicleStopDetail {
 @Override
	public String toString() {
		return "VehicleStopDetail [stop=" + stop + ", time=" + time + ", vehicle=" + vehicle + "]";
	}

	protected Stop stop;
  protected long time=-1L;
  protected Vehicle vehicle;
  protected Long trafficTime = null;

  public VehicleStopDetail(Stop stop, long time, Vehicle vehicle) {
		this.stop = stop;
		this.time = time;
		this.vehicle = vehicle;
 }
	public VehicleStopDetail(Stop stop, long time, Long trafficTime, Vehicle vehicle) {
		this.stop = stop;
		this.time = time;
		this.trafficTime = trafficTime;
		this.vehicle = vehicle;
	}

	/**
 * @return the stop
 */
public Stop getStop() {
	return stop;
}
/**
 * @param stop the stop to set
 */
public void setStop(Stop stop) {
	this.stop = stop;
}
/* (non-Javadoc)
 * @see java.lang.Object#hashCode()
 */
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((stop == null) ? 0 : stop.hashCode());
	result = prime * result + (int) (time ^ (time >>> 32));
	result = prime * result + ((vehicle == null) ? 0 : vehicle.hashCode());
	return result;
}
/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	VehicleStopDetail other = (VehicleStopDetail) obj;
	if (stop == null) {
		if (other.stop != null)
			return false;
	} else if (!stop.equals(other.stop))
		return false;
	if (time != other.time)
		return false;
	if (vehicle == null) {
		if (other.vehicle != null)
			return false;
	} else if (!vehicle.equals(other.vehicle))
		return false;
	return true;
}
/**
 * @return the time
 */
public long getTime() {
	return time;
}
/**
 * @param time the time to set
 */
public void setTime(long time) {
	this.time = time;
}
/**
 * @return the vehicle
 */
public Vehicle getVehicle() {
	return vehicle;
}
/**
 * @param vehicle the vehicle to set
 */
public void setVehicle(Vehicle vehicle) {
	this.vehicle = vehicle;
}

public Long getTrafficTime() {
	return trafficTime;
}
public void setTrafficTime(long trafficTime) { this.trafficTime = trafficTime; }

}
