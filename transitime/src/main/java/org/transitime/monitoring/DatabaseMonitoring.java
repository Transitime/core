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
import org.transitime.db.structs.DbTest;
import org.transitime.utils.EmailSender;

/**
 * For monitoring access to database. Makes sure can read and write to database.
 *
 * @author SkiBu Smith
 *
 */
public class DatabaseMonitoring extends MonitorBase {

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

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		try {
			// Clear out old data from db
			DbTest.deleteAll(agencyId);
			
			// See if can write an object to database
			if (!DbTest.write(agencyId, 999)) {
				setMessage("Could not write DbTest object to database.");
				return true;
			}

			// See if can read object from database
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
		setMessage("Successfully read and wrote to database.");
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
