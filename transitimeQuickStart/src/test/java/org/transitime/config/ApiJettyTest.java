package org.transitime.config;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;

public class ApiJettyTest {

	@Test
	public void test() {
		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/api");
		File warFile = new File(
                "C:\\Users\\Brendan\\Documents\\TransitimeTest\\core\\transitimeApi\\target\\api.war" );
		
		webapp.setWar(warFile.getAbsolutePath());
		// location to go to=
		// http://127.0.0.1:8080/ctx1/
		
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
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
