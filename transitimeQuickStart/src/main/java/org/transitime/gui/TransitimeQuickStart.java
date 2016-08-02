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
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.applications.GtfsFileProcessor;
import org.transitime.config.ConfigFileReader;
import org.transitime.configData.CoreConfig;
import org.transitime.db.webstructs.ApiKey;
import org.transitime.db.webstructs.ApiKeyManager;
import org.transitime.db.webstructs.WebAgency;
import org.transitime.modules.Module;

/**
 * 
 * @author Brendan Egan
 *
 */
public class TransitimeQuickStart {
	private static final Logger logger = LoggerFactory.getLogger(TransitimeQuickStart.class);
	private ApiKey apiKey = null;
	static Server webserver = new Server(8080);
	WebAppContext apiapp = null;
	WebAppContext webapp = null;

	public static void main(String args[]) {
		/*
		 * WelcomePanel window = new WelcomePanel(); window.WelcomePanelstart();
		 */

		InputPanel windowinput = new InputPanel();
		windowinput.InputPanelstart();

	}

	public void startGtfsFileProcessor(String gtfsZipFileName) {

		try {
			URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
			String configFilePath = configFile.getPath();
			// String
			// configFilePath="..//transitimeQuickStart//src//main//resources//transiTimeconfig.xml";
			String gtfsFilePath;
			if (gtfsZipFileName == null) {
				URL gtfsFile = this.getClass().getClassLoader().getResource("collins.zip");
				gtfsFilePath = gtfsFile.getPath();
				gtfsZipFileName = gtfsFilePath;
			} else {
				gtfsFilePath = gtfsZipFileName;
			}
			String notes = null;
			String gtfsUrl = null;
			// String gtfsZipFileName = gtfsFilePath;
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
			logger.info("startGtfsFileProcessor successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void createApiKey() {
		try {

			String fileName = "transiTimeconfig.xml";

			ConfigFileReader.processConfig(this.getClass().getClassLoader().getResource(fileName).getPath());

			String name = "Brendan";
			String url = "http://www.transitime.org";
			String email = "egan129129@gmail.com";
			String phone = "123456789";
			String description = "Foo";
			ApiKeyManager manager = ApiKeyManager.getInstance();
			apiKey = manager.generateApiKey(name, url, email, phone, description);

			List<ApiKey> keys = manager.getApiKeys();
			for (ApiKey key : keys) {
				logger.info(key.getKey());
			}
			
			logger.info("createApiKey successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void startCore(String realtimefeedURL, String loglocation) {
		try {
			URL configFile = this.getClass().getClassLoader().getResource("transiTimeconfig.xml");
			String configFilePath = configFile.getPath();
			// TODO this by using the config file
			String agencyid = System.getProperties().getProperty("transitime.core.agencyId");
			System.getProperties().setProperty("transitime.core.configRevStr", "0");
			System.getProperties().setProperty("transitime.core.agencyId", "agencyid");
			// uses default if nothing entered
			if (loglocation.equals("")) {
				// uses current directory if one not specified
				loglocation = System.getProperty("user.dir");
			}
			System.getProperties().setProperty("transitime.logging.dir", loglocation);
			System.getProperties().setProperty("transitime.configFiles", configFilePath);
			// only set the paramater for realtimeURLfeed if specified by user
			if (!realtimefeedURL.equals("")) {
				System.getProperties().setProperty("transitime.avl.url", realtimefeedURL);
			}

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
			// start servers
			Core.startRmiServers(agencyid);
			logger.info("startCore successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void startDatabase() {
		try {
			String dbPath = "mem:test;sql.enforce_strict_size=true";

			String serverProps;
			String url;
			String user = "sa";
			String password = "";
			org.hsqldb.server.Server serverdb;
			boolean isNetwork = true;
			boolean isHTTP = false; // Set false to test HSQL protocol, true to
									// test
									// HTTP, in which case you can use
									// isUseTestServlet to target either HSQL's
									// webserver, or the Servlet server-mode
			boolean isServlet = false;

			serverdb = new org.hsqldb.server.Server();
			serverdb.setDatabaseName(0, "test");
			serverdb.setDatabasePath(0, dbPath);
			serverdb.setLogWriter(null);
			serverdb.setErrWriter(null);
			serverdb.start();
			logger.info("startDatabase successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void addApi(String apikey) {
		try {
			// Server server = new Server(8080);

			apiapp = new WebAppContext();
			apiapp.setContextPath("/api");
			File warFile = new File(TransitimeQuickStart.class.getClassLoader().getResource("api.war").getPath());
			apiapp.setWar(warFile.getPath());

			// location to go to=
			// http://127.0.0.1:8080/api/v1/key/1727f2a/agency/02/command/routes?format=json
			Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(webserver);
			classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
					"org.eclipse.jetty.annotations.AnnotationConfiguration");
			apiapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
					".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
			// Set the path to the override descriptor, based on your
			// $(jetty.home)
			// directory
			apiapp.setOverrideDescriptor("override-web.xml");

			// server.start();
			// server.join();
			logger.info("add api successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void addWebapp() {
		try {
			// Server server = new Server(8081);
			
			webapp = new WebAppContext();
			webapp.setContextPath("/webapp");
			File warFile = new File(TransitimeQuickStart.class.getClassLoader().getResource("web.war").getPath());

			webapp.setWar(warFile.getPath());

			// location to go to=
			// http://127.0.0.1:8080/api/v1/key/1727f2a/agency/02/command/routes?format=json

			webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
					".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
			webapp.setOverrideDescriptor("override-web.xml");
		
			System.setProperty("transitime.apikey",apiKey.getKey());
			// server.join();
			logger.info("add Webapp successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void startJetty() {
		try {
			HandlerCollection handlerCollection = new HandlerCollection();
			handlerCollection.setHandlers(new Handler[] { apiapp, webapp });
			webserver.setHandler(handlerCollection);
			webserver.start();
			/*apiapp.start();
			webapp.start();*/
			logger.info("started Jetty successful");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void webAgency() {
		try {
			String agencyId = "02";
			String hostName = "127.0.0.1";
			boolean active = true;
			String dbName = "02";
			String dbType = "hsql";
			String dbHost = "http://127.0.0.1:8080/";
			String dbUserName = "sa";
			String dbPassword = "";
			// Name of database where to store the WebAgency object
			String webAgencyDbName = "test";

			// Create the WebAgency object
			WebAgency webAgency = new WebAgency(agencyId, hostName, active, dbName, dbType, dbHost, dbUserName,
					dbPassword);
			System.out.println("Storing " + webAgency);

			// Store the WebAgency
			webAgency.store(webAgencyDbName);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ApiKey getApiKey() {
		return apiKey;
	}

	public void setApiKey(ApiKey apiKey) {
		this.apiKey = apiKey;
	}
}
