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

package org.transitime.avl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.StringConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;

/**
 * For reading in a batch of AVL data in CSV format and processing it. It only
 * reads a single batch of data, unlike the usual AVL modules that continuously
 * read data. This module is useful for debugging because can relatively easily
 * create a plain text CSV file of AVL data and see what the code does.
 * <p>
 * The AVL data is processed directly by this class by it calling
 * AvlProcessor.processAvlReport(avlReport). The messages do not go through
 * the JMS server and JMS server does not need to be running.
 * <p>
 * Note: the URL for the GTFS-realtime feed is obtained in this module
 * from CoreConfig.getCsvAvlFeedURI(). This means it can be set in the
 * config file or as a Java property on the command line.
 *
 * @author SkiBu Smith
 *
 */
public class BatchCsvAvlFeedModule extends Module {

	/*********** Configurable Parameters for this module ***********/
	private static String getCsvAvlFeedFileName() {
		return csvAvlFeedFileName.getValue();
	}
	private static StringConfigValue csvAvlFeedFileName =
			new StringConfigValue("transitime.avl.csvAvlFeedFileName", 
					"/Users/Mike/cvsAvlData/testAvlData.csv");

	/****************** Logging **************************************/
	private static final Logger logger = LoggerFactory
			.getLogger(BatchCsvAvlFeedModule.class);

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	public BatchCsvAvlFeedModule(String projectId) {
		super(projectId);
	}

	/* 
	 * Reads in AVL reports from CSV file and processes them.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		List<AvlReport> avlReports = 
				(new AvlCsvReader(getCsvAvlFeedFileName())).get();
		
		// Process the AVL Reports read in.
		for (AvlReport avlReport : avlReports) {
			logger.info("Processing avlReport={}", avlReport);
			
			// Update the Core SystemTime to use this AVL time
			Core.getInstance().setSystemTime(avlReport.getTime());

			AvlProcessor.getInstance().processAvlReport(avlReport);
		}

		// Kill off the whole program because done processing the AVL data
		System.exit(0);
	}

}
