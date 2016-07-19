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
package org.transitime.gui;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.applications.GtfsFileProcessor;
import org.transitime.config.ConfigFileReader;
import org.transitime.config.ConfigFileReader.ConfigException;
import org.transitime.config.ConfigValue.ConfigParamException;
import org.transitime.configData.CoreConfig;
import org.transitime.db.webstructs.ApiKey;
import org.transitime.db.webstructs.ApiKeyManager;
import org.transitime.modules.Module;
/**
 * 
 * @author Brendan Egan
 *
 */
public class TransitimeQuickStart {
	private static final Logger logger = LoggerFactory.getLogger(TransitimeQuickStart.class);
	private ApiKey apiKey;
	
	public static void main (String args[])
	{
		WelcomePanel window = new WelcomePanel();
		window.WelcomePanelstart();
		/*InputPanel windowinput = new InputPanel();
		windowinput.InputPanelstart();
		OutputPanel windowoutput = new OutputPanel();
		windowoutput.OutputPanelstart();*/
	}
	public void StartGtfsFileProcessor(String gtfsZipFileName)
	{
		URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
    	String configFilePath=configFile.getPath();
		//String configFilePath="..//transitimeQuickStart//src//main//resources//transiTimeconfig.xml";
    	String gtfsFilePath;
    	if(gtfsZipFileName==null)
    	{
    		URL gtfsFile = this.getClass().getClassLoader().getResource("collins.zip");
    		gtfsFilePath=gtfsFile.getPath();
    		gtfsZipFileName=gtfsFilePath;
    	}
    	else
    	{
    	gtfsFilePath=gtfsZipFileName;
    	}
		String notes = null;
		String gtfsUrl = null;
		//String gtfsZipFileName = gtfsFilePath;
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

		GtfsFileProcessor processor = new GtfsFileProcessor(configFilePath, notes, gtfsUrl, gtfsZipFileName,
				unzipSubdirectory, gtfsDirectoryName, supplementDir, regexReplaceListFileName, pathOffsetDistance,
				maxStopToPathDistance, maxDistanceForEliminatingVertices, defaultWaitTimeAtStopMsec, maxSpeedKph,
				maxTravelTimeSegmentLength, configRev, shouldStoreNewRevs, trimPathBeforeFirstStopOfTrip);
		 processor.process();
	}
	public ApiKey CreateApikey()
	{
		String fileName = "transiTimeconfig.xml";
		try {
			ConfigFileReader.processConfig(this.getClass().getClassLoader()
					.getResource(fileName).getPath());
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (ConfigParamException e) {
			
		}
		String name="Brendan";
		String url="http://www.transitime.org";
		String email="egan129129@gmail.com";
		String phone="123456789";
		String description="Foo";
		ApiKeyManager manager = ApiKeyManager.getInstance();
		apiKey = manager.generateApiKey(name,
				url, email,
				phone, description);
		
		List<ApiKey> keys = manager.getApiKeys();
		for(ApiKey key:keys)
		{
			System.out.print(key);
		}
		return apiKey;
	}
	public void StartJettyapi(String apikey){
		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/api");
		File warFile = new File(
		ApiTest.class.getClassLoader().getResource("api.war").getPath());
		
		System.out.print(warFile.getPath()+"test");
		webapp.setWar(warFile.getPath());
		
		// location to go to= http://127.0.0.1:8080/api/v1/key/1727f2a/agency/02/command/routes?format=json
		
		Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        webapp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );

		server.setHandler(webapp);
		try {
			server.start();
			//server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void StartCore(String realtimefeedURL,String loglocation)
	{
		URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
    	String configFilePath=configFile.getPath(); 
    	
		String agencyid = "02";
		System.getProperties().setProperty("transitime.core.configRevStr", "0");
		System.getProperties().setProperty("transitime.core.agencyId", "02");
		//uses default if nothing entered 
		if(loglocation.equals(""))
		{
			//uses current directory if one not specified
			 loglocation= System.getProperty("user.dir");
		}		
		System.getProperties().setProperty("transitime.logging.dir",
				loglocation);
		System.getProperties().setProperty("transitime.configFiles",
				configFilePath);
		//only set the paramater for realtimeURLfeed if specified by user
		if(!realtimefeedURL.equals(""))
		{
	System.getProperties().setProperty("transitime.avl.url",
				realtimefeedURL);
		}
		try {
			// Initialize the core now
			Core.createCore();
			List<String> optionalModuleNames = CoreConfig.getOptionalModules();
			if (optionalModuleNames.size() > 0)
				logger.info("Starting up optional modules specified via "
						+ "transitime.modules.optionalModulesList param:");
			else
				logger.info("No optional modules to start up.");
			for (String moduleName : optionalModuleNames) {
				logger.info("Starting up optional module " + moduleName);
				Module.start(moduleName);
			}
			//start servers
			Core.startRmiServers(agencyid);
		} catch (Exception e) {
			//fail(e.toString());
			e.printStackTrace();
		}
	}
	public void StartJettyWebapp(){
		Server server = new Server(8081);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/webapp");
		File warFile = new File(
		ApiTest.class.getClassLoader().getResource("web.war").getPath());
		
		
		webapp.setWar(warFile.getPath());
		
		// location to go to= http://127.0.0.1:8080/api/v1/key/1727f2a/agency/02/command/routes?format=json
		
		Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        webapp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );

		server.setHandler(webapp);
		try {
			server.start();
			//server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void startDatabase()
	{

    	String  dbPath = "mem:test;sql.enforce_strict_size=true";
    	
        String  serverProps;
        String  url;
        String  user     = "sa";
        String  password = "";
        org.hsqldb.server.Server server;
        boolean isNetwork = true;
        boolean isHTTP    = false;    // Set false to test HSQL protocol, true to test HTTP, in which case you can use isUseTestServlet to target either HSQL's webserver, or the Servlet server-mode
        boolean isServlet = false;
        
    	server=new org.hsqldb.server.Server();
    	server.setPort(8085);
    	server.setDatabaseName(0, "test");
        server.setDatabasePath(0, dbPath);
        server.setLogWriter(null);
        server.setErrWriter(null);
        server.start();
	}
}


