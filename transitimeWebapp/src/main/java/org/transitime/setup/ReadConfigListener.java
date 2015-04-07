package org.transitime.setup;

import javax.servlet.ServletContextEvent;

import org.transitime.config.Config;
import org.transitime.config.Config.ConfigException;
import org.transitime.config.ConfigValue.ConfigParamException;

public class ReadConfigListener implements javax.servlet.ServletContextListener {	 

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {	
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		try {
			String configFileLocation=arg0.getServletContext().getInitParameter("transitime_config_file_location");			
			Config.processConfig(configFileLocation);
		} catch (Exception e) {			
			e.printStackTrace();
		} 
	}

}
