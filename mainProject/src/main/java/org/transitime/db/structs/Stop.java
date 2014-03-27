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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.gtfs.DbConfig;
import org.transitime.gtfs.TitleFormatter;
import org.transitime.gtfs.gtfsStructs.GtfsStop;


/**
 * For storing in db information on a stop. Based on GTFS info
 * from stops.txt file.
 * 
 * @author SkiBu Smith
 */
@Entity @DynamicUpdate @Table(name="Stops")
public class Stop implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	// The stop ID
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String id;
	
	// The stop code used for SMS and phone systems
	@Column
	private final Integer code;
	
	// Name of the stop
	@Column
	private final String name;
	
	// Latitude/longitude of stop
	@Embedded
	private final Location loc;

	// If should generate special ScheduleAdherence data for this stop
	@Column
	private final boolean adherenceStop;

	// Indicates that vehicle can leave route path before departing this stop
	// since the driver is taking a break.
	@Column
	private final Boolean layoverStop;

	// Indicates that vehicle is not supposed to depart the stop until the
	// scheduled departure time.
	@Column
	private final Boolean waitStop;

	// Indicates if stop should be hidden from public
	@Column
	private final boolean hidden;

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = -7960122597996109340L;

	/********************** Member Functions **************************/
	
	public Stop(GtfsStop gtfsStop, TitleFormatter titleFormatter) {
		// Because will be writing data to sandbox rev in the db
		configRev = DbConfig.SANDBOX_REV;
		
		id = gtfsStop.getStopId();
		
		// Some agencies like SFMTA don't bother to fill in the stop_code field in 
		// the GTFS data. But if they use a numeric stopId can use that.
		Integer stopCode = gtfsStop.getStopCode();
		if (stopCode == null) {
			// stop_code was not set in GTFS data so try using stop_id
			try {
				stopCode = Integer.parseInt(id);
			} catch (NumberFormatException e) {
				// Well, we tried using the stopId but it was not numeric.
				// Therefore the stopCode will simply be null.
			}
		}
		code = stopCode;
		
		name = titleFormatter.processTitle(gtfsStop.getStopName());
		loc = new Location(gtfsStop.getStopLat(), gtfsStop.getStopLon());
		// If adherence_stop not set then the default is false
		adherenceStop = (gtfsStop.getAdherenceStop() != null ?  gtfsStop.getAdherenceStop() : false); 
		// If layover_stop not set then the default is false
		layoverStop = gtfsStop.getlayoverStop(); 
		// If wait_stop not set then the default is false
		waitStop = gtfsStop.getWaitStop(); 
		// If hidden not set then the default is false
		hidden = (gtfsStop.getHidden() != null ?  gtfsStop.getHidden() : false); 
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Stop() {
		configRev = -1;
		id = null;
		code = null;
		name = null;
		loc = null;
		adherenceStop = false; 
		layoverStop = null; 
		waitStop = null; 
		hidden = false; 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Stop [" 
				+ "configRev=" + configRev
				+ ", id=" + id 
				+ ", code=" + code 
				+ ", name=" + name
				+ ", loc=" + loc 
				+ ", adherenceStop=" + adherenceStop
				+ ", layoverStop=" + layoverStop
				+ ", waitStop=" + waitStop
				+ ", hidden=" + hidden 
				+ "]";
	}
	
	/**
	 * Deletes rev 0 from the Stops table
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE Stop WHERE configRev=0";
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of Stop objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Stop> getStops(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Stop " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (adherenceStop ? 1231 : 1237);
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + configRev;
		result = prime * result + (hidden ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (layoverStop ? 1231 : 1237);
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (waitStop ? 1231 : 1237);
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
		Stop other = (Stop) obj;
		if (adherenceStop != other.adherenceStop)
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (configRev != other.configRev)
			return false;
		if (hidden != other.hidden)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (layoverStop == null) {
			if (other.layoverStop != null)
				return false;
		} else if (!layoverStop.equals(other.layoverStop))
			return false;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (waitStop == null) {
			if (other.waitStop != null)
				return false;
		} else if (!waitStop.equals(other.waitStop))
			return false;
		return true;
	}

	/************* Getter Methods ****************************/
	
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the loc
	 */
	public Location getLoc() {
		return loc;
	}

	/**
	 * 	
	 * Specifies if system should determine schedule adherence times
	 * for this stop. Some agencies configure all stops to have times but
	 * determining schedule adherence for every single stop would
	 * clutter things up. Only want to show adherence for a subset
	 * of stops.
     * @return the adherenceStop
	 */
	public boolean isScheduleAdherenceStop() {
		return adherenceStop;
	}

	/**
	 * Indicates that vehicle can leave route path before departing this stop
	 * since the driver is taking a break.
	 * 
	 * @return the layoverStop. Can be true, false, or null
	 */
	public Boolean isLayoverStop() {
		return layoverStop;
	}

	/**
	 * Indicates that vehicle is not supposed to depart the stop until the
	 * scheduled departure time.
	 * 
	 * @return the waitStop. Can be true, false, or null
	 */
	public Boolean isWaitStop() {
		return waitStop;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}
	
	
}
