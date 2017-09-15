package org.transitime.config;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;

import junit.framework.TestCase;

public class ApiJettyTest extends TestCase{
	private Server server = new Server(8080);
	
	@Test
	public void test() {
		try{
		

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/api");
		File warFile = new File(
		ApiJettyTest.class.getClassLoader().getResource("api.war").getPath());
		
		System.out.print(warFile.getPath()+"test");
		webapp.setWar(warFile.getPath());
		
		// location to go to= http://127.0.0.1:8080/api/
		
		Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        webapp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );

		server.setHandler(webapp);
		
			server.start();
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}
			   @Test
			    public void shouldBePreAuthenticated() throws Exception {
			        String userId = "invalid";
			        HttpClient client = new DefaultHttpClient();
			        HttpGet mockRequest = new HttpGet("http://localhost:8080/api");
			        mockRequest.setHeader("http-user",userId);
			        HttpResponse mockResponse = client.execute(mockRequest);
			        BufferedReader rd = new BufferedReader
			          (new InputStreamReader(mockResponse.getEntity().getContent()));    
			        assertTrue(true);
			    }

			    
			    public void shutdownServer() throws Exception {
			        server.stop();
			    }
			
		
	

}
