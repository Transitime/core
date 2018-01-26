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

package org.transitclock.applications;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.DbSetupConfig;
import org.transitclock.db.structs.AvlReport;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class DbTest {

	private static final Logger logger = LoggerFactory.getLogger(DbTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			List<AvlReport> avlReports = AvlReport.getAvlReportsFromDb(
					new Date(), // beginTime
					new Date(), // endTime
					null, // vehicleId
					null); // SQL clause
			if (avlReports != null)
				logger.info("Successfully connected to the database!");
		} catch (Exception e) {
			logger.error("Error occurred when trying to access database for "
					+ "dbName={}. {}", DbSetupConfig.getDbName(), e.getMessage(), e);
		}

	}

	/********************** Member Functions **************************/

}
