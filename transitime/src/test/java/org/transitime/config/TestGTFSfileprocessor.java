package org.transitime.config;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import org.transitime.applications.GtfsFileProcessor;
import org.junit.Test;

public class TestGtfsFileProcessor extends TestCase{

	@Test
	public void testProcess() {
		fail("Not yet implemented");
	}

	
		
	        @Test
			public void Testmain()
			{
	        	String configFile="C:\\Users\\Brendan\\Documents\\Transitime\\core\\transiTimeconfig.xml";
	        	String notes=null;
	        	String gtfsUrl=null;
	        	String gtfsZipFileName=null;
	        	String unzipSubdirectory=null;
	        	String gtfsDirectoryName="C:\\Users\\Brendan\\Documents\\GTFS\\GTFSAUS\\";
	        	String supplementDir=null;
	        	String regexReplaceListFileName=null;
	        	double pathOffsetDistance=0.0;
	        	double maxStopToPathDistance=60.0;
	        	double maxDistanceForEliminatingVertices=3.0;
	        	int defaultWaitTimeAtStopMsec=10000;
	        	double maxSpeedKph=97;
	        	double maxTravelTimeSegmentLength=1000.0;
	        	int configRev=-1;
	        	boolean shouldStoreNewRevs=true;
	        	boolean trimPathBeforeFirstStopOfTrip=false;
			  String args1 ="-c C:\\Users\\Brendan\\Documents\\Transitime\\core\\transiTimeconfig.xml -gtfsDirectoryName C:\\Users\\Brendan\\Documents\\GTFS\\GTFSAUS\\ -storeNewRevs -maxTravelTimeSegmentLength 1000"; //for example
			  GtfsFileProcessor Test = new GtfsFileProcessor(args1);
			  GtfsFileProcessor Test1 = new GtfsFileProcessor("-c",configFile,"-n",notes,"-gtfsUrl",gtfsUrl,"-gtfsZipFileName",gtfsZipFileName,"-unzipSubdirectory",unzipSubdirectory,"-gtfsDirectoryName",gtfsDirectoryName,"-supplementDir",supplementDir,"-regexReplaceFile",regexReplaceListFileName,"-pathOffsetDistance",pathOffsetDistance,"-maxStopToPathDistance",maxStopToPathDistance,"-maxDistanceForEliminatingVertices",maxDistanceForEliminatingVertices,"-defaultWaitTimeAtStopMsec",defaultWaitTimeAtStopMsec,"-maxSpeedKph",maxSpeedKph,"-maxTravelTimeSegmentLength",maxTravelTimeSegmentLength,"-configRev",configRev,"-shouldStoreNewRevs",shouldStoreNewRevs,"-trimPathBeforeFirstStopOfTrip",trimPathBeforeFirstStopOfTrip);
			  GtfsFileProcessor Test2 = new GtfsFileProcessor(configFile,notes,gtfsUrl,gtfsZipFileName,unzipSubdirectory,gtfsDirectoryName,supplementDir,regexReplaceListFileName,pathOffsetDistance,maxStopToPathDistance,maxDistanceForEliminatingVertices,defaultWaitTimeAtStopMsec,maxSpeedKph,maxTravelTimeSegmentLength,configRev,shouldStoreNewRevs,trimPathBeforeFirstStopOfTrip);
			  Assert();
			}
		    
		
	}


