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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.gtfs.TitleFormatter;
import org.transitime.gtfs.gtfsStructs.GtfsRoute;
import org.transitime.utils.OrderedCollection;
import org.transitime.utils.StringUtils;


/**
 * For storing in db information for a route. Based on GTFS information
 * from routes.txt and other files.
 * 
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="Routes")
public class Route implements Serializable {
	
	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String id;
	
	@Column(length=10)
	private final String color;
	
	@Column(length=10)
	private final String textColor;
	
	// Not declared final because need to set route order for all
	// routes that did not have the order configured in the db,
	// but can only do so once all routes read in and sorted.
	@Column
	private Integer routeOrder;
	
	@Column
	private final boolean hidden;
	
	@Column(length=2)
	private final String type;
	
	@Column
	private final String description;
	
	@Column
	private final String name;
	
	@Column(length=80)
	private final String shortName;
	
	@Embedded
	private final Extent extent;
	
	// Optional parameter that specifies how far away AVL report can be
	// from segments and still be considered a match.
	@Column
	private final Double maxDistance;
	
	@Transient // Later will probably want to store this in database, 
	           // but not yet sure. This means it is not available to application!
	private final List<TripPattern> tripPatternsForRoute;

	// For getStops()
	@Transient
	private Collection<Stop> stops = null;
	
	// For getPathSegments()
	@Transient
	private Collection<Vector> stopPaths = null;

	// For getOrderedStopsByDirection()
	@Transient
	private Map<String, List<String>> orderedStopsPerDirectionMap = null;
	
	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = 9037023420649883860L;

	/********************** Member Functions **************************/

	/**
	 * Constructor. Used for when processing GTFS data. Creates a 
	 * Route object that can be written to database.
	 * 
	 * @param configRev
	 * @param gtfsRoute
	 * @param tripPatternsForRoute
	 * @param titleFormatter
	 * @param shouldCombineShortAndLongNamesForRoutes
	 */
	public Route(int configRev, GtfsRoute gtfsRoute, 
			List<TripPattern> tripPatternsForRoute,
			TitleFormatter titleFormatter, 
			boolean shouldCombineShortAndLongNamesForRoutes) {
		// Because will be writing data to the sandbox in the db
		this.configRev = configRev;
		
		// Here are most of the params from GtfsRoute
		this.id = gtfsRoute.getRouteId();
		this.color = gtfsRoute.getRouteColor();
		this.textColor = gtfsRoute.getRouteTextColor();
		this.routeOrder = gtfsRoute.getRouteOrder();
		this.hidden = gtfsRoute.getHidden();
		this.type = gtfsRoute.getRouteType();
		this.description = gtfsRoute.getRouteDesc(); 

		// Get the name of the route. Need to do some fancy processing here because 
		// need to fix the capitalization using the TitleFormatter. This also
		// does all the regex processing to fix other issues. Might also need
		// to combine short and long names into a single name.
		if (shouldCombineShortAndLongNamesForRoutes) {			
			if (gtfsRoute.getRouteLongName() != null && !gtfsRoute.getRouteLongName().isEmpty()) {
				String shortName = "";
				if (gtfsRoute.getRouteShortName() != null)
					shortName = gtfsRoute.getRouteShortName() + " - ";
				this.name = shortName + titleFormatter.processTitle(gtfsRoute.getRouteLongName());
			} else
				this.name = titleFormatter.processTitle(gtfsRoute.getRouteShortName());
		} else {
			if (gtfsRoute.getRouteLongName() != null && !gtfsRoute.getRouteLongName().isEmpty())
				this.name = titleFormatter.processTitle(gtfsRoute.getRouteLongName());
			else
				this.name = titleFormatter.processTitle(gtfsRoute.getRouteShortName());
		}
		
		this.tripPatternsForRoute = tripPatternsForRoute;	
		this.shortName = gtfsRoute.getRouteShortName();
		this.maxDistance =gtfsRoute.getMaxDistance();  
		
		// Determine the extent of the route by looking at the extent
		// of all of the trip patterns.
		this.extent = new Extent();
		for (TripPattern tp : tripPatternsForRoute) {
			this.extent.add(tp.getExtent());
		}
	}
		
	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Route() {
		configRev = -1;
		id = null;
		color = null;
		textColor = null;
		routeOrder = null;
		hidden = false;
		type = null;
		description = null;
		name = null;
		shortName = null;
		extent = null;
		tripPatternsForRoute = null;
		maxDistance = null;
	}
	
	/**
	 * Deletes rev from the Routes table
	 * 
	 * @param session
	 * @param configRev
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromRev(Session session, int configRev) 
			throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE Route WHERE configRev=" + configRev;
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}	
	
	/**
	 * Comparator for sorting Routes into proper order.
	 * 
	 * If routeOrder is set and is below 1,000 then the route will be at
	 * beginning of list and will be ordered by routeOrder. If routeOrder is set
	 * and is above 1,000,000 then route will be put at end of list and will be
	 * ordered by routeOrder. If routeOrder is not set then will order by route
	 * short name. If route short name starts with numbers it will be padded by
	 * zeros so that proper numerical order will be used.
	 */
	private static final Comparator<Route> routeComparator = 
			new Comparator<Route>() {
		/**
		 * Returns negative if r1<r2, zero if r1=r2, and positive if r1>r2
		 */
		@Override
		public int compare(Route r1, Route r2) {
			// Handle if routeOrder indicates r1 should be at beginning of list
			if (r1.atBeginning()) { 
				// If r2 also at beginning and it should be before r1...
				if (r2.atBeginning() && r1.getRouteOrder()>r2.getRouteOrder())
					return 1;
				else
					return -1;
			}
			
			// Handle if routeOrder indicates r1 should be at end of list
			if (r1.atEnd()) {
				// If r2 also at end and it should be after r1...
				if (r2.atEnd() && r1.getRouteOrder()<r2.getRouteOrder())
					return -1;
				else
					return 1;
			}
			
			// r1 is in the middle so check to see if r2 is at beginning or end
			if (r2.atBeginning())
				return 1;
			if (r2.atEnd())
				return -1;
			
			// Both r1 and r2 don't have a route order to order them by  
			// route name
			return StringUtils.paddedName(r1.name).compareTo(
					StringUtils.paddedName(r2.name));
		}
	};
	
	/**
	 * Returns List of Route objects for the specified database revision.
	 * Orders them based on the GTFS route_order extension or the
	 * route short name if route_order not set.
	 * 
	 * @param session
	 * @param configRev
	 * @return Map of routes keyed on routeId
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Route> getRoutes(Session session, int configRev) 
			throws HibernateException {
		// Get list of routes from database
		String hql = "FROM Route " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		List<Route> routesList = query.list();
	
		// Put the routes into proper order
		Collections.sort(routesList, routeComparator);
		
		// Need to set the route order for each route so that can sort
		// predictions based on distance from stop and route order. For
		// the routes that didn't have route ordered configured in db
		// start with 1000 and count on up.
		int routeOrderForWhenNotConfigured = 1000;
		for (Route route: routesList) {
			if (!route.atBeginning() && !route.atEnd()) {
				route.setRouteOrder(routeOrderForWhenNotConfigured++);
			}
		}
		
		// Return the list of routes
		return routesList;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Don't want to output list of full TripPattern objects because
		// each TripPattern.toString() result is pretty long (list of stops,
		// extent, etc). Therefore for tripPatternsForRoute just output
		// a short version of the object.
		String tripPatternIds = "not set"; 
		if (tripPatternsForRoute != null) {
			tripPatternIds = "[";
			for (TripPattern tp : tripPatternsForRoute) 
				tripPatternIds += tp.toShortString() + ", ";
			tripPatternIds += "]";
		}
		
		return "Route ["
				+ "configRev=" + configRev 
				+ ", id=" + id 
				+ ", name=" + name
				+ ", color=" + color 
				+ ", textColor=" + textColor 
				+ ", routeOrder=" + routeOrder 
				+ ", hidden=" + hidden 
				+ ", type=" + type
				+ ", description=" + description 
				+ ", shortName=" + shortName
				+ ", extent" + extent
				+ ", tripPatternsForRoute=" + tripPatternIds
				+ "]";
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + configRev;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((extent == null) ? 0 : extent.hashCode());
		result = prime * result + (hidden ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + routeOrder;
		result = prime * result
				+ ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result
				+ ((textColor == null) ? 0 : textColor.hashCode());
		result = prime
				* result
				+ ((tripPatternsForRoute == null) ? 0 : tripPatternsForRoute
						.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Route other = (Route) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (configRev != other.configRev)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (extent == null) {
			if (other.extent != null)
				return false;
		} else if (!extent.equals(other.extent))
			return false;
		if (hidden != other.hidden)
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
		if (routeOrder != other.routeOrder)
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (textColor == null) {
			if (other.textColor != null)
				return false;
		} else if (!textColor.equals(other.textColor))
			return false;
		if (tripPatternsForRoute == null) {
			if (other.tripPatternsForRoute != null)
				return false;
		} else if (!tripPatternsForRoute.equals(other.tripPatternsForRoute))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * Returns unordered collection of stops associated with route.
	 * <p>
	 * Synchronized because caching stops.
	 * 
	 * @return
	 */
	public synchronized Collection<Stop> getStops() {
		// If stop collection already determined then simply return it
		if (stops != null)
			return stops;
		
		// Get the trip patterns for the route. Can't use the member
		// variable tripPatternsForRoute since it is only set when the
		// GTFS data is processed and stored in the db. Since this member
		// is transient it is not stored in the db and therefore not
		// available to this client application. But it can be obtained
		// from the DbConfig.
		List<TripPattern> tripPatternsForRoute = 
				Core.getInstance().getDbConfig().getTripPatternsForRoute(id);

		// Stop list not yet determined so determine it now using
		// trip patterns.
		Map<String, Stop> stopMap = new HashMap<String, Stop>();
		for (TripPattern tripPattern : tripPatternsForRoute) {
			for (StopPath stopPath : tripPattern.getStopPaths()) {
				String stopId = stopPath.getStopId();
				
				// If already added this stop then continue to next one
				if (stopMap.containsKey(stopId))
					continue;
				
				Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
				stopMap.put(stopId, stop);
			}
		}
		stops = stopMap.values();
		
		// Return the newly created collection of stops
		return stops;
	}
	
	/**
	 * Returns the specified trip pattern, or null if that trip pattern doesn't
	 * exist for the route.
	 * 
	 * @param tripPatternId
	 * @return
	 */
	public TripPattern getTripPattern(String tripPatternId) {
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(getId());
		for (TripPattern tripPattern : tripPatternsForRoute) {
			if (tripPattern.getId().equals(tripPatternId))
				return tripPattern;
		}
		
		// Never found the specified trip pattern
		return null;
	}

	/**
	 * Returns longest trip pattern for the directionId specified.
	 * Note: gets trip patterns from Core, which means it works
	 * in the core application, not just when processing GTFS data.
	 * 
	 * @param directionId
	 * @return
	 */
	public TripPattern getLongestTripPatternForDirection(String directionId) {
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(getId());
		TripPattern longestTripPatternForDir = null;
		for (TripPattern tripPattern : tripPatternsForRoute) {
			if (Objects.equals(tripPattern.getDirectionId(), directionId)) {
				if (longestTripPatternForDir == null
						|| tripPattern.getNumberStopPaths() > longestTripPatternForDir
								.getNumberStopPaths())
					longestTripPatternForDir = tripPattern;
			}
		}
		
		return longestTripPatternForDir;
	}
	
	/**
	 * Returns the longest trip pattern for each direction ID for the route.
	 * Will typically be two trip patterns since there are usually two
	 * directions per route.
	 * 
	 * @return
	 */
	public List<TripPattern> getLongestTripPatternForEachDirection() {
		List<TripPattern> tripPatterns = new ArrayList<TripPattern>();
		
		List<String> directionIds = getDirectionIds();
		for (String directionId : directionIds)
			tripPatterns.add(getLongestTripPatternForDirection(directionId));
		
		return tripPatterns;
	}
	
	/**
	 * Returns list of trip patterns for the directionId specified.
	 * 
	 * @param directionId
	 * @return
	 */
	public List<TripPattern> getTripPatterns(String directionId) {
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(getId());
		List<TripPattern> tripPatternsForDir = new ArrayList<TripPattern>();
		for (TripPattern tripPattern : tripPatternsForRoute) {
			if (Objects.equals(tripPattern.getDirectionId(), directionId))
				tripPatternsForDir.add(tripPattern);
		}
		
		return tripPatternsForDir;
	}
	
	/**
	 * Returns list of direction IDs for the route.
	 * 
	 * @return
	 */
	public List<String> getDirectionIds() {
		List<String> directionIds = new ArrayList<String>();
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(getId());
		for (TripPattern tripPattern : tripPatternsForRoute) {
			String directionId = tripPattern.getDirectionId();
			if (!directionIds.contains(directionId))
				directionIds.add(directionId);	
		}
		return directionIds;
	}

	/**
	 * Returns unordered collection of path vectors associated with route
	 * @return
	 */
	public synchronized Collection<Vector> getPathSegments() {
		// If stop paths collection already determined then simply return it
		if (stopPaths != null)
			return stopPaths;
		
		// Get the trip patterns for the route. Can't use the member
		// variable tripPatternsForRoute since it is only set when the
		// GTFS data is processed and stored in the db. Since this member
		// is transient it is not stored in the db and therefore not
		// available to this client application. But it can be obtained
		// from the DbConfig.
		List<TripPattern> tripPatternsForRoute = 
				Core.getInstance().getDbConfig().getTripPatternsForRoute(id);
		
		Map<String, StopPath> stopPathMap = new HashMap<String, StopPath>();
		for (TripPattern tripPattern : tripPatternsForRoute) {
			for (StopPath stopPath : tripPattern.getStopPaths()) {
				String stopPathId = stopPath.getId();
				
				// If already added this stop then continue to next one
				if (stopPathMap.containsKey(stopPathId))
					continue;
				
				stopPathMap.put(stopPathId, stopPath);
			}
		}
		
		// For each of the unique stop paths add the vectors to the collection
		stopPaths = new ArrayList<Vector>(stopPathMap.values().size());
		for (StopPath stopPath : stopPathMap.values()) {
			for (Vector vector : stopPath.getSegmentVectors()) {
				stopPaths.add(vector);
			}
		}
		
		// Return the newly created collection of stop paths
		return stopPaths;
	}
	
	/**
	 * For each GTFS direction ID returns list of stops that in the appropriate
	 * order for the direction. The appropriate order means that when there are
	 * different trip patterns that the stops that are different will be
	 * inserted appropriately into the list
	 * 
	 * @return Map keyed by direction ID and value of List of ordered stop IDs.
	 */
	public Map<String, List<String>> getOrderedStopsByDirection() {
		// If already determined the stops return the cached map
		if (orderedStopsPerDirectionMap != null)
			return orderedStopsPerDirectionMap;
		
		// Haven't yet determined ordered stops so do so now
		orderedStopsPerDirectionMap = new HashMap<String, List<String>>();
		
		// For each direction
		for (String directionId : getDirectionIds()) {
			// Determine ordered collection of stops for direction
			OrderedCollection orderedCollection = new OrderedCollection();			
			List<TripPattern> tripPatternsForDir = getTripPatterns(directionId);
			for (TripPattern tripPattern : tripPatternsForDir) {
				List<String> stopIdsForTripPattern = tripPattern.getStopIds();
				orderedCollection.add(stopIdsForTripPattern);
			}
			
			orderedStopsPerDirectionMap.put(directionId, orderedCollection.get());
		}
		
		return orderedStopsPerDirectionMap;
	}
	
	/********************** Getter Methods **************************/

	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the name of the route
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The short name is either specified by route_short_name in the routes.txt
	 * GTFS file or if that is null it will be the long name name. 
	 * @return the short name for the route
	 */
	public String getShortName() {
		return shortName != null ? shortName : name;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @return the textColor
	 */
	public String getTextColor() {
		return textColor;
	}

	/**
	 * @return the routeOrder or null if not set
	 */
	public Integer getRouteOrder() {
		return routeOrder;
	}

	/**
	 * Declared private because just for internal use. For setting
	 * route order once all routes read in and sorted.
	 * @param routeOrder
	 */
	private void setRouteOrder(int routeOrder) {
		this.routeOrder = routeOrder;
	}
	
	private boolean atBeginning() {
		return routeOrder != null && routeOrder < 1000;
	}
	
	private boolean atEnd() {
		return routeOrder != null && routeOrder >= 1000000;
	}
	
	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the extent of the stops for the route
	 */
	public Extent getExtent() {
		return extent;
	}
	
	/**
	 * For specifying on a per route basis how far AVL report can be from
	 * segment and still have it be considered a match.
	 * 
	 * @return the max distance if set, otherwise NaN
	 */
	public double getMaxAllowableDistanceFromSegment() {
		if (maxDistance != null)
			return maxDistance;
		else
			return Double.NaN;
	}
	
	/**
	 * Just for debugging
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		System.out.println(StringUtils.paddedName("Y2"));
		System.out.println(StringUtils.paddedName("Y101"));
		System.out.println(StringUtils.paddedName("Y2xx"));
		System.out.println(StringUtils.paddedName("123SKJFD"));
		System.out.println(StringUtils.paddedName("123"));
		System.out.println(StringUtils.paddedName("123.456"));
	}
}
