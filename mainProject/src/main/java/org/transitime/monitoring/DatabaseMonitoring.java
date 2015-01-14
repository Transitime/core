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

package org.transitime.monitoring;

import java.util.List;

import org.hibernate.HibernateException;
import org.transitime.applications.Core;
import org.transitime.config.DoubleConfigValue;
import org.transitime.db.hibernate.DataDbLogger;
import org.transitime.db.structs.DbTest;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;

/**
 * For monitoring access to database. Looks at both size of the db logging queue
 * and whether can read and write to database.
 *
 * @author SkiBu Smith
 *
 */
public class DatabaseMonitoring extends MonitorBase {

	DoubleConfigValue maxQueueFraction = new DoubleConfigValue(
			"transitime.monitoring.maxQueueFraction", 
			0.4, 
			"If database queue fills up by more than this 0.0 - 1.0 "
			+ "fraction then database monitoring is triggered.");
	
	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public DatabaseMonitoring(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}

	/**
	 * Determines if db queue is too full.
	 * 
	 * @return True if database queue is too full
	 */
	private boolean dbQueueTriggered() {
		Core core = Core.getInstance();
		if (core == null)
			return false;
		
		DataDbLogger dbLogger = core.getDbLogger();
		
		setMessage("Database queue fraction=" 
				+ StringUtils.twoDigitFormat(dbLogger.queueLevel())
				+ " while max allowed fraction=" 
				+ StringUtils.twoDigitFormat(maxQueueFraction.getValue()) 
				+ ". Items in queue=" + dbLogger.queueSize());
		
		return dbLogger.queueLevel() > maxQueueFraction.getValue(); 
	}
	
	/**
	 * Determines if can read and write from/to the database
	 * 
	 * @return True if there is a problem
	 */
	private boolean dbWriteReadTriggered() {
		try {
			// Clear out old data from db
			DbTest.deleteAll(agencyId);
			
			// See if can write an object to database
			if (!DbTest.write(agencyId, 999)) {
				setMessage("Could not write DbTest object to database.");
				return true;
			}

			List<DbTest> dbTests = DbTest.readAll(agencyId);
			if (dbTests.size() == 0) {
				setMessage("Could not read DbTest objects from database.");
				return true;
			}
		} catch (HibernateException e) {
			setMessage("Problem accessing database. " + e.getMessage());
			return true;
		}

		// Everything OK
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		// Check each system value and return true if there is a problem.
		if (dbQueueTriggered())
			return true;
		
		if (dbWriteReadTriggered())
			return true;
		
		// No problems detected so return false
		return false;

	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "Database";
	}


}
