package org.transitclock.config;

import org.junit.Test;

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
import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.modules.Module;

public class createWebAgencyTest {
	
	@Test
	public void testwebagency(){
		try{
	String agencyId = "02";
	String hostName = "jdbc:hsqldb:mem://localhost/xdb";
	boolean active = true;
	String dbName = "02";
	String dbType = "hsql";
	String dbHost = "localhost";
	String dbUserName = "SA";
	String dbPassword = "";
	// Name of database where to store the WebAgency object
	String webAgencyDbName = "web";
	
	// Create the WebAgency object
	WebAgency webAgency = new WebAgency(agencyId, hostName, active, dbName,
			dbType, dbHost, dbUserName, dbPassword);
	System.out.println("Storing " + webAgency);
	
	// Store the WebAgency
	webAgency.store(webAgencyDbName);
	assertTrue(true);
	} catch (Exception e) {
		fail(e.toString());
		e.printStackTrace();
	}

	}
}
