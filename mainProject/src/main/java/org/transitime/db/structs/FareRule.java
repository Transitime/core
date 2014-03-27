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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.gtfs.DbConfig;
import org.transitime.gtfs.gtfsStructs.GtfsFareRule;

/**
 * Contains data from the fare_rules.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="FareRules")
public class FareRule implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String fareId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String routeId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String originId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String destinationId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String containsId;

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = 6017523577565033167L;

	/********************** Member Functions **************************/

	/**
	 * For constructing FareRule object using GTFS data.
	 * 
	 * @param gfr
	 *            The GTFS data for the fare rule
	 * @param properRouteId
	 *            If the routeId should be changed to use parent route ID
	 */
	public FareRule(GtfsFareRule gfr, String properRouteId) {
		configRev = DbConfig.SANDBOX_REV;
		fareId = gfr.getFareId();
		// routeId, originId and destinationId are primary keys, which means they 
		// cannot be null. But they can be null from the GTFS fare_rules.txt
		// file since fare rule could apply to entire system. Therefore if
		// null set to empty string.
		String routeIdToUse; 
		if (properRouteId == null)
			routeIdToUse = gfr.getRouteId()==null?"":gfr.getRouteId();
		else
			routeIdToUse = properRouteId;
		routeId = routeIdToUse;
		originId = gfr.getOriginId()==null?"":gfr.getOriginId();
		destinationId = gfr.getDestinationId()==null?"":gfr.getDestinationId();
		containsId = gfr.getContainsId();
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private FareRule() {
		configRev = -1;
		fareId = null;
		routeId = null;
		originId = null;
		destinationId = null;
		containsId = null;
	}
	
	/**
	 * Deletes rev 0 from the FareRules table
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE FareRule WHERE configRev=0";
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of FareRule objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<FareRule> getFareRules(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM FareRule " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FareRule ["
				+ "configRev=" + configRev 
				+ ", fareId=" + fareId
				+ ", routeId=" + routeId 
				+ ", originId=" + originId
				+ ", destinationId=" + destinationId 
				+ ", containsId=" + containsId 
				+ "]";
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((containsId == null) ? 0 : containsId.hashCode());
		result = prime * result + configRev;
		result = prime * result + destinationId.hashCode();
		result = prime * result + ((fareId == null) ? 0 : fareId.hashCode());
		result = prime * result	+ originId.hashCode();
		result = prime * result + routeId.hashCode();
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
		FareRule other = (FareRule) obj;
		if (containsId == null) {
			if (other.containsId != null)
				return false;
		} else if (!containsId.equals(other.containsId))
			return false;
		if (configRev != other.configRev)
			return false;
		if (!destinationId.equals(other.destinationId))
			return false;
		if (fareId == null) {
			if (other.fareId != null)
				return false;
		} else if (!fareId.equals(other.fareId))
			return false;
		if (!originId.equals(other.originId))
			return false;
		if (!routeId.equals(other.routeId))
			return false;
		return true;
	}

	/**************************** Getter Methods ************************/
	
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the fareId
	 */
	public String getFareId() {
		return fareId;
	}

	/**
	 * @return the routeId
	 */
	public String getRouteId() {
		// With respect to the database, routeId cannot be null since 
		// it is a primary key. But sometimes it won't be set. For this case
		// should return null instead of empty string for consistency.
		return routeId.length()==0?null:routeId;
	}

	/**
	 * @return the originId
	 */
	public String getOriginId() {
		// With respect to the database, originId cannot be null since 
		// it is a primary key. But sometimes it won't be set. For this case
		// should return null instead of empty string for consistency.
		return originId.length()==0?null:originId;
	}

	/**
	 * @return the destinationId
	 */
	public String getDestinationId() {
		// With respect to the database, destinationId cannot be null since 
		// it is a primary key. But sometimes it won't be set. For this case
		// should return null instead of empty string for consistency.
		return destinationId.length()==0?null:destinationId;
	}

	/**
	 * @return the containsId
	 */
	public String getContainsId() {
		return containsId;
	}
	
}
