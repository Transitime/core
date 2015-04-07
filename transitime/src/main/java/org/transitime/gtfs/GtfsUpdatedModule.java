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

package org.transitime.gtfs;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.configData.AgencyConfig;
import org.transitime.modules.Module;
import org.transitime.monitoring.MonitorBase;
import org.transitime.utils.EmailSender;
import org.transitime.utils.HttpGetFile;
import org.transitime.utils.Time;

/**
 * Downloads GTFS file from web server if it has been updated and notifies
 * users. Useful for automatically determining when GTFS data has been updated
 * by an agency.
 * <p>
 * When a GTFS file is downloaded then this module also e-mails recipients
 * specified by the parameter transitime.monitoring.emailRecipients
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsUpdatedModule extends Module {

	// Configuration parameters
	private static StringConfigValue url =
			new StringConfigValue(
					"transitime.gtfs.url", 
					null, 
					"URL where to retrieve the GTFS file.");

	private static StringConfigValue dirName =
			new StringConfigValue(
					"transitime.gtfs.dirName", 
					null, 
					"Directory on agency server where to place the GTFS file.");

	private static LongConfigValue intervalMsec =
			new LongConfigValue(
					"transitime.gtfs.intervalMsec",
					// Low cost unless file actually downloaded so do pretty 
					// frequently so get updates as soon as possible
					4 * Time.MS_PER_HOUR, 
					"How long to wait before checking if GTFS file has changed "
					+ "on web");
	
	private static EmailSender emailSender = new EmailSender();
	
	private static final Logger logger = LoggerFactory
			.getLogger(GtfsUpdatedModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	public GtfsUpdatedModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Gets the GTFS file via http from the configured URL urlStr and stores it
	 * in the configured directory dirName.
	 * <p>
	 * If file on web server last modified time is not newer than the local file
	 * then the file will not actually be downloaded.
	 * <p>
	 * If file is downloaded then users and e-mailed.
	 */
	public static void get() {
		logger.info("Checking to see if GTFS should be downloaded "
				+ "because it was modified. {}", url.getValue());
		
		// Construct the getter
		HttpGetFile httpGetFile = 
				new HttpGetFile(url.getValue(), dirName.getValue());

		// If file hasn't been modified then don't want to download it
		// since it can be large. Therefore determine age of previously 
		// downloaded file and use If-Modified-Since to only actually
		// get the file if it is newer on the web server
		File file = new File(httpGetFile.getFullFileName());
		if (file.exists()) {
			// Get the last modified time of the local file. Add 10 minutes
			// since the web server might be load balanced and the files
			// on the different servers might have slightly different last 
			// modified times. To make sure that don't keep downloading 
			// from the different servers until get the file with most 
			// recent modified time add 10 minutes to the time to indicate
			// that as long as the local file is within 10 minutes of the
			// remote file that it is ok.
			long lastModified = file.lastModified() + 10*Time.MS_PER_MIN;

			httpGetFile.addRequestHeader("If-Modified-Since",
					Time.httpDate(lastModified));
			
			logger.debug("The file {} already exists so using "
					+ "If-Modified-Since header of \"{}\" or {} msec.", 
					httpGetFile.getFullFileName(), Time.httpDate(lastModified), 
					lastModified);
		}
		
		try {
			// Actually get the file from web server
			int httpResponseCode = httpGetFile.getFile();

			// If got a new file (instead of getting a NOT MODIFIED
			// response) then send message to those monitoring so that
			// the GTFS file can be processed.
			if (httpResponseCode == HttpStatus.SC_OK) {
				if (file.exists())
					logger.debug("Got remote file because version on web server "
							+ "is newer.");
				else
					logger.debug("Got remote file because don't have a local "
							+ "copy of it.");
				
				// Email message
				String subject = "GTFS file was updated for " 
						+ AgencyConfig.getAgencyId();
				String message = "For " + AgencyConfig.getAgencyId() 
						+ " the GTFS file " + url.getValue() 
						+ " was updated so was downloaded to " 
						+ httpGetFile.getFullFileName();
				emailSender.send(MonitorBase.recipientsGlobal(), subject, 
						message);
			} else if (httpResponseCode == HttpStatus.SC_NOT_MODIFIED) {
				// If not modified then don't need to do anything
			} else {
				// Got unexpected response so log issue
				logger.error("Error retrieving {} . Http response code={}", 
						url.getValue(), httpResponseCode);
			}
		} catch (IOException e) {
			logger.error("Error retrieving {} . {}", 
					url.getValue(), e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {	
		// Continue running module forever
		while(true) {
			// Wait until appropriate time
			Time.sleep(intervalMsec.getValue());
		
			// Get the GTFS file if it has been updated. Catch and
			// handle all exceptions to make sure module continues
			// to run even if there is an unexpected problem.
			try {
				get();
			} catch (RuntimeException e) {
				logger.error("Exception in GtfsUpdatedModule", e);
			}
		}
	}

	/**
	 * For debugging
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Start the module
		start("org.transitime.gtfs.GtfsUpdatedModule");
	}

}
