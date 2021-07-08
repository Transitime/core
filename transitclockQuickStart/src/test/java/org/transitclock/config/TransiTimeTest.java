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
package org.transitclock.config;

import static org.junit.Assert.*;
import java.net.URL;
import java.util.List;
import junit.framework.TestCase;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.applications.GtfsFileProcessor;
import org.transitclock.configData.CoreConfig;
import org.transitclock.modules.Module;

/**
 * 
 * @author Brendan Egan
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransiTimeTest  {
	private static final Logger logger = LoggerFactory.getLogger(TransiTimeTest.class);

	@Test
	public void test_1_GTFSfileprocessor() {
		try {
			URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
			String configFilePath = configFile.getPath();

			URL gtfsFile = this.getClass().getClassLoader().getResource("collins.zip");
			String gtfsFilePath = gtfsFile.getPath();
			String notes = null;
			String gtfsUrl = null;
			String gtfsZipFileName = gtfsFilePath;
			String unzipSubdirectory = null;
			String gtfsDirectoryName = null;
			String supplementDir = null;
			String regexReplaceListFileName = null;
			double pathOffsetDistance = 0.0;
			double maxStopToPathDistance = 60.0;
			double maxDistanceForEliminatingVertices = 3.0;
			int defaultWaitTimeAtStopMsec = 10000;
			double maxSpeedKph = 97;
			double maxTravelTimeSegmentLength = 1000.0;
			int configRev = -1;
			boolean shouldStoreNewRevs = true;
			boolean shouldDeleteRevs = false;
			boolean trimPathBeforeFirstStopOfTrip = false;
			

			GtfsFileProcessor testprocessor = new GtfsFileProcessor(configFilePath, notes, gtfsUrl, gtfsZipFileName,
					unzipSubdirectory, gtfsDirectoryName, supplementDir, regexReplaceListFileName, pathOffsetDistance,
					maxStopToPathDistance, maxDistanceForEliminatingVertices, defaultWaitTimeAtStopMsec, maxSpeedKph,
					maxTravelTimeSegmentLength, configRev, shouldStoreNewRevs,shouldDeleteRevs,  trimPathBeforeFirstStopOfTrip);
			testprocessor.process();
			assertTrue(true);
		} catch (Exception e) {
			//fail(e.toString());
			e.printStackTrace();
		}

	}

	@Test
	public void test_2_Core() {

		String agencyid = "02";
		System.getProperties().setProperty("transitclock.core.configRevStr", "0");
		System.getProperties().setProperty("transitclock.core.agencyId", "02");
		
		//Sending VM arguments
		System.getProperties().setProperty("transitclock.logging.dir",
				"C:\\Users\\Brendan\\Documents\\TransitimeTest\\core\\transitime\\logs\\");
		System.getProperties().setProperty("transitclock.configFiles",
				"C:\\Users\\Brendan\\Documents\\TransitimeTest\\core\\transitime\\src\\main\\resources\\transiTimeconfig.xml");
		try {

			// Initialize the core now
			Core.createCore();
			
			List<String> optionalModuleNames = CoreConfig.getOptionalModules();
			if (optionalModuleNames.size() > 0)
				logger.info("Starting up optional modules specified via "
						+ "transitclock.modules.optionalModulesList param:");
			else
				logger.info("No optional modules to start up.");
			for (String moduleName : optionalModuleNames) {
				logger.info("Starting up optional module " + moduleName);
				Module.start(moduleName);
			}
			//start servers
			Core.startRmiServers(agencyid);
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}

	}
}
