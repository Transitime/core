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
package org.transitime.db;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.ConfigFileReader;
import org.transitime.configData.DbSetupConfig;
import org.transitime.db.structs.AvlReport;

/**
 * 
 * @author Sean Crudden
 *
 */
public class TestDatabase extends TestCase {

	static String fileName = "transiTimeconfig.xml";
	
	private static final Logger logger = LoggerFactory
			.getLogger(TestDatabase.class);

	public void testNoop() {
	  // expects a database
	}
	
	public void xtestDatabase() {
		try {
									
			ConfigFileReader.processConfig(this.getClass().getClassLoader()
					.getResource(fileName).getPath());
						
			List<AvlReport> avlReports = AvlReport.getAvlReportsFromDb(
					new Date(), // beginTime
					new Date(), // endTime
					null, // vehicleId
					null); // SQL clause
			
			assertTrue(avlReports!=null);					
		} catch (Exception e) {			
			logger.error("Error occurred when trying to access database for "
					+ "dbName={}. {}", DbSetupConfig.getDbName(),
					e.getMessage(), e);
			fail(e.toString());
		}
	}
	
	public void testCreateAgency()
	{
		
	}

}
