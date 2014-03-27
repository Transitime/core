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
import java.util.TimeZone;

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
import org.transitime.gtfs.gtfsStructs.GtfsAgency;

/**
 * Contains data from the agency.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="Agencies")
public class Agency implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String agencyId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String agencyName;
	
	@Column
	private final String agencyUrl;
	
	// Note: agencyTimezone can be reasonable long. At least as long 
	// as "America/Los_Angeles". Valid timezone format is at 
	// http://en.wikipedia.org/wiki/List_of_tz_zones
	@Column(length=40)
	private final String agencyTimezone;
	
	@Column(length=15)
	private final String agencyLang;
	
	@Column(length=15)
	private final String agencyPhone;
	
	@Column
	private final String agencyFareUrl;

	@Embedded
	private final Extent extent;
	

	@Transient
	private TimeZone timezone = null;
	
	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = -3381456129303325040L;

	/********************** Member Functions **************************/

	public Agency(GtfsAgency gtfsAgency, List<Route> routes) {
		configRev = DbConfig.SANDBOX_REV;
		agencyId = gtfsAgency.getAgencyId();
		agencyName = gtfsAgency.getAgencyName();
		agencyUrl = gtfsAgency.getAgencyUrl();
		agencyTimezone = gtfsAgency.getAgencyTimezone();
		agencyLang = gtfsAgency.getAgencyLang();
		agencyPhone = gtfsAgency.getAgencyPhone();
		agencyFareUrl = gtfsAgency.getAgencyFareUrl();
		
		Extent extent = new Extent();
		for (Route route : routes) {
			extent.add(route.getExtent());
		}
		this.extent = extent;
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Agency() {
		configRev = -1;
		agencyId = null;
		agencyName = null;
		agencyUrl = null;
		agencyTimezone = null;
		agencyLang = null;
		agencyPhone = null;
		agencyFareUrl = null;
		extent = null;
	}

	/**
	 * Deletes rev 0 from the Agencies table
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE Agency WHERE configRev=0";
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of Agency objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Agency> getAgencies(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Agency " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Returns TimeZone object for agency. Useful for creating
	 * Calendar objects and such.
	 * 
	 * @return The TimeZone object for this agency
	 */
	public TimeZone getTimeZone() {
		if (timezone == null)
			timezone = TimeZone.getTimeZone(agencyTimezone); 
		return timezone;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Agency [" 
				+ "configRev=" + configRev 
				+ ", agencyId=" + agencyId
				+ ", agencyName=" + agencyName 
				+ ", agencyUrl=" + agencyUrl
				+ ", agencyTimezone=" + agencyTimezone 
				+ ", agencyLang=" + agencyLang 
				+ ", agencyPhone=" + agencyPhone
				+ ", agencyFareUrl=" + agencyFareUrl 
				+ ", extent=" + extent
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
				+ ((agencyFareUrl == null) ? 0 : agencyFareUrl.hashCode());
		result = prime * result
				+ ((agencyId == null) ? 0 : agencyId.hashCode());
		result = prime * result
				+ ((agencyLang == null) ? 0 : agencyLang.hashCode());
		result = prime * result
				+ ((agencyName == null) ? 0 : agencyName.hashCode());
		result = prime * result
				+ ((agencyPhone == null) ? 0 : agencyPhone.hashCode());
		result = prime * result
				+ ((agencyTimezone == null) ? 0 : agencyTimezone.hashCode());
		result = prime * result
				+ ((agencyUrl == null) ? 0 : agencyUrl.hashCode());
		result = prime * result
				+ ((extent == null) ? 0 : extent.hashCode());
		result = prime * result + configRev;
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
		Agency other = (Agency) obj;
		if (agencyFareUrl == null) {
			if (other.agencyFareUrl != null)
				return false;
		} else if (!agencyFareUrl.equals(other.agencyFareUrl))
			return false;
		if (agencyId == null) {
			if (other.agencyId != null)
				return false;
		} else if (!agencyId.equals(other.agencyId))
			return false;
		if (agencyLang == null) {
			if (other.agencyLang != null)
				return false;
		} else if (!agencyLang.equals(other.agencyLang))
			return false;
		if (agencyName == null) {
			if (other.agencyName != null)
				return false;
		} else if (!agencyName.equals(other.agencyName))
			return false;
		if (agencyPhone == null) {
			if (other.agencyPhone != null)
				return false;
		} else if (!agencyPhone.equals(other.agencyPhone))
			return false;
		if (agencyTimezone == null) {
			if (other.agencyTimezone != null)
				return false;
		} else if (!agencyTimezone.equals(other.agencyTimezone))
			return false;
		if (agencyUrl == null) {
			if (other.agencyUrl != null)
				return false;
		} else if (!agencyUrl.equals(other.agencyUrl))
			return false;
		if (extent == null) {
			if (other.extent != null)
				return false;
		} else if (!extent.equals(other.extent))
			return false;
		if (configRev != other.configRev)
			return false;
		return true;
	}

	/************************** Getter Methods ******************************/

	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the agencyId
	 */
	public String getId() {
		return agencyId;
	}

	/**
	 * @return the agencyName
	 */
	public String getName() {
		return agencyName;
	}

	/**
	 * @return the agencyUrl
	 */
	public String getUrl() {
		return agencyUrl;
	}

	/**
	 * Valid timezone format is at http://en.wikipedia.org/wiki/List_of_tz_zones
	 * 
	 * @return the agencyTimezone as a String
	 */
	public String getTimeZoneStr() {
		return agencyTimezone;
	}

	/**
	 * @return the agencyLang
	 */
	public String getLang() {
		return agencyLang;
	}

	/**
	 * @return the agencyPhone
	 */
	public String getPhone() {
		return agencyPhone;
	}

	/**
	 * @return the agencyFareUrl
	 */
	public String getFareUrl() {
		return agencyFareUrl;
	}
	
	/**
	 * @return The extent of all the stops for the agency
	 */
	public Extent getExtent() {
		return extent;
	}
	
}
