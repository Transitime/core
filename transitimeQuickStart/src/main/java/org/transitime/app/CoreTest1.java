package org.transitime.app;
import org.transitime.applications.Core;
import org.transitime.configData.AgencyConfig;



public class CoreTest1{
	
//	@Test
	public void  test()
	{
	String configrev="0 /dev/null 2>&1";
	String agencyid="02";
	Core testcore = new Core(agencyid);
	//Core test=testcore.createCore();
	/*try{*/
			// Write pid file so that monit can automatically start
			// or restart this application
			//PidFile.createPidFile(CoreConfig.getPidFileDirectory()
			//		+ AgencyConfig.getAgencyId() + ".pid");
			
			
			// Initialize the core now
			testcore =testcore.createCore();
						
				
			
			// Start the RMI Servers so that clients can obtain data
			// on predictions, vehicles locations, etc.
			agencyid = AgencyConfig.getAgencyId();			
			testcore.startRmiServers(agencyid);
	/*	} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	*/
	}
}









