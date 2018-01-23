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
package org.transitime.testapp;

import org.hsqldb.server.Server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    	 public void testApp()
    	    {
    	    	String  dbPath = "mem:test;sql.enforce_strict_size=true";
    	    	
    	        String  serverProps;
    	        String  url;
    	        String  user     = "sa";
    	        String  password = "";
    	        Server  server;
    	        boolean isNetwork = true;
    	        boolean isHTTP    = false;    // Set false to test HSQL protocol, true to test HTTP, in which case you can use isUseTestServlet to target either HSQL's webserver, or the Servlet server-mode
    	        boolean isServlet = false;
    	        
    	    	server=new Server();
    	    	server.setPort(8085);
    	    	server.setDatabaseName(0, "test");
    	        server.setDatabasePath(0, dbPath);
    	        server.setLogWriter(null);
    	        server.setErrWriter(null);
    	        server.start();
    	    	
    	    	
    	        assertTrue( true );
    	    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
}
