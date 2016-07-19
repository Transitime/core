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
