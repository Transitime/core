package org.transitime.core.dataCache;

import org.transitime.core.Indices;

public class KalmanErrorCacheKey implements java.io.Serializable {
	
	private String blockId;
	private Integer tripIndex;
	private Integer stopPathIndex;
	private Integer segmentIndex;
	
	String vehicleId;
	/**
	 * Needs to be serializable to add to cache
	 */
	private static final long serialVersionUID = 5029823633051153716L;
	

	public KalmanErrorCacheKey(Indices indices, String vehicleId) {
		super();
		this.blockId=indices.getBlock().getId();
		this.tripIndex=indices.getTripIndex();
		this.stopPathIndex=indices.getStopPathIndex();
		this.segmentIndex=indices.getSegmentIndex();
		this.vehicleId = vehicleId;
	}
	
	/**
	 * @return the blockId
	 */
	public String getBlockId() {
		return blockId;
	}

	/**
	 * @param blockId the blockId to set
	 */
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	/**
	 * @return the tripIndex
	 */
	public int getTripIndex() {
		return tripIndex;
	}

	/**
	 * @param tripIndex the tripIndex to set
	 */
	public void setTripIndex(int tripIndex) {
		this.tripIndex = tripIndex;
	}

	/**
	 * @return the stopPathIndex
	 */
	public int getStopPathIndex() {
		return stopPathIndex;
	}

	/**
	 * @param stopPathIndex the stopPathIndex to set
	 */
	public void setStopPathIndex(int stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}

	/**
	 * @return the segmentIndex
	 */
	public int getSegmentIndex() {
		return segmentIndex;
	}

	/**
	 * @param segmentIndex the segmentIndex to set
	 */
	public void setSegmentIndex(int segmentIndex) {
		this.segmentIndex = segmentIndex;
	}

	/**
	 * @return the vehicleId
	 */
	public String getVehicleId() {
		return vehicleId;
	}

	/**
	 * @param vehicleId the vehicleId to set
	 */
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	/**
	 * @param indices the indices to set
	 */
	public void setIndices(Indices indices) {
		this.blockId=indices.getBlock().getId();
		this.tripIndex=indices.getTripIndex();
		this.stopPathIndex=indices.getStopPathIndex();
		this.segmentIndex=indices.getSegmentIndex();				
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KalmanErrorCacheKey [blockId=" + blockId + ", tripIndex="
				+ tripIndex + ", stopPathIndex=" + stopPathIndex
				+ ", segmentIndex=" + segmentIndex + ", vehicleId=" + vehicleId
				+ "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result
				+ ((segmentIndex == null) ? 0 : segmentIndex.hashCode());
		result = prime * result
				+ ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
		result = prime * result
				+ ((tripIndex == null) ? 0 : tripIndex.hashCode());
		result = prime * result
				+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
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
		KalmanErrorCacheKey other = (KalmanErrorCacheKey) obj;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (segmentIndex == null) {
			if (other.segmentIndex != null)
				return false;
		} else if (!segmentIndex.equals(other.segmentIndex))
			return false;
		if (stopPathIndex == null) {
			if (other.stopPathIndex != null)
				return false;
		} else if (!stopPathIndex.equals(other.stopPathIndex))
			return false;
		if (tripIndex == null) {
			if (other.tripIndex != null)
				return false;
		} else if (!tripIndex.equals(other.tripIndex))
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		return true;
	}
	
	

	
}




