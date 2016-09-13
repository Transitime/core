package org.transitime.core.predictiongenerator.kalman;

public class TripSegment {
	VehicleStopDetail origin;
	VehicleStopDetail destination;
	
	
	public TripSegment(VehicleStopDetail origin, VehicleStopDetail destination) {
		super();
		this.origin = origin;
		this.destination = destination;
	}
	public long getDuration()
	{
		if(this.origin!=null && this.getDestination()!=null )
			return destination.getTime()-origin.getTime();
		else
			return -1;
			
	}
	/**
	 * @return the origin
	 */
	public VehicleStopDetail getOrigin() {
		return origin;
	}
	
	/**
	 * @return the destination
	 */
	public VehicleStopDetail getDestination() {
		return destination;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
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
		TripSegment other = (TripSegment) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "TripSegment [origin=" + origin + ", destination=" + destination + ", duration="+(destination.getTime()-origin.getTime())+"]";
	}
	
	
}
