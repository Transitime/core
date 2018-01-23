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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.configData.AgencyConfig;
import org.transitime.logging.Markers;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * A module that runs in a separate thread that archives old log files as
 * needed.
 *
 * @author SkiBu Smith
 *
 */
public class ArchiveOldFilesModule extends Module {
	
	/******************** Parameters ************************************/

	private static StringConfigValue timeToArchive =
			new StringConfigValue("transitime.maintenance.timeToArchive", 
					"01:00:00", // 1 am
					"Specified time of day that should archive files. Should "
					+ "usually be run in middle of night when load is less. "
					+ "Time is in the format HH:MM:SS");

	private static StringConfigValue awsRegion =
			new StringConfigValue("transitime.maintenance.awsRegion",
					AwsGlacier.OREGON_REGION,
					"The region for Amazon AWS where files should be archive "
					+ "to.");

	private static StringConfigValue logDirForInventoryFile =
			new StringConfigValue("transitime.maintenance.logDirForInventoryFile",
					"Directory where to write the inventory file that lists "
					+ "the archiveIds and other info for the data written "
					+ "to the specified vault. Should be something like "
					+ "D:/Logs/mbta");
	
	private static StringConfigValue logFileBaseDir =
			new StringConfigValue("transitime.maintenance.logFileBaseDir",
					"Where to find the log files. This part of the directory "
					+ "name is not included in the file description for each "
					+ "file in the archived zip file. This way the file names "
					+ "in the zip file will be something like "
					+ "mbta/core/2014/12/20 instead of "
					+ "D:/Logs/mbta/core/2014/12/20.");
	
	private static StringConfigValue awsVaultName =
			new StringConfigValue("transitime.maintenance.awsVaultName",
					"For creating the vault name for archiving files to AWS "
					+ "Glacier. Should contain the agency name such that will "
					+ "be something like mbta-core");
	
	private static StringConfigValue awsVaultName2 =
			new StringConfigValue("transitime.maintenance.awsVaultName2",
					null,
					"For when making two separate archives. Can use two "
					+ "archives if some files should be archived sooner "
					+ "that others. Null means not using second archive.");

	private static StringConfigValue logFileSubDirectory =
			new StringConfigValue("transitime.maintenance.logFileSubDir",
					"The subdirectory beyond the base directory. Specifies "
					+ "where to find the log files to be archived. This part "
					+ "of the file names is included in the file descriptions "
					+ "in the zip file. Should be something like mbta/core");

	private static StringConfigValue logFileSubDirectory2 =
			new StringConfigValue("transitime.maintenance.logFileSubDir2",
					null,
					"For when making two separate archives. Can use two "
					+ "archives if some files should be archived sooner "
					+ "that others. Null means not using second archive.");
	
	private static IntegerConfigValue daysTillFilesArchived =
			new IntegerConfigValue("transitime.maintenance.daysTillFilesArchived",
					90,
					"How many days old files can be online before they are archived.");
	
	private static IntegerConfigValue daysTillFilesArchived2 =
			new IntegerConfigValue("transitime.maintenance.daysTillFilesArchived2",
					90,
					"For when making two separate archives. Can use two "
					+ "archives if some files should be archived sooner "
					+ "that others. Null means not using second archive.");
	
	/******************** Logging **************************************/
	
	private static final Logger logger = LoggerFactory
			.getLogger(ArchiveOldFilesModule.class);

	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 */
	public ArchiveOldFilesModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Sleeps until the time of day specified by secondsIntoDay member
	 */
	private void sleepTillAppropriateTime() {
		int secondsIntoDay = Time.parseTimeOfDay(timeToArchive.getValue());
		
		int nowSecsIntoDay = 
				Core.getInstance().getTime().getMsecsIntoDay(new Date())
				/ Time.MS_PER_SEC;
		int secsToSleep = secondsIntoDay - nowSecsIntoDay;
		if (nowSecsIntoDay > secondsIntoDay)
			secsToSleep += 1 * Time.SEC_PER_DAY;
		Time.sleep(secsToSleep * Time.MS_PER_SEC);		
	}
	
	/**
	 * Actually archives the logs
	 */
	private void archiveLogFiles() {
		logger.info("ArchiveOldFilesModule reached appropriate time "
				+ "of {} so archiving log files.",
				timeToArchive.getValue());
		
		// Create main archiver that can write the files to Amazon AWS Glacier
		AwsGlacierArchiver archiver = new AwsGlacierArchiver(
				awsRegion.getValue(), awsVaultName.getValue(),
				logDirForInventoryFile.getValue());
		
		// Archive the files in the specified directory 
		ArchiveOldFiles.archive(archiver, logFileBaseDir.getValue(),
				logFileSubDirectory.getValue(),
				daysTillFilesArchived.getValue());
		
		// If a secondary archival is configured then do it too. A second
		// archival can be useful because can separate out the maintenance
		// logs, such as updating config and updating travel times and can
		// easily see in archive when those processes were done. And can 
		// keep the smaller updating logs online longer since they take
		// much less disk space.
		if (awsVaultName2.getValue() != null) {
			// Create the secondary archiver that can write the files to 
			// Amazon AWS Glacier
			AwsGlacierArchiver archiver2 = new AwsGlacierArchiver(
					awsRegion.getValue(), awsVaultName2.getValue(),
					logDirForInventoryFile.getValue());
			
			// Archive the files in the specified directory 
			ArchiveOldFiles.archive(archiver2, logFileBaseDir.getValue(),
					logFileSubDirectory2.getValue(),
					daysTillFilesArchived2.getValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {		
		while (true) {
			try {
				// Sleep until the time of day specified by secondsIntoDay 
				// member
				sleepTillAppropriateTime();
				
				// Actually archive the logs
				archiveLogFiles();
			} catch (Throwable t) {
				// Note: catching Throwable instead of just Exception since
				// AWS calls can throw a Throwable and don't want to this
				// archival thread to exit when there is an error. Should 
				// continue to run and send an e-mail once a day as a 
				// reminder that there is a problem.
				logger.error(Markers.email(), 
						"Error when archiving old files for agencyId={}.", 
						AgencyConfig.getAgencyId(), t);
			}
		}

	}

}
