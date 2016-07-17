package org.transitime.core.predictiongenerator.kalman;

public class TripSegment {
	VehicleStopDetail origin;
	VehicleStopDetail destination;
	double e=-1;
	/**
	 * @return the e
	 */
	public double getE() {
		return e;
	}
	/**
	 * @param e the e to set
	 */
	public void setE(double e) {
		this.e = e;
	}
	public TripSegment(VehicleStopDetail origin, VehicleStopDetail destination,
			double e) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.e = e;
	}
	public TripSegment(VehicleStopDetail origin, VehicleStopDetail destination) {
		super();
		this.origin = origin;
		this.destination = destination;
	}
	/**
	 * @return the origin
	 */
	public VehicleStopDetail getOrigin() {
		return origin;
	}
	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(VehicleStopDetail origin) {
		this.origin = origin;
	}
	/**
	 * @return the destination
	 */
	public VehicleStopDetail getDestination() {
		return destination;
	}
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(VehicleStopDetail destination) {
		this.destination = destination;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		long temp;
		temp = Double.doubleToLongBits(e);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
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
		TripSegment other = (TripSegment) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (Double.doubleToLongBits(e) != Double.doubleToLongBits(other.e))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		return true;
	}
	
}
