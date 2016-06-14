package org.transitime.core.dataCache;

import org.transitime.core.Indices;

public class HistoricalAverageCacheKey implements java.io.Serializable {
	
	private String blockId;
	private String tripId;
	private Integer stopPathIndex;
	
	
	public HistoricalAverageCacheKey(Indices indices) {
		super();
		this.blockId=indices.getBlock().getId();
		
		this.stopPathIndex=indices.getStopPathIndex();
		
		int tripIndex = indices.getTripIndex();
		
		this.tripId=indices.getBlock().getTrip(tripIndex).getId();
		
	}
	@Override
	public String toString() {
		return "HistoricalAverageCacheKey [blockId=" + blockId + ", tripId=" + tripId + ", stopPathIndex="
				+ stopPathIndex + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
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
		HistoricalAverageCacheKey other = (HistoricalAverageCacheKey) obj;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (stopPathIndex == null) {
			if (other.stopPathIndex != null)
				return false;
		} else if (!stopPathIndex.equals(other.stopPathIndex))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}
	public HistoricalAverageCacheKey(String blockId, String tripId, Integer stopPathIndex) {
		super();
		this.blockId = blockId;
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;					
	}
				
	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public Integer getStopPathIndex() {
		return stopPathIndex;
	}

	public void setStopPathIndex(Integer stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8359136555230359690L;
					
}




