/**
 * 
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

import org.transitime.utils.HttpGetFile;
import org.transitime.utils.Time;


/**
 * For grabbing a GTFS zip file over the web using http.
 * The getFile() method copies the file to the directory
 * specified by getDirectoryForStoringFile().
 * 
 * @author SkiBu Smith
 *
 */
public class HttpGetGtfsFile extends HttpGetFile {


	/********************** Member Functions **************************/

	public HttpGetGtfsFile(String projectId, String urlStr) {
		super(urlStr, getDirectoryForStoringFile(projectId));
	}
	
	/**
	 * Returns directory name of where to store the file.
	 * The directory will be /GTFS/projectId/MM-dd-yyyy/
	 * 
	 * @return the directory name for storing the results
	 */
	private static String getDirectoryForStoringFile(String projectId) {		
		return "/GTFS/" + projectId + "/" + 
				Time.dateStr(System.currentTimeMillis()) + "/";
	}
	
	/**
	 * Main entry point to class. Reads in specified file from URL and stores 
	 * it using same file name into directory specified by getDirectoryRorFile().
	 * The directory name will be something like "/GTFS/projectId/MM-dd-yyyy/file.zip".
	 * @param projectId For determining directory where to store file
	 * @param urlStr URL of where to get file
	 * @return The file name of the newly created file, null if there was a problem
	 */
	public static String getFile(String projectId, String urlStr) {
		HttpGetFile getter = new HttpGetGtfsFile(projectId, urlStr);
		try {
			getter.getFile();
			return getter.getFullFileName();
		} catch (Exception e) {
			HttpGetFile.logger.error("Exception occurred when reading in file: ", 
					e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Copies over Gtfs file.
	 * @param args First arg=projectId, second arg=url
	 */
	public static void main(String[] args) {
		String projectId = args[0];
		String url = args[1];
		HttpGetGtfsFile.getFile(projectId, url);
	}

}
