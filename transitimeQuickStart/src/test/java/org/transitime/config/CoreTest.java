package org.transitime.config;
import org.transitime.applications.Core;
import static org.junit.Assert.*;
import java.net.URL;
import junit.framework.TestCase;
import org.transitime.applications.GtfsFileProcessor;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.ConfigFileReader;
import org.transitime.configData.AgencyConfig;
import org.transitime.configData.CoreConfig;
import org.transitime.core.ServiceUtils;
import org.transitime.core.TimeoutHandlerModule;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.hibernate.DataDbLogger;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ActiveRevisions;
import org.transitime.db.structs.Agency;
import org.transitime.gtfs.DbConfig;
import org.transitime.ipc.servers.CommandsServer;
import org.transitime.ipc.servers.ConfigServer;
import org.transitime.ipc.servers.PredictionsServer;
import org.transitime.ipc.servers.ServerStatusServer;
import org.transitime.ipc.servers.VehiclesServer;
import org.transitime.modules.Module;
import org.transitime.monitoring.PidFile;
import org.transitime.utils.SettableSystemTime;
import org.transitime.utils.SystemTime;
import org.transitime.utils.SystemCurrentTime;
import org.transitime.utils.Time;


import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.ConfigFileReader;
import org.transitime.configData.AgencyConfig;
import org.transitime.configData.CoreConfig;
import org.transitime.core.ServiceUtils;
import org.transitime.core.TimeoutHandlerModule;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.hibernate.DataDbLogger;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ActiveRevisions;
import org.transitime.db.structs.Agency;
import org.transitime.gtfs.DbConfig;
import org.transitime.ipc.servers.CommandsServer;
import org.transitime.ipc.servers.ConfigServer;
import org.transitime.ipc.servers.PredictionsServer;
import org.transitime.ipc.servers.ServerStatusServer;
import org.transitime.ipc.servers.VehiclesServer;
import org.transitime.modules.Module;
import org.transitime.monitoring.PidFile;
import org.transitime.utils.SettableSystemTime;
import org.transitime.utils.SystemTime;
import org.transitime.utils.SystemCurrentTime;
import org.transitime.utils.Time;
import junit.framework.TestCase;
public class CoreTest {
	
	/*@Test
	public void  test()
	{
	String configrev="0 /dev/null 2>&1";
	String agencyid="02";
	Core testcore = new Core(agencyid);
	
	//Core test=testcore.createCore();
	try{
			// Write pid file so that monit can automatically start
			// or restart this application
			//PidFile.createPidFile(CoreConfig.getPidFileDirectory()
			//		+ AgencyConfig.getAgencyId() + ".pid");
			
			
			// Initialize the core now
			testcore=testcore.createCore();
						
				
			
			// Start the RMI Servers so that clients can obtain data
			// on predictions, vehicles locations, etc.
			//String agencyId = AgencyConfig.getAgencyId();			
			//testcore.startRmiServers(agencyId);
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	
	}*/
}









