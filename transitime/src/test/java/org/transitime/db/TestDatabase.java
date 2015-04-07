package org.transitime.db;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.Config;
import org.transitime.configData.DbSetupConfig;
import org.transitime.db.structs.AvlReport;

public class TestDatabase extends TestCase {

	static String fileName = "testConfig.xml";
	
	private static final Logger logger = LoggerFactory
			.getLogger(TestDatabase.class);

	public void testDatabase() {
		try {
									
			Config.readConfigFile(this.getClass().getClassLoader()
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

}
