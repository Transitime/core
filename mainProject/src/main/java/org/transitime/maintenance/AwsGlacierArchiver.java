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

package org.transitime.maintenance;

import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An archiver for archiving files to Amazon AWS Glacier.
 *
 * @author SkiBu Smith
 *
 */
public class AwsGlacierArchiver implements ArchiverInterface {

	private final AwsGlacier glacier;
	private final String vaultName;
	private final String logDirectory;
	
	private static final Logger logger = LoggerFactory
			.getLogger(AwsGlacierArchiver.class);

	/********************** Member Functions **************************/

	/**
	 * Constructs an archiver with all the information that it needs.
	 * 
	 * @param region
	 *            Region of the AWS Glacier vault
	 * @param vaultName
	 *            Name of the AWS Glacier vault
	 * @param logDirectory
	 *            Directory where log of uploads is written for the vault.
	 */
	public AwsGlacierArchiver(String region, String vaultName, 
			String logDirectory) {
		this.glacier = new AwsGlacier(region);
		this.vaultName = vaultName;
		this.logDirectory = logDirectory;
	}
	
	/**
	 * Adds record of upload to the log file for the vault. This way don't need
	 * to read the vault contents from Glacier, which is a nuisance since it
	 * takes hours.
	 * 
	 * @param description
	 * @param archiveId
	 */
	private void addToArchiveLog(String description, String archiveId) {
		String logFile = logDirectory + "/" + vaultName + "_vault_log.txt";
		try {
			// Open up log file in append mode
			FileWriter fw = new FileWriter(logFile, true);
			fw.write(vaultName + ", " + description + ", " + archiveId + "\n");
			fw.close();
		} catch (IOException e) {
			logger.error("Exception occurred when adding description=\"{}\" "
					+ "to vaultName=\"{}\" for logFile=\"{}\"",
					description, vaultName, logFile, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.maintenance.ArchiverInterface#upload(java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(String fileName, String description) {
		logger.info("Uploading to Amazon AWS Glacier vaultName=\"{}\" the "
				+ "file=\"{}\" with description=\"{}\"", 
				vaultName, fileName, description);
		String archiveId = glacier.upload(vaultName, fileName, description);
		
		if (archiveId != null) {
			// Was successful so add ID to log file for the vault so that
			// can read archived files by looking up the appropriate archive
			// ID associated with a description.
			addToArchiveLog(description, archiveId);
		} else {
			// Was not successful so log problem
			logger.error("Error occurred uploading to vaultName=\"{}\" the "
					+ "fileName=\"{}\" with description=\"{}\"", 
					vaultName, fileName, description);
		}
		
		return archiveId;
	}

}
