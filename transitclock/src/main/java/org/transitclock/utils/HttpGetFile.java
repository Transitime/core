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
package org.transitclock.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies a file from the specified URL and stores it locally. Useful
 * for things such as reading a GTFS zip file and storing it on local
 * machine for further processing.
 * 
 * @author SkiBu Smith
 *
 */
public class HttpGetFile {

	// UserAgenct info sent to web server when file is requested
	private static final String USER_AGENT = "Transitime";
	
	// In case can't figure out file name to use to store the
	// file from the URL
	private static final String DEFAULT_FILE_NAME = "DEFAULT_FILE";
	
	private final String urlStr;
	private final String dirNameForResult;
	private final String fullFileNameForResult;
	
	private final List<String> headerKeys = new ArrayList<String>();
	private final List<String> headerValues = new ArrayList<String>();
	
	protected static final Logger logger = 
			LoggerFactory.getLogger(HttpGetFile.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param urlStr
	 *            URL of file to get
	 * @param dirName
	 *            Directory where gotten file is to be written
	 */
	public HttpGetFile(String urlStr, String dirName) {
		this.urlStr = urlStr;
		
		// Make sure directory name ends with a "/"
		if (!dirName.endsWith("/"))
			dirName += "/";
		this.dirNameForResult = dirName;
		
		fullFileNameForResult = dirNameForResult + getFileNameFromUrl(urlStr);
	}
	
	/**
	 * Adds specified header key/value to the request. Useful if need to set
	 * If-Modified-Since or such.
	 * 
	 * @param key
	 * @param value
	 */
	public void addRequestHeader(String key, String value) {
		headerKeys.add(key);
		headerValues.add(value);
	}
	
	/**
	 * Simply a getter. Returns the full name where the file is stored on the
	 * local file system.
	 * 
	 * @return full file name
	 */
	public String getFullFileName() {
		return fullFileNameForResult;
	}
	
	/**
	 * Gets the file name from the URL so it can be used as part of the file
	 * name for storing the results.
	 * 
	 * @param urlStr
	 * @return the file name to use for storing the results
	 */
	private static String getFileNameFromUrl(String urlStr) {
		int lastSlashPos = urlStr.lastIndexOf('/');
		if (lastSlashPos == -1) {
			logger.error("Couldn't determine file name so using {}", 
					DEFAULT_FILE_NAME);
			return DEFAULT_FILE_NAME;
		}
		
		return urlStr.substring(lastSlashPos + 1);
	}
	
	/**
	 * Actually gets and stores the file. The User-Agency property is always set
	 * to USER_AGENT.
	 * 
	 * @return The http response code such as HttpStatus.SC_OK
	 * @throws IOException
	 */
	public int getFile() throws IOException {
		IntervalTimer timer = new IntervalTimer();
		
		logger.debug("Getting URL={}", urlStr);
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agency", USER_AGENT);
		
		// Set request properties
		for (int i=0; i<headerKeys.size(); ++i) {
			connection.setRequestProperty(headerKeys.get(i), headerValues.get(i));
		}
		
		// Get and log response code
		int responseCode = connection.getResponseCode();
		long expectedContentLength = connection.getContentLengthLong();
		long remoteFileLastModified = connection.getLastModified();
		logger.debug("Response code for getting file {} is {} and file size "
				+ "is {} bytes and remote file lastModified=\"{}\" or {} msec",
				urlStr, responseCode, expectedContentLength, 
				Time.httpDate(remoteFileLastModified), remoteFileLastModified);

		// Open file for where results are to be written
		File file = new File(fullFileNameForResult);
		
		// If file could not be read in or is not newer that lastModified time
		// of the existing file on the server then don't need to continue
		// reading it in.
		if (responseCode != HttpStatus.SC_OK) {
			logger.debug("Response code was {} so not reading in file", 
					responseCode);
			return responseCode;
		}
		
		// Sometimes a web server will return http status OK (200) even
		// when the remote file is older than the time set for If-Modified-Since
		// header. For this situation still don't want to read in the file
		// so simply return http status NO_MODIFIED (304).
		if (file.lastModified() > 0 
				&& remoteFileLastModified < file.lastModified()) {
			logger.warn("Response code was {} but the local file was modified "
					+ "after the remote file so it must be up to date. "
					+ "Therefore remote file not read in.",
					responseCode);
			return HttpStatus.SC_NOT_MODIFIED;
		}
		
		logger.debug("Actually reading data from URL {} . Local file "
				+ "lastModified={} or {} msec and remoteFileLastModified={} "
				+ "or {} msec.", 
				urlStr, 
				Time.httpDate(file.lastModified()), 
				file.lastModified(),
				Time.httpDate(remoteFileLastModified),
				remoteFileLastModified);
		
		// Make sure output directory exists
		file.getParentFile().mkdirs();

		// Open input stream for reading data
		InputStream in = connection.getInputStream();
		
		// Open the stream
		FileOutputStream fos = new FileOutputStream(file);
		
		IntervalTimer loopTimer = new IntervalTimer();
		long lengthSinceLoggingMsg = 0;
		
		// Copy contents to file
		byte[] buffer = new byte[4096];
		int length;
		int totalLength = 0;
		while ((length = in.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
			totalLength += length;
			lengthSinceLoggingMsg += length;
			
			// Every once in a while log progress. Don't want to
			// check timer every loop since that would be expensive.
			// So only check timer for every MB downloaded.
			if (lengthSinceLoggingMsg > 1024*1024) {
				lengthSinceLoggingMsg = 0;
				if (loopTimer.elapsedMsec() > 10 * Time.MS_PER_SEC) {
					loopTimer.resetTimer();
					logger.debug("Read in {} bytes or {}% of file {}",
							totalLength,
							StringUtils.oneDigitFormat(100.0 * totalLength
									/ expectedContentLength), 
							urlStr);
				}
			}
		}
		
		// Close things up
		in.close();
		fos.close();
		
		// Set the last modified time so that it is the same as on the 
		// web server. 
		file.setLastModified(connection.getLastModified());
		
		if (totalLength == expectedContentLength)
			logger.debug("Successfully copied {} to file {}. Length was {} "
					+ "bytes. Took {} msec.", urlStr, fullFileNameForResult,
					totalLength, timer.elapsedMsec());
		else
			logger.error("When copying {} to file {} the expected length was "
							+ "{} but only copied {} bytes", urlStr,
					fullFileNameForResult, expectedContentLength, totalLength);
		
		// Return the http response code such as 200 for OK or 304 for 
		// Not Modified
		return connection.getResponseCode();
	}
		
}
