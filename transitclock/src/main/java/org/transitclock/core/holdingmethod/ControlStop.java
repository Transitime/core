package org.transitclock.core.holdingmethod;

public class ControlStop {
	String stopPathIndex;
	String stopId;
	public ControlStop(String combined) {
		if(combined.contains(":"))
		{
			String splits[]=combined.split(":");
			stopPathIndex=splits[1];
			stopId=splits[0];					
		}else
		{
			stopId=combined;
			stopPathIndex=null;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
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
		ControlStop other = (ControlStop) obj;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopPathIndex == null) {
			if (other.stopPathIndex != null)
				return false;
		} else if (!stopPathIndex.equals(other.stopPathIndex))
			return false;
		return true;
	}
	public ControlStop(String stopPathIndex, String stopId) {
		super();
		this.stopPathIndex = stopPathIndex;
		this.stopId = stopId;
	}
	public String getStopPathIndex() {
		return stopPathIndex;
	}
	public void setStopPathIndex(String stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
}
