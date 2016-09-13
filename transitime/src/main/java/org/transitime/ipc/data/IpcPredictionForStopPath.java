package org.transitime.ipc.data;

import java.io.Serializable;
import java.util.Date;

import org.transitime.db.structs.PredictionForStopPath;

public class IpcPredictionForStopPath implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4379439564376344615L;


	private final Date creationTime;
	
		
	private final Double predictionTime;
	
	
	private final String tripId;
	
	
	private final String algorithm;
	
	
	private final Integer stopPathIndex;
	
	public IpcPredictionForStopPath(PredictionForStopPath predictionForStopPath) {
		this.tripId=predictionForStopPath.getTripId();
		this.algorithm=predictionForStopPath.getAlgorithm();
		this.stopPathIndex=predictionForStopPath.getStopPathIndex();
		this.predictionTime=predictionForStopPath.getPredictionTime();
		this.creationTime=predictionForStopPath.getCreationTime();
	
	}

	@Override
	public String toString() {
		return "IpcPredictionForStopPath [creationTime=" + creationTime + ", predictionTime=" + predictionTime
				+ ", tripId=" + tripId + ", algorithm=" + algorithm + ", stopPathIndex=" + stopPathIndex + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
		result = prime * result + ((predictionTime == null) ? 0 : predictionTime.hashCode());
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
		IpcPredictionForStopPath other = (IpcPredictionForStopPath) obj;
		if (algorithm == null) {
			if (other.algorithm != null)
				return false;
		} else if (!algorithm.equals(other.algorithm))
			return false;
		if (creationTime == null) {
			if (other.creationTime != null)
				return false;
		} else if (!creationTime.equals(other.creationTime))
			return false;
		if (predictionTime == null) {
			if (other.predictionTime != null)
				return false;
		} else if (!predictionTime.equals(other.predictionTime))
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

	public Date getCreationTime() {
		return creationTime;
	}

	public Double getPredictionTime() {
		return predictionTime;
	}

	public String getTripId() {
		return tripId;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Integer getStopPathIndex() {
		return stopPathIndex;
	}

}
