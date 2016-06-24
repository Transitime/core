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
package org.transitime.config;

import static org.junit.Assert.*;
import java.net.URL;
import junit.framework.TestCase;
import org.transitime.applications.GtfsFileProcessor;
import org.junit.Test;
/**
 * 
 * @author Brendan Egan
 *
 */
public class TestGTFSFileProcessor extends TestCase {

	@Test
	public void test() {
		try {
			URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
        	String configFilePath=configFile.getPath();

        	URL gtfsFile = this.getClass().getClassLoader().getResource("Capmetro.zip");
        	String gtfsFilePath=gtfsFile.getPath();
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
			boolean trimPathBeforeFirstStopOfTrip = false;

			GtfsFileProcessor testprocessor = new GtfsFileProcessor(configFilePath, notes, gtfsUrl, gtfsZipFileName,
					unzipSubdirectory, gtfsDirectoryName, supplementDir, regexReplaceListFileName, pathOffsetDistance,
					maxStopToPathDistance, maxDistanceForEliminatingVertices, defaultWaitTimeAtStopMsec, maxSpeedKph,
					maxTravelTimeSegmentLength, configRev, shouldStoreNewRevs, trimPathBeforeFirstStopOfTrip);
			 testprocessor.process();
			assertTrue(true);
		} catch (Exception e) {
			System.out.println(e);
			fail(e.toString());
		}

	}

}
