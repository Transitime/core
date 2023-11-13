/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.db.structs;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import net.jcip.annotations.Immutable;

import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.classic.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.SpatialMatch.MatchType;
import org.transitclock.core.TemporalMatch;
import org.transitclock.core.VehicleState;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.Geo;
import org.transitclock.utils.IntervalTimer;

/**
 * For persisting the match for the vehicle. This data is later used
 * for determining expected travel times. The key/IDs for the table
 * are vehicleId and the AVL avlTime so that the Match data can easily
 * be joined with AvlReport data to get additional information.
 * <p>
 * Serializable since Hibernate requires such.
 * <p>
 * Implements Lifecycle so that can have the onLoad() callback be called when
 * reading in data so that can intern() member strings. In order to do this the
 * String members could not be declared as final since they are updated after
 * the constructor is called. 
 *
 * @author SkiBu Smith
 *
 */
@Immutable // From jcip.annoations
@Entity 
@DynamicUpdate 
@Table(name="Matches",
       indexes = { @Index(name="AvlTimeIndex", 
                          columnList="avlTime" ) } )
public class Match implements Lifecycle, Serializable {

	// vehicleId is an @Id since might get multiple AVL reports
	// for different vehicles with the same avlTime but need a unique
	// primary key.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private String vehicleId;
	
	// Need to use columnDefinition to explicitly specify that should use 
	// fractional seconds. This column is an Id since shouldn't get two
	// AVL reports for the same vehicle for the same avlTime.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	@Id
	private final Date avlTime;

	// So that know which configuration was being used when this data point 
	// was created
	@Column
	private final int configRev;
	
	// So that know which service type was used when this data point was created
	@Column
	private String serviceId;
	
	// Not truly needed because currently using only trip info for generating
	// travel times, which is the main use of Match data from the db.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String blockId;
	
	// Creating travel times on a trip by trip basis so this element is 
	// important.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String tripId;
	
	// Important because generating travel times on a per stop path basis
	@Column
	private final int stopPathIndex;
	
	// Not currently needed. Added for possible future uses of Match
	@Column
	private final int segmentIndex;
	
	// Not currently needed. Added for possible future uses of Match
	@Column
	private final float distanceAlongSegment;

	// The distanceAlongStopPath is the important item since travel times are
	// based on dividing up the stop path into travel time paths. These travel
	// time paths are independent of the path segments.
	@Column
	private final float distanceAlongStopPath;
	
	// Whether vehicle is considered to be at a stop. Especially useful so
	// can filter out atStop matches when determining travel times since
	// instead using arrival/departure times for that situation.
	@Column
	private final boolean atStop;

	@Column
	private SpatialMatch.MatchType type;
	
	
	// Needed because serializable due to Hibernate requirement
	private static final long serialVersionUID = -7582135605912244678L;

	private static final Logger logger = 
			LoggerFactory.getLogger(Match.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param vehicleState
	 */
	public Match(VehicleState vehicleState) {
		this.vehicleId = vehicleState.getVehicleId();
		this.avlTime = vehicleState.getAvlReport().getDate();
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		this.serviceId = vehicleState.getBlock().getServiceId();
		this.blockId = vehicleState.getBlock().getId();
		
		TemporalMatch lastMatch = vehicleState.getMatch();
		this.tripId = lastMatch!=null ? lastMatch.getTrip().getId() : null;
		this.stopPathIndex = lastMatch!=null ? lastMatch.getStopPathIndex() : -1;
		this.segmentIndex = lastMatch!=null ? lastMatch.getSegmentIndex() : -1;
		this.distanceAlongSegment = 
				(float) (lastMatch!=null ? lastMatch.getDistanceAlongSegment() : 0.0);
		this.distanceAlongStopPath =
				(float) (lastMatch!=null ? lastMatch.getDistanceAlongStopPath() : 0.0);
		this.atStop = vehicleState.getMatch().isAtStop();
		this.type=lastMatch.getType();
		
		// Log each creation of a Match to the match.log log file
		logger.info(this.toString());
	}

	/**
	 * Hibernate requires a no-args constructor for reading data.
	 * So this is an experiment to see what can be done to satisfy
	 * Hibernate but still have an object be immutable. Since
	 * this constructor is only intended to be used by Hibernate
	 * is is declared protected, since that still works. That way
	 * others won't accidentally use this inappropriate constructor.
	 * And yes, it is peculiar that even though the members in this
	 * class are declared final that Hibernate can still create an
	 * object using this no-args constructor and then set the fields.
	 * Not quite as "final" as one might think. But at least it works.
	 */
	protected Match() {
		this.vehicleId = null;
		this.avlTime = null;
		this.configRev = -1;
		this.serviceId = null;
		this.blockId = null;
		this.tripId = null;
		this.stopPathIndex = -1;
		this.segmentIndex = -1;
		this.distanceAlongSegment = Float.NaN;	
		this.distanceAlongStopPath = Float.NaN;
		this.atStop = false;
		this.type= MatchType.TRANSITCLOCK;
	}

	public SpatialMatch.MatchType getType() {
		return type;
	}

	public void setType(SpatialMatch.MatchType type) {
		this.type = type;
	}

	private Match(String vehicleId,
					long avlTime,
					int configRev,
					String serviceId,
					String blockId,
					String tripId,
					int stopPathIndex,
					int segmentIndex,
					float distanceAlongSegment,
					float distanceAlongStopPath,
					boolean atStop) {
		this.vehicleId = vehicleId;
		this.avlTime = new Date(avlTime);
		this.configRev = configRev;
		this.serviceId = serviceId;
		this.blockId = blockId;
		this.tripId = tripId;
		this.stopPathIndex = stopPathIndex;
		this.segmentIndex = segmentIndex;
		this.distanceAlongSegment = distanceAlongSegment;
		this.distanceAlongStopPath = distanceAlongStopPath;
		this.atStop = atStop;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (atStop ? 1231 : 1237);
		result = prime * result + ((avlTime == null) ? 0 : avlTime.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + configRev;
		result = prime * result + Float.floatToIntBits(distanceAlongSegment);
		result = prime * result + Float.floatToIntBits(distanceAlongStopPath);
		result = prime * result + segmentIndex;
		result =
				prime * result
						+ ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + stopPathIndex;
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result =
				prime * result
						+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
		return result;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Match other = (Match) obj;
		if (atStop != other.atStop)
			return false;
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
		if (configRev != other.configRev)
			return false;
		if (Float.floatToIntBits(distanceAlongSegment) != Float
				.floatToIntBits(other.distanceAlongSegment))
			return false;
		if (Float.floatToIntBits(distanceAlongStopPath) != Float
				.floatToIntBits(other.distanceAlongStopPath))
			return false;
		if (segmentIndex != other.segmentIndex)
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (stopPathIndex != other.stopPathIndex)
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
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
		return "Match ["
				+ "vehicleId=" + vehicleId 
				+ ", avlTime=" + avlTime
				+ ", configRev=" + configRev
				+ ", serviceId=" + serviceId
				+ ", blockId=" + blockId 
				+ ", tripId=" + tripId
				+ ", stopPathIndex=" + stopPathIndex 
				+ ", segmentIndex=" + segmentIndex 
				+ ", distanceAlongSegment="	+ Geo.distanceFormat(distanceAlongSegment)
				+ ", distanceAlongStopPath=" + Geo.distanceFormat(distanceAlongStopPath)
				+ ", atStop=" + atStop
				+ "]";
	}
	
	/**
	 * Allows batch retrieval of Match data from database. This is likely the
	 * best way to read in large amounts of data.
	 * 
	 * @param projectId
	 * @param beginTime
	 * @param endTime
	 * @param sqlClause
	 *            The clause is added to the SQL for retrieving the
	 *            arrival/departures. Useful for ordering the results. Can be
	 *            null.
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public static List<Match> getMatchesFromDb(
			String projectId, Date beginTime, Date endTime, 
			String sqlClause,
			final Integer firstResult, final Integer maxResults) {
		IntervalTimer timer = new IntervalTimer();

		// Get the database session. This is supposed to be pretty light weight
		Session session = HibernateUtils.getSession(projectId, false);

		// Create the query. Table name is case sensitive and needs to be the
		// class name instead of the name of the db table.
		String hql = "FROM Match " +
				"    WHERE avlTime between :beginDate " +
				"      AND :endDate";
		if (sqlClause != null)
			hql += " " + sqlClause;
		Query query = session.createQuery(hql);
		
		// Set the parameters for the query
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);
		
		if (firstResult != null) {
		// Only get a batch of data at a time
			query.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		
		try {
			@SuppressWarnings("unchecked")
			List<Match> matches = query.list();
			logger.debug("Getting matches from database took {} msec",
					timer.elapsedMsec());
			return matches;
		} catch (HibernateException e) {
			// Log error to the Core logger
			Core.getLogger().error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}

	public static Long getMatchesCountFromDb(
			String projectId, Date beginTime, Date endTime, 
			String sqlClause) {
		IntervalTimer timer = new IntervalTimer();

		// Get the database session. This is supposed to be pretty light weight
		Session session = HibernateUtils.getSession(projectId, false);

		// Create the query. Table name is case sensitive and needs to be the
		// class name instead of the name of the db table.
		String hql = "Select count(*) FROM Match " +
				"    WHERE avlTime >= :beginDate " +
				"      AND avlTime < :endDate";
		if (sqlClause != null)
			hql += " " + sqlClause;
		Query query = session.createQuery(hql);
		
		// Set the parameters for the query
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);
		
		Long count = null;
		
		try {
			count = (Long) query.uniqueResult();
			logger.debug("Getting matches from database took {} msec",
					timer.elapsedMsec());
			return count;
		} catch (HibernateException e) {
			// Log error to the Core logger
			Core.getLogger().error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}

	
	public String getVehicleId() {
		return vehicleId;
	}

	public Date getDate() {
		return avlTime;
	}

	public long getTime() {
		return avlTime.getTime();
	}
	
	public int getConfigRev() {
		return configRev;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public String getBlockId() {
		return blockId;
	}

	public String getTripId() {
		return tripId;
	}

	public int getStopPathIndex() {
		return stopPathIndex;
	}

	public int getSegmentIndex() {
		return segmentIndex;
	}

	public float getDistanceAlongSegment() {
		return distanceAlongSegment;
	}
	
	public float getDistanceAlongStopPath() {
		return distanceAlongStopPath;
	}
	
	/**
	 * Returns true if vehicle is at or near a stop. 
	 */
	public boolean isAtStop() {
		return atStop;
	}

	/**
	 * Callback due to implementing Lifecycle interface. Used to compact
	 * string members by interning them.
	 */
	@Override
	public void onLoad(Session s, Serializable id) throws CallbackException {
		if (vehicleId != null)
			vehicleId = vehicleId.intern();
		if (tripId != null)
			tripId = tripId.intern();
		if (blockId != null)
			blockId = blockId.intern();
		if (serviceId != null)
			serviceId = serviceId.intern();
	}
	
	/**
	 * Implemented due to Lifecycle interface being implemented. Not actually
	 * used.
	 */
	@Override
	public boolean onSave(Session s) throws CallbackException {
		return Lifecycle.NO_VETO;
	}

	/**
	 * Implemented due to Lifecycle interface being implemented. Not actually
	 * used.
	 */
	@Override
	public boolean onUpdate(Session s) throws CallbackException {
		return Lifecycle.NO_VETO;
	}

	/**
	 * Implemented due to Lifecycle interface being implemented. Not actually
	 * used.
	 */
	@Override
	public boolean onDelete(Session s) throws CallbackException {
		return Lifecycle.NO_VETO;
	}

	public static class Builder {
		String vehicleId;
		long avlTime;
		int configRev;
		String serviceId;
		String blockId;
		String tripId;
		int stopPathIndex;
		int segmentIndex;
		float distanceAlongSegment;
		float distanceAlongStopPath;
		boolean atStop;

		public Builder(String vehicleId,
									 long avlTime,
									 int configRev,
									 String serviceId,
									 String blockId,
									 String tripId,
									 int stopPathIndex,
									 int segmentIndex,
									 float distanceAlongSegment,
									 float distanceAlongStopPath,
									 boolean atStop) {
			this.vehicleId = vehicleId;
			this.avlTime = avlTime;
			this.configRev = configRev;
			this.serviceId = serviceId;
			this.blockId = blockId;
			this.tripId = tripId;
			this.stopPathIndex = stopPathIndex;
			this.segmentIndex = segmentIndex;
			this.distanceAlongSegment = distanceAlongSegment;
			this.distanceAlongStopPath = distanceAlongStopPath;
			this.atStop = atStop;
		}

		public Match create() {
			return new Match(vehicleId,
							avlTime,
							configRev,
							serviceId,
							blockId,
							tripId,
							stopPathIndex,
							segmentIndex,
							distanceAlongSegment,
							distanceAlongStopPath,
							atStop);
		}
	}
}
