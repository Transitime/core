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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * For keeping track of information having to do with a configuration revision.
 * This way can keep track of reason for processing config, when it was run,
 * etc.
 *
 * @author Michael Smith (michael@transitclock.org)
 *
 */
@Entity @DynamicUpdate
public class ConfigRevision {

	@Id
	@Column
	private final int configRev;

	@Temporal(TemporalType.TIMESTAMP)
	private final Date processedTime;
	
	// Last modified time of zip file if GTFS data comes directly
	// from zip file instead of from a directory.
	@Temporal(TemporalType.TIMESTAMP)
	private final Date zipFileLastModifiedTime;
	
	@Column(length=512)
	private final String notes;

	// Logging
	public static final Logger logger = 
			LoggerFactory.getLogger(ConfigRevision.class);

	/********************** Member Functions **************************/

	/**
	 * @param configRev
	 * @param processedTime
	 * @param zipFileLastModifiedTime
	 * @param notes
	 */
	public ConfigRevision(int configRev, Date processedTime,
			Date zipFileLastModifiedTime, String notes) {
		this.configRev = configRev;
		this.processedTime = processedTime;
		this.zipFileLastModifiedTime = zipFileLastModifiedTime;
		this.notes = notes;
	}
	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private ConfigRevision() {
		this.configRev = -1;
		this.processedTime = null;
		this.zipFileLastModifiedTime = null;
		this.notes = null;
	}
	
	@Override
	public String toString() {
		return "ConfigRevision [" 
				+ "configRev=" + configRev 
				+ ", processedTime=" + processedTime 
				+ ", zipFileLastModifiedTime=" + zipFileLastModifiedTime
				+ ", notes=" + notes 
				+ "]";
	}

	/**
	 * Stores this ConfigRevision into the database for the agencyId.
	 * 
	 * @param agencyId
	 */
	public void save(String agencyId) {
		Session session = HibernateUtils.getSession(agencyId);
		Transaction tx = session.beginTransaction();
		try {
			session.save(this);
			tx.commit();
		} catch (HibernateException e) {
			logger.error("Error saving ConfigRevision data to db. {}", 
					this, e);
		} finally {
			session.close();
		}
	}
	
	/*********************** Getters *********************/
	
	public int getConfigRev() {
		return configRev;
	}
	
	public Date getProcessedTime() {
		return processedTime;
	}

	/**
	 * Last modified time of zip file if GTFS data comes directly from zip file
	 * instead of from a directory.
	 * 
	 * @return
	 */
	public Date getZipFileLastModifiedTime() {
		return zipFileLastModifiedTime;
	}
	
	public String getNotes() {
		return notes;
	}

}
