package org.transitime.core.dataCache;

import org.transitime.core.Indices;

/**
 * @author Sean Og Crudden
 * This is really just Indices without segment.
 */
public class KalmanErrorCacheKey implements java.io.Serializable {
	
	private String blockId;
	private Integer tripIndex;
	private Integer stopPathIndex;
	
		
	/**
	 * Needs to be serializable to add to cache
	 */
	private static final long serialVersionUID = 5029823633051153716L;
	

	public KalmanErrorCacheKey(Indices indices) {
		super();
		this.blockId=indices.getBlock().getId();
		this.tripIndex=indices.getTripIndex();
		this.stopPathIndex=indices.getStopPathIndex();		
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
	 * @param indices the indices to set
	 */
	public void setIndices(Indices indices) {
		this.blockId=indices.getBlock().getId();
		this.tripIndex=indices.getTripIndex();
		this.stopPathIndex=indices.getStopPathIndex();
					
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
		result = prime * result + ((tripIndex == null) ? 0 : tripIndex.hashCode());
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
		KalmanErrorCacheKey other = (KalmanErrorCacheKey) obj;
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
		if (tripIndex == null) {
			if (other.tripIndex != null)
				return false;
		} else if (!tripIndex.equals(other.tripIndex))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "KalmanErrorCacheKey [blockId=" + blockId + ", tripIndex=" + tripIndex + ", stopPathIndex="
				+ stopPathIndex + "]";
	}


	
}




