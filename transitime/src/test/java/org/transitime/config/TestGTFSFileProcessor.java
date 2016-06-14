package org.transitime.config;

import static org.junit.Assert.*;

import java.net.URL;

import junit.framework.TestCase;
import org.transitime.applications.GtfsFileProcessor;
import org.junit.Test;

public class TestGTFSFileProcessor extends TestCase {

	@Test
	public void test() {
		try {
			URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
        	String configFilePath=configFile.getPath();

        	URL gtfsFile = this.getClass().getClassLoader().getResource("gtfs.zip");
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
