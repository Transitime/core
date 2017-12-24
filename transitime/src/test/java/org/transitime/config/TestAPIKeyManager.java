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


import junit.framework.TestCase;

import org.transitime.config.ConfigFileReader.ConfigException;
import org.transitime.config.ConfigValue.ConfigParamException;
import org.transitime.db.webstructs.ApiKey;
import org.transitime.db.webstructs.ApiKeyManager;

/**
 * 
 * @author Sean Crudden
 *
 */
public class TestAPIKeyManager extends TestCase {

	static String fileName = "transiTimeconfig.xml";

	protected void setUp() throws Exception {
//		super.setUp();
	}

	protected void tearDown() throws Exception {
//		super.tearDown();
	}

	
	public void testNoOp() {
	  
	}
	
	public void xtestAPIKeyManager() {
		try {

			ConfigFileReader.processConfig(this.getClass().getClassLoader()
					.getResource(fileName).getPath());
			
			ApiKeyManager manager = ApiKeyManager.getInstance();
			
			ApiKey apiKey = manager.generateApiKey("test",
					"http://127.0.0.1:8080/transitime", "test@test.com",
					"12345678", "test"); 
			
			assert(manager.isKeyValid(apiKey.getKey()));

		} catch (ConfigException | ConfigParamException e) {			
			fail(e.toString());
		}

	}
}