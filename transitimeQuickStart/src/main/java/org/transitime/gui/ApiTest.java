package org.transitime.gui;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class ApiTest {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		ServletContextHandler context0 = new ServletContextHandler(ServletContextHandler.SESSIONS);
		String warlocation = "C:\\Users\\Brendan\\Documents\\TransitimeTest\\core\\transitimeApi\\target\\api.war";
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/ctx1");
		//for test
		//webapp.setResourceBase("src/main/webapp");
		
		webapp.setWar(warlocation);
		// http://127.0.0.1:8080/ctx1/
		webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*jstl.*\\.jar$");

		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList
				.setServerDefault(server);
		 classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
		 "", "");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
		 "org.eclipse.jetty.annotations.AnnotationConfiguration");

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { context0, webapp });

		server.setHandler(contexts);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
