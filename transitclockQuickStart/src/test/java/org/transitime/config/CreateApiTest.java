package org.transitime.config;

import static org.junit.Assert.*;

import java.net.URL;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;
import org.transitime.config.ConfigFileReader.ConfigException;
import org.transitime.config.ConfigValue.ConfigParamException;
import org.transitime.db.webstructs.ApiKey;
import org.transitime.db.webstructs.ApiKeyManager;

public class CreateApiTest {

	@Test
	public void test() {
		String fileName = "transiTimeconfig.xml";
		
		try {
			ConfigFileReader.processConfig(this.getClass().getClassLoader()
					.getResource(fileName).getPath());
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (ConfigParamException e) {
			
		}
		String name="Brendan";
		String url="http://www.transitime.org";
		String email="egan129129@gmail.com";
		String phone="123456789";
		String description="Foo";
		ApiKeyManager manager = ApiKeyManager.getInstance();
		ApiKey apiKey = manager.generateApiKey(name,
				url, email,
				phone, description);
		assert(manager.isKeyValid(apiKey.getKey()));
	}

}
