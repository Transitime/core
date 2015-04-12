package org.transitime.config;


import junit.framework.TestCase;

import org.transitime.config.Config.ConfigException;
import org.transitime.db.webstructs.ApiKey;
import org.transitime.db.webstructs.ApiKeyManager;

public class TestAPIKeyManager extends TestCase {

	static String fileName = "testConfig.xml";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAPIKeyManager() {
		try {

			Config.readConfigFile(this.getClass().getClassLoader()
					.getResource(fileName).getPath());
			
			ApiKeyManager manager = ApiKeyManager.getInstance();
			
			ApiKey apiKey = manager.generateApiKey("test",
					"http://127.0.0.1:8080/transitime", "test@test.com",
					"12345678", "test");
			
			assert(manager.isKeyValid(apiKey.getKey()));

		} catch (ConfigException e) {
			
			fail(e.toString());
		}

	}
}