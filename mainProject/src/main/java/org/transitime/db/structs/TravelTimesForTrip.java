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
package org.transitime.db.structs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Index;
import org.transitime.db.hibernate.HibernateUtils;


/**
 * Keeps track of travel times for a trip. Can be shared amongst
 * trips if the travel times are similar. But need separate travel
 * times for every trip pattern.
 * 
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="TravelTimesForTrips")
@org.hibernate.annotations.Table(appliesTo = "TravelTimesForTrips", 
indexes = { @Index(name="travelTimesRevIndex", 
                   columnNames={"travelTimesRev"} ) } )
public class TravelTimesForTrip implements Serializable {

	// Need a generated ID because trying to share TravelTimesForStopPath 
	// objects because having a separate set for each trip would be too much. 
	// But will usually still have a few per path and trip pattern. Therefore 
	// also need the generated ID.
	@Column 
	@Id 
	@GeneratedValue 
	private Integer id;

	// Need configRev for the configuration so that when old configurations 
	// cleaned out can also get rid of old travel times.
	@Column
	private final int configRev;
	
	// Each time update travel times it gets a new travel time rev. This
	// way can compare travel times with previous revisions. Probably only need
	// to keep the previous travel time rev around for comparison but by
	// using an integer for the rev all of the can be kept. 
	@Column
	private final int travelTimesRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripPatternId;
	
	// So know which trip these travel times were created for. Useful
	// for logging statements. Used when creating travel times based
	// on schedule.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripCreatedForId;
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(name="TravelTimesForTrip_to_TravelTimesForPath_joinTable")
	@OrderColumn(name="listIndex")
	private final List<TravelTimesForStopPath> travelTimesForStopPaths = 
			new ArrayList<TravelTimesForStopPath>();

	// Hibernate requires that this class be serializable if it has multiple 
	// column IDs so doing it in case have multiple ID columns in future.
	private static final long serialVersionUID = -5208608077900300605L;


	/********************** Member Functions **************************/

	/**
	 * Simple constructor.
	 * 
	 * @param configRev
	 * @param travelTimesRev
	 * @param trip
	 *            So can determine trip pattern ID and set which trip this was
	 *            created for.
	 */
	public TravelTimesForTrip(int configRev, int travelTimesRev, Trip trip) {
		this.configRev = configRev;
		this.travelTimesRev = travelTimesRev;
		this.tripPatternId = trip.getTripPattern().getId();
		this.tripCreatedForId = trip.getId();
	}
	
	/**
	 * Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private TravelTimesForTrip() {
		this.configRev = -1;
		this.travelTimesRev= -1;
		this.tripPatternId = null;
		this.tripCreatedForId = null;
	}
	
	/**
	 * For when creating a new TravelTimesForTrip. This method is for
	 * adding each TravelTimesForStopPath.
	 * 
	 * @param travelTimesForPath
	 */
	public void add(TravelTimesForStopPath travelTimesForPath) {
		travelTimesForStopPaths.add(travelTimesForPath);
	}

	/**
	 * Returns Map keyed by tripPatternId of Lists of TravelTimesForTrip. Since
	 * there are usually multiple trips per trip pattern the Map contains a List
	 * of TravelTimesForTrip instead of just a single one.
	 * 
	 * @param projectId
	 * @param travelTimesRev
	 * @return Map keyed by tripPatternId of Lists of TripPatterns
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, List<TravelTimesForTrip>> getTravelTimesForTrips(
			String projectId, int travelTimesRev) 
			throws HibernateException {
		// Create the db session
		Session session = HibernateUtils.getSession(projectId);

		// Get List of all TravelTimesForTrip for the specified rev
		String hql = "FROM TravelTimesForTrip " +
				"    WHERE travelTimesRev = :travelTimesRev";
		Query query = session.createQuery(hql);
		query.setInteger("travelTimesRev", travelTimesRev);
		List<TravelTimesForTrip> allTravelTimes;
		try {
			allTravelTimes = query.list();
		} catch (Exception e) {
			throw e;
		} finally {
			// Always close the session
			session.close();
		}
		
		// Now create the map and return it
		Map<String, List<TravelTimesForTrip>> map = 
				new HashMap<String, List<TravelTimesForTrip>>();
		for (TravelTimesForTrip travelTimes : allTravelTimes) {
			// Get the List to add the travelTimes to
			String tripPatternId = travelTimes.getTripPatternId();
			List<TravelTimesForTrip> listForTripPattern = map.get(tripPatternId);
			if (listForTripPattern == null) {
				listForTripPattern = new ArrayList<TravelTimesForTrip>();
				map.put(tripPatternId, listForTripPattern);				
			}
			
			// Add the travelTimes to the List
			listForTripPattern.add(travelTimes);			
		}
		
		// Return the map containing all the travel times
		return map;
	}

	/**
	 * Returns true if every single stop path travel time is schedule based.
	 * @return
	 */
	public boolean purelyScheduleBased() {
		for (TravelTimesForStopPath times : travelTimesForStopPaths) {
			if (!times.getHowSet().isScheduleBased())
				return false;
		}
		
		// All of them travel times are schedule based so return true
		return true;
	}
	
	/**
	 * Needed because of Hibernate and also because want to cache 
	 * TravelTimesForTrip and to do so need to store the objects
	 * as keys in a map and need hashCode() and equals() for doing
	 * that properly. 
	 * <p>
	 * Note that not comparing tripCreatedForId. This
	 * is VERY IMPORTANT because when caching TravelTimesForTrip
	 * don't need to the tripCreatedForId to match. This is the only
	 * way that can used cached values for other trips when processing
	 * historic travel data.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + travelTimesRev;
		result = prime * result + tripPatternId.hashCode();
//		result = prime * result + tripCreatedForId.hashCode();
		result = prime * result + travelTimesForStopPaths.hashCode();
		return result;
	}

	/**
	 * Needed because of Hibernate and also because want to cache 
	 * TravelTimesForTrip and to do so need to store the objects
	 * as keys in a map and need hashCode() and equals() for doing
	 * that properly.
	 * <p>
	 * Note that not comparing tripCreatedForId. This
	 * is VERY IMPORTANT because when caching TravelTimesForTrip
	 * don't need to the tripCreatedForId to match. This is the only
	 * way that can used cached values for other trips when processing
	 * historic travel data.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TravelTimesForTrip other = (TravelTimesForTrip) obj;
		if (travelTimesForStopPaths == null) {
			if (other.travelTimesForStopPaths != null)
				return false;
		} else if (!travelTimesForStopPaths.equals(other.travelTimesForStopPaths))
			return false;
		if (configRev != other.configRev)
			return false;
		if (travelTimesRev != other.travelTimesRev)
			return false;
		if (!tripPatternId.equals(other.tripPatternId))
			return false;
//		if (!tripCreatedForId.equals(other.tripCreatedForId))
//			return false;
		return true;
	}

	
	@Override
	public String toString() {
		return "TravelTimesForTrip ["
				+ "configRev=" + configRev
				+ ", travelTimesRev=" + travelTimesRev
				+ ", tripPatternId=" + tripPatternId 
				+ ", tripCreatedForId=" + tripCreatedForId
				+ ", travelTimesForStopPaths=" + travelTimesForStopPaths 
				+ "]";
	}

	/**************************** Getter Methods ******************************/
	
	public int getConfigRev() {
		return configRev;
	}

	public int getTravelTimeRev() {
		return travelTimesRev;
	}
	
	public String getTripPatternId() {
		return tripPatternId;
	}

	public String getTripCreatedForId() {
		return tripCreatedForId;
	}
	
	public List<TravelTimesForStopPath> getTravelTimesForStopPaths() {
		return travelTimesForStopPaths;
	}
	
	public TravelTimesForStopPath getTravelTimesForStopPath(int index) {
		return travelTimesForStopPaths.get(index);
	}
	
	/**
	 * @return Number of stopPaths in trip
	 */
	public int numberOfStopPaths() {
		return travelTimesForStopPaths.size();
	}

}
