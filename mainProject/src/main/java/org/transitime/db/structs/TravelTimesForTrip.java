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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
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
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripPatternId;
	
	// So know which trip these travel times were created for. Useful
	// for logging statements.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripCreatedForId;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(name="TravelTimesForTrip_to_TravelTimesForPath_joinTable")
	@OrderColumn( name="listIndex")
	private final List<TravelTimesForStopPath> travelTimesForStopPaths = 
			new ArrayList<TravelTimesForStopPath>();

	@Column(length=40)
	@Enumerated(EnumType.STRING)
	private final HowSet howSet;
	
	// Hibernate requires that this class be serializable if it has multiple 
	// column IDs so doing it in case have multiple ID columns in future.
	private static final long serialVersionUID = -5208608077900300605L;

	/**
	 * This enumeration is for keeping track of how the travel times were  
	 * determined. This way can tell of they should be overridden or not.  
	 */
	public enum HowSet {
		// From when there are no schedule times so simply need to use a
		// default speed
		DEFAULT_SPEED(0),

		// From interpolating data in GTFS stop_times.txt file
		SCHEDULE_TIMES(1),
		
		// No AVL data was available for the actual day so using data from
		// another day.
		AVL_DATA_FOR_ANOTHER_DAY(2),
	
		// No AVL data was available for the actual trip so using data from
		// a trip that is before or after the trip in question
		AVL_DATA_FOR_ADJACENT_TRIP(3),
		
		// Based on actual running times as determined by AVL data
		AVL_DATA(4);
		
		@SuppressWarnings("unused")
		private int value;
		
		private HowSet(int value) {
			this.value =  value;
		}
		
		public boolean isScheduleBased() {
			return this == DEFAULT_SPEED || 
					this == SCHEDULE_TIMES;
		}
	};
	

	/********************** Member Functions **************************/

	public TravelTimesForTrip(int configRev, Trip trip, HowSet howSet) {
		this.configRev = configRev;
		this.tripPatternId = trip.getTripPattern().getId();
		this.tripCreatedForId = trip.getId();
		this.howSet = howSet;
	}
	
	/**
	 * Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private TravelTimesForTrip() {
		this.configRev = -1;
		this.tripPatternId = null;
		this.tripCreatedForId = null;
		this.howSet = HowSet.SCHEDULE_TIMES;
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
	 * @param configRev
	 * @return Map keyed by tripPatternId of Lists of TripPatterns
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, List<TravelTimesForTrip>> getTravelTimesForTrip(
			String projectId, int configRev) 
			throws HibernateException {
		// Create the db session
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();

		// Get List of all TravelTimesForTrip for the specified rev
		String hql = "FROM TravelTimesForTrip " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + tripPatternId.hashCode();
		result = prime * result + tripCreatedForId.hashCode();
		result = prime * result + travelTimesForStopPaths.hashCode();
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
		TravelTimesForTrip other = (TravelTimesForTrip) obj;
		if (travelTimesForStopPaths == null) {
			if (other.travelTimesForStopPaths != null)
				return false;
		} else if (!travelTimesForStopPaths.equals(other.travelTimesForStopPaths))
			return false;
		if (configRev != other.configRev)
			return false;
		if (!tripPatternId.equals(other.tripPatternId))
			return false;
		if (!tripCreatedForId.equals(other.tripCreatedForId))
			return false;
		return true;
	}

	
	@Override
	public String toString() {
		return "TravelTimesForTrip ["
				+ "configRev=" + configRev
				+ ", tripPatternId=" + tripPatternId 
				+ ", tripCreatedForId=" + tripCreatedForId
				+ ", travelTimesForStopPaths=" + travelTimesForStopPaths 
				+ ", howSet=" + howSet 
				+ "]";
	}

	/**************************** Getter Methods ******************************/
	
	public int getConfigRev() {
		return configRev;
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

	/**
	 * @return the howSet
	 */
	public HowSet getHowSet() {
		return howSet;
	}
	
}
