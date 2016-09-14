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
package org.transitime.config;

import java.util.List;

import org.transitime.config.ConfigFileReader.ConfigException;
import org.transitime.config.ConfigValue.ConfigParamException;
import org.transitime.configData.CoreConfig;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author Sean Crudden
 *
 */
@SuppressWarnings("deprecation")
public class TestConfig extends TestCase {

	static String fileName = "transiTimeconfig.xml";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNoOp() {
	  
	}
	
	public void xtestReadConfigFile() {
		try {
			// Load in config file
			ConfigFileReader.processConfig(this.getClass().getClassLoader()
					.getResource(fileName).getPath());

			// Get the module list param
			List<String> moduleList = CoreConfig.getOptionalModules();

			assertTrue(moduleList.get(0).equals(
					"org.transitime.avl.GtfsRealtimeModule"));
		} catch (ConfigException | ConfigParamException e) {			
			fail(e.toString());
		}

	}

}
