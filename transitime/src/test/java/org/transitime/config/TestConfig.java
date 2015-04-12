package org.transitime.config;

import java.io.File;
import java.util.List;

import org.transitime.config.Config.ConfigException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestConfig extends TestCase {

	static String fileName = "testConfig.xml";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testReadConfigFile() {
		try {

			Config.readConfigFile(this.getClass().getClassLoader()
					.getResource(fileName).getPath());

			List<String> moduleList = Config.getConfigFileData().get(
					"transitime.modules.optionalModulesList");

			Assert.assertTrue(moduleList.get(0).equals(
					"org.transitime.avl.GtfsRealtimeModule"));

		} catch (ConfigException e) {
			
			fail(e.toString());
		}

	}

}
