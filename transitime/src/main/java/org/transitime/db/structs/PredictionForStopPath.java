package org.transitime.db.structs;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.criterion.Restrictions;
import org.transitime.db.hibernate.HibernateUtils;
/**
 * @author Sean Og Crudden
 * Store the travel time prediction for a stopPath.  
 */
@Entity @DynamicUpdate 
@Table(name="StopPathPredictions",
       indexes = { @Index(name="StopPathPredictionTimeIndex", 
                   columnList="creationTime" ) } )
public class PredictionForStopPath implements Serializable{

	
	private static final long serialVersionUID = -6409934486927225387L;

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime;
	
	@Column	
	private Double predictionTime;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String tripId;
	
	@Column
	private String algorithm;
	
	@Column
	private Integer stopPathIndex;

	public PredictionForStopPath(Date creationTime, Double predictionTimeMilliseconds, String tripId, Integer stopPathIndex, String algorithm) {
		super();
		this.creationTime = creationTime;
		this.predictionTime = predictionTimeMilliseconds;
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;
		this.algorithm=algorithm;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public void setPredictionTime(Double predictionTime) {
		this.predictionTime = predictionTime;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setStopPathIndex(Integer stopPathIndex) {
		this.stopPathIndex = stopPathIndex;
	}

	public Integer getStopPathIndex() {
		return stopPathIndex;
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
		PredictionForStopPath other = (PredictionForStopPath) obj;
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
	@SuppressWarnings("unchecked")
	public static List<PredictionForStopPath> getPredictionForStopPathFromDB (
			Date beginTime, 
			Date endTime,
			String algorithm,
			String tripId,
			Integer stopPathIndex)
	{
		Session session = HibernateUtils.getSession();
		Criteria criteria = session.createCriteria(PredictionForStopPath.class);
		
		if(algorithm!=null)
			criteria.add(Restrictions.eq("algorithm", algorithm));
		if(tripId!=null)
			criteria.add(Restrictions.eq("tripId", algorithm));
		if(stopPathIndex!=null)
			criteria.add(Restrictions.eq("stopPathIndex", stopPathIndex));
		/*		
		if(beginTime!=null)
			criteria.add(Restrictions.gt("creationTime", beginTime));
		if(endTime!=null)
			criteria.add(Restrictions.lt("creationTime", endTime));		
		*/
		return criteria.list();				
	}

	public PredictionForStopPath() {
		super();
		this.creationTime = null;
		this.predictionTime = null;
		this.tripId = null;
		this.stopPathIndex = null;
		this.algorithm=null;						
	}
			
}
