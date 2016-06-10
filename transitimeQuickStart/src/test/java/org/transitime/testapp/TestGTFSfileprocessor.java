package org.transitime.testapp;
import
import static org.junit.Assert.*;
import junit.framework.TestCase;
import trasitime.src.main.java.org.transitime.applications.GtfsFileProcessor;
import org.junit.Test;

public class TestGTFSfileprocessor extends TestCase{

	@Test
	public void testProcess() {
		fail("Not yet implemented");
	}

	
		
	        @Test
			public void Testmain()
			{
			  String[] args ={"-c,C:\\Users\\Brendan\\Documents\\Transitime\\core\\transiTimeconfig.xml,-gtfsDirectoryName,C:\\Users\\Brendan\\Documents\\GTFS\\GTFSAUS\\,-storeNewRevs,-maxTravelTimeSegmentLength 1000"}; //for example
			  GtfsFileProcessor Test = new GtfsFileProcessor(args);
			  Test.main(args);
			}
		    
		
	}


