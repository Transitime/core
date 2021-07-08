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
package org.transitclock.applications;

import java.io.PrintWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.db.webstructs.ApiKey;
import org.transitclock.db.webstructs.ApiKeyManager;

/**
 * For creating a new API key.
 * 
 * @author Sean Crudden
 *
 */
public class CreateAPIKey {
	/**
	 * For testing and debugging. Currently creates a new key for an
	 * application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Options options = new Options();
		Option helpOption = new Option("h", "help", false,
				"Display usage and help info.");

		Option configOption = new Option("c", "config", true,
				"Specifies optional configuration file to read in.");
		Option nameOption = new Option("n", "name", true, "Application Name");
		Option urlOption = new Option("u", "url", true, "Application URL");
		Option emailOption = new Option("e", "email", true, "Email address");
		Option phoneOption = new Option("p", "phone", true, "Phone number");
		Option descriptionOption = new Option("d", "description", true,
				"Description");
		options.addOption(configOption);
		options.addOption(nameOption);
		options.addOption(emailOption);
		options.addOption(urlOption);
		options.addOption(phoneOption);
		options.addOption(descriptionOption);
		options.addOption(helpOption);

		// Parse the options
		CommandLineParser parser = new BasicParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("n") && cmd.hasOption("u")
					&& cmd.hasOption("e") && cmd.hasOption("p")
					&& cmd.hasOption("d")) {
				String configFile = null;

				// Read in the data from config file
				configFile = cmd.getOptionValue("c");
				if (configFile != null)
					ConfigFileReader.processConfig(configFile);

				ApiKeyManager manager = ApiKeyManager.getInstance();
				ApiKey apiKey = manager.generateApiKey(cmd.getOptionValue("n"),
						cmd.getOptionValue("u"), cmd.getOptionValue("e"),
						cmd.getOptionValue("p"), cmd.getOptionValue("d"));

				System.out.println(apiKey);
			} else {
				throw new Exception("All arguments required");
			}

		} catch (Exception e) {

			e.printStackTrace(System.out);
			printHelp(options);
		}
	}

	static void printHelp(Options options) {
		final String commandLineSyntax = "java -jar createApiKey.jar";
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, 80, // printedRowWidth
				commandLineSyntax, "args:", // header
				options, 2, // spacesBeforeOption
				2, // spacesBeforeOptionDescription
				null, // footer
				true); // displayUsage
		writer.close();
	}

}
