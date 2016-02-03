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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.configData.AgencyConfig;
import org.transitime.logging.Markers;
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
					"URL where to retrieve the GTFS file.");

	private static StringConfigValue dirName =
			new StringConfigValue(
					"transitime.gtfs.dirName", 
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
		boolean fileAlreadyExists = file.exists();
		if (fileAlreadyExists) {
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
				if (fileAlreadyExists)
					logger.info("Got remote file because version on web server "
							+ "is newer. Url={} dir={}", 
							httpGetFile.getFullFileName(), dirName.getValue());
				else
					logger.info("Got remote file because didn't have a local "
							+ "copy of it. Url={} dir={}",
							httpGetFile.getFullFileName(), dirName.getValue());
				
				// Email message
				String subject = "GTFS file was updated for " 
						+ AgencyConfig.getAgencyId();
				String message = "For " + AgencyConfig.getAgencyId() 
						+ " the GTFS file " + url.getValue() 
						+ " was updated so was downloaded to " 
						+ httpGetFile.getFullFileName();
				emailSender.send(MonitorBase.recipientsGlobal(), subject, 
						message);
				
				// Make copy of GTFS zip file in separate directory for archival
				archive(httpGetFile.getFullFileName());
			} else if (httpResponseCode == HttpStatus.SC_NOT_MODIFIED) {
				// If not modified then don't need to do anything
				logger.info("Remote GTFS file {} not updated (got "
						+ "HTTP NOT_MODIFIED status 304) since the local "
						+ "one  at {} has last modified date of {}",
						url.getValue(), httpGetFile.getFullFileName(), 
						new Date(file.lastModified()));
			} else {
				// Got unexpected response so log issue
				logger.error("Error retrieving remote GTFS file {} . Http "
						+ "response code={}", 
						url.getValue(), httpResponseCode);
			}
		} catch (IOException e) {
			logger.error("Error retrieving {} . {}", 
					url.getValue(), e.getMessage());
		}
	}

	/**
	 * Copies the specified file to a directory at the same directory level but
	 * with the directory name that is the last modified date of the file (e.g.
	 * 03-28-2015).
	 * 
	 * @param fullFileName
	 *            The full name of the file to be copied
	 */
	private static void archive(String fullFileName) {
		// Determine name of directory to archive file into. Use date of
		// lastModified time of file e.g. yyyy-MM-dd. Putting year first
		// and then month means that the directories will be listed 
		// chronologically when listed using unix ls command.
		File file = new File(fullFileName);
		Date lastModified = new Date(file.lastModified());
		DateFormat readableDateFormat =
				new SimpleDateFormat("yyyy-MM-dd");
		String dirName = readableDateFormat.format(lastModified);
		
		// Copy the file to the sibling directory with the name that is the
		// last modified date (e.g. 03-28-2015)
		Path source = Paths.get(fullFileName);
		Path target = source.getParent().getParent().resolve(dirName)
				.resolve(source.getFileName());
		
		logger.info("Archiving file {} to {}", 
				source.toString(), target.toString());
		
		try {
			// Create the directory where file is to go
			String fullDirName = target.getParent().toString();
			new File(fullDirName).mkdir();
			
			// Copy the file to the directory
			Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			logger.error("Was not able to archive GTFS file {} to {}", 
					source.toString(), target);
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
			} catch (Exception e) {
				logger.error(Markers.email(),
						"Exception in GtfsUpdatedModule for agencyId={}", 
						AgencyConfig.getAgencyId(), e);
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
