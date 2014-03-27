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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.gtfs.DbConfig;
import org.transitime.gtfs.GtfsData;


/**
 * A trip pattern, as obtained from stop_times.txt GTFS file.
 * A trip pattern defines what stops are associated with a
 * trip. Trip pattern reduces the amount of data needed to describe
 * an agency since trips usually can share trip patterns.
 * 
 * @author SkiBu Smith
 */
@Entity @DynamicUpdate @Table(name="TripPatterns")
public class TripPattern extends TripPatternBase implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String id;
	
	@Column
	private final String name;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String directionId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;
	
	// So know lat lon range of the trip pattern 
	@Embedded
	private final Extent extent;
	
	// So know which trips use this trip pattern
	@Transient
	private List<Trip> trips = new ArrayList<Trip>();
	
	// Hibernate requires this class to be serializable because it uses multiple
	// columns for the Id.
	private static final long serialVersionUID = 8002349177548788550L;

	/********************** Member Functions **************************/
	
	/**
	 * Create a TripPattern. 
	 * 
	 * Note: The name comes from the trip trip_headsign
	 * data. If not set then uses name of last stop for trip.
	 * 
	 * @param tripPatternBase Already have the TripPatternBase info so
	 * pass that in. That way it doesn't need to be recreated from Trip.
	 * @param trip For supplying additional info
	 * @param gtfsData So can access stop data for determining extent of 
	 * trip pattern.
	 */
	public TripPattern(TripPatternBase tripPatternBase, Trip trip, GtfsData gtfsData) {
		super(tripPatternBase);

		// Because will be writing data to the sandbox rev in the db
		configRev = DbConfig.SANDBOX_REV;

		// Generate the id . 
		id = generateTripPatternId(
				shapeId,
				stopPaths.get(0),
				stopPaths.get(stopPaths.size()-1),
				trip,
				gtfsData);
		
		// Now that have the trip pattern ID set it for each StopPath
		for (StopPath path : stopPaths) {
			path.setTripPatternId(id);
		}
		
		// The trip_headsign in trips.txt and therefore the the trip name can be
		// null. For these cases use the last stop as a destination.
		if (trip.getName() != null) {
			name = trip.getName();
		} else {
			// trip_headsign was null so try using final stop name as the destination
			// as a fallback.
			StopPath lastPath = stopPaths.get(stopPaths.size()-1);
			String lastStopIdForTrip = lastPath.getStopId();
			Stop lastStopForTrip = gtfsData.getStop(lastStopIdForTrip);
			name = lastStopForTrip.getName();
		}
		
		// Store additional info from this trip
		directionId = trip.getDirectionId();
		routeId = trip.getRouteId();
		
		// Remember that this trip pattern refers to this particular 
		// trip. Additional trips will be added as they are processed.
		trips.add(trip);
		
		// Determine extent of trip pattern and store it within
		extent = new Extent();
		for (StopPath path : stopPaths) {
			// Determine the stop
			Stop stop = gtfsData.getStop(path.getStopId());
			extent.add(stop.getLoc());
		}
	}
	
	/**
	 * Hibernate requires a not-arg constructor
	 */
	@SuppressWarnings("unused")
	private TripPattern() {
		super();
		
		configRev = -1;
		id = null;
		name = null;
		directionId = null;
		routeId = null;
		extent = null;
		
	}

	/**
	 * Deletes rev 0 from the TripPattern_to_Path_joinTable, Paths, 
	 * and TripPatterns tables.
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		// In a perfect Hibernate world one would simply call on session.delete()
		// for each trip pattern and the join table and the associated trip pattern
		// elements would be automatically deleted by using the magic of Hibernate.
		// But this means that would have to read in all the objects and sub-objects
		// first, which of course takes lots of time and memory, often causing
		// program to crash due to out of memory issue. Therefore
		// using the much, much faster solution of direct SQL calls. Can't use
		// HQL on the join table since it is not a regularly defined table. 
		//
		// TODO Would be great to see if can actually use HQL and delete the 
		// appropriate TripPatterns and have the join table and the trip pattern
		// elements table be automatically updated. I doubt this would work but
		// would be interesting to try if had the time.
		//
		// Delete from TripPattern_to_Path_joinTable first since it has a foreign
		// key to the StopPath table, 
		int rowsUpdated = 0;
		rowsUpdated += 
				session.createSQLQuery("DELETE FROM TripPattern_to_Path_joinTable " + 
									   "WHERE TripPatterns_configRev=0").
				executeUpdate();
		rowsUpdated += 
				session.createSQLQuery("DELETE FROM StopPaths WHERE configRev=0").
				executeUpdate();
		rowsUpdated += 
				session.createSQLQuery("DELETE FROM TripPatterns WHERE configRev=0").
				executeUpdate();
		return rowsUpdated;
		
//		// Because TripPattern uses a List of Paths things are
//		// complicated because there are multiple tables with foreign keys.
//		// And the join table is not a regular Hibernate table so I don't
//		// believe can use hql to empty it out. Therefore it is best to
//		// read in the objects and then delete them and let Hibernate make
//		// sure it is all done correctly.
//		// NOTE: Unfortunately this is quite slow since have to read in
//		// all the objects first. Might just want to use regular SQL to
//		// delete the items in the TripPattern_to_Path_joinTable
//		List<TripPattern> tripPatternsFromDb = getTripPatterns(session, 0);
//		for (TripPattern tp : tripPatternsFromDb)
//			session.delete(tp);
//		// Need to flush. Otherwise when writing new TripPatterns will get
//		// a uniqueness violation even though already told the session to
//		// delete those objects.
//		session.flush();
//		return tripPatternsFromDb.size();
		
//		int numUpdates = 0;
//		String hql;
//		
//		// Need to first delete the list of Paths
//		hql = "DELETE StopPath WHERE configRev=0";
//		numUpdates += session.createQuery(hql).executeUpdate();
////		hql = "";
////		numUpdates += session.createQuery(hql).executeUpdate();
//		
//		// Note that hql uses class name, not the table name
//		hql = "DELETE TripPattern WHERE configRev=0";
//		numUpdates += session.createQuery(hql).executeUpdate();
//		return numUpdates;
	}
	
	/**
	 * Returns list of TripPattern objects for the specified configRev
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<TripPattern> getTripPatterns(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM TripPattern " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}


	/**
	 * Determines the ID of the TripPattern. If the Trip has a non-null shape ID
	 * then use it. If the shape ID is null then base the ID on the beginning and 
	 * ending stop IDs. It will be "stop1Id_to_stop2Id".
	 * 
	 * It is important to not just always use "stop1_to_stop2" since some 
	 * agencies might for the same stops have different stopPaths for connecting
	 * them. Therefore should use the shapeId from the Trip passed in. 
	 * 
	 * @param shapeId Used for the trip pattern id if it is not null
	 * @param path1 If shapeId null then used as part of ID
	 * @param path2 If shapeId null then used as part of ID
	 * @param trip In case things get complicated with determining ID
	 * @param gtfsData In case things get complicated with determining ID
	 * @return
	 */
	private static String generateTripPatternId(String shapeId, 
			StopPath path1, StopPath path2,
			Trip trip, 
			GtfsData gtfsData) {
		String tripPatternId;
		
		// Use the shape ID if it is set.
		if (shapeId != null)
			tripPatternId = shapeId;
		else
			// No shape ID available so use "stop1_to_stop2"
			tripPatternId = path1.getStopId() + "_to_" + path2.getStopId();
		
		// Still need to make sure that tripPatternIds are unique. Seen where
		// SFMTA defines a trip using the same shape but defines a different
		// number of stops. For this situation make the ID unique and
		// warn the user that there likely is a problem with the data.
		boolean problemWithTripPatternId = false;
		while (gtfsData.isTripPatternIdAlreadyUsed(tripPatternId)) {
			tripPatternId += "_var";
			problemWithTripPatternId = true;
		}
		
		if (problemWithTripPatternId)
			GtfsData.logger.warn("There was a problem with creating trip " + 
					"pattern for tripId={} for routeId={} in " +
					"TripPattern.generateTripPatternId(). " + 
					"There already was a trip pattern with the desired name. " + 
					"This likely means that a trip pattern is defined with the " +
					"same shapeId (which is used for the trip pattern ID) but " +
					"with different stop list indicating the trips are not " +
					"consistently defined. Therefore using the special " +
					"tripPatternId={}.",
					trip.getId(), trip.getRouteId(),
					tripPatternId);
		
		return tripPatternId;
	}
	
	/**
	 * When processing a new trip let the TripPattern know that
	 * this additional Trip refers to it. 
	 * @param gtfsTrip
	 */
	public void addTrip(Trip trip) {
		trips.add(trip);
	}
			
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Don't want to list full trips array because that is
		// a lot of unneeded data. Only list the tripIds from
		// the trips array.
		String tripsIds = "[";
		for (Trip t : trips) {
			tripsIds += t.getId() + ", ";
		}
		tripsIds += "]";
		
		return "TripPattern ["
				+ "configRev=" + configRev
				+ ", id=" + id
				+ ", name=" + name
				+ ", routeId=" + routeId
				+ ", shapeId=" + shapeId
				+ ", extent=" + extent
				+ ", trips=" + tripsIds
				+ ", stopPaths=" + stopPaths
				+ "]";
	}	

	/**
	 * For when don't want to display the entire contents of TripPattern,
	 * which can be pretty large because contains list of stops and trips.
	 * @return A short version of the TripPattern object
	 */
	public String toShortString() {
		return name 
				+ " from stop " + stopPaths.get(0).getStopId() 
				+ " to stop " + stopPaths.get(stopPaths.size()-1).getStopId(); 
	}
		
	/**
	 * A short version of the Trip string. Only includes the name and
	 * a list of the trip ids.
	 * @return
	 */
	public String toStringListingTripIds() {
		String s = "Trip Pattern [id=" + id + ", name=" + name + ", trips=[";
		for (Trip trip : trips) {
			s += trip.getId() + ",";
		}
		s += "] ]";
		return s;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + configRev;
		result = prime * result
				+ ((directionId == null) ? 0 : directionId.hashCode());
		result = prime * result + ((extent == null) ? 0 : extent.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((trips == null) ? 0 : trips.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TripPattern other = (TripPattern) obj;
		if (configRev != other.configRev)
			return false;
		if (directionId == null) {
			if (other.directionId != null)
				return false;
		} else if (!directionId.equals(other.directionId))
			return false;
		if (extent == null) {
			if (other.extent != null)
				return false;
		} else if (!extent.equals(other.extent))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (trips == null) {
			if (other.trips != null)
				return false;
		} else if (!trips.equals(other.trips))
			return false;
		return true;
	}

	/************** Getter Methods ****************/
	
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}
	
	/**
	 * @return the List of stop IDs
	 */
	public List<StopPath> getStopPaths() {
		return stopPaths;
	}
	
	/**
	 * @param index
	 * @return The specified StopPath or null if index out of range
	 */
	public StopPath getStopPath(int index) {
		if (index < 0 || index >= stopPaths.size())
			return null;
		
		return stopPaths.get(index);
	}
	
	/**
	 * Returns the number of stopPaths/stops configured.
	 * @return
	 */
	public int getNumberStopPaths() {
		return stopPaths.size();
	}
	
	/**
	 * Gets the stopId of the specified stop
	 * @param i
	 * @return
	 */
	public String getStopId(int i) {
		return stopPaths.get(i).getStopId();
	}
	
	/**
	 * Gets the pathId of the specified stop
	 * @param i
	 * @return
	 */
	public String getStopPathId(int i) {
		return stopPaths.get(i).getStopPathId();
	}
	
	/**
	 * @return shapeId which is the shape_id from the trip used to
	 * create this trip pattern.
	 */
	public String getShapeId() {
		return shapeId;
	}
	
	
	/**
	 * @return the id which is of the form "stopId1_to_stopIds"
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return routeId, the id of the route
	 */
	public String getRouteId() {
		return routeId;
	}

	/**
	 * Usually from the trip_headsign from the trips.txt file
	 * @return name, the title of the trip pattern
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Optional element direction_id specified in trips.txt GTFS file.
	 * @return
	 */
	public String getDirectionId() {
		return directionId;
	}
	
	/**
	 * NOTE: the Trip List is not available when the object has been read from the
	 * database. It is only available while actually processing the GTFS data.
	 * 
	 * @return trips, the list of the trips that use the trip pattern
	 */
	public List<Trip> getTrips() {
		return trips;
	}
	
	/**
	 * The extent of all of the stops that make up the trip pattern
	 * @return extent
	 */
	public Extent getExtent() {
		return extent;
	}
	
}
