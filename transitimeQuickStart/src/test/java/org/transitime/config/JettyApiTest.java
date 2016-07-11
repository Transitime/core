package org.transitime.config;

import static org.junit.Assert.*;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;

public class JettyApiTest {

		 public static void main(String[] args) throws Exception
		    {
		        Server server = new Server(8080);
		 
		        ServletContextHandler context0 = new ServletContextHandler(ServletContextHandler.SESSIONS);
		        context0.setContextPath("/ctx0");
		        String warlocation="C:\\Users\\Brendan\\Documents\\jetty\\my-app\\target\\hello-world-0.1-SNAPSHOT.war";
		        WebAppContext webapp = new WebAppContext();
		        webapp.setContextPath("/ctx1");
		        webapp.setWar(warlocation);
		        //http://127.0.0.1:8080/ctx1/
		 
		        ContextHandlerCollection contexts = new ContextHandlerCollection();
		        contexts.setHandlers(new Handler[] { context0, webapp });
		 
		        server.setHandler(contexts);
		 
		        server.start();
		        server.join();
		    }

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
