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
package org.transitime.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unzips a zip file
 * 
 * @author SkiBu Smith
 *
 */
public class Unzip {

	private static final Logger logger = LoggerFactory
			.getLogger(Unzip.class);


	/********************** Member Functions **************************/
	
	/**
	 * Unzips the specified file into the specified directory
	 * 
	 * @param zipFileName
	 * @param subDirectory If not null then will put the resulting unzipped files
	 * into the subdirectory. If null then resulting files end up in same
	 * directory as the zip file.
	 * @return If successful, directory name where files extracted to. If problem 
	 * then null.
	 */
	public static String unzip(String zipFileName, String subDirectory) {
		IntervalTimer timer = new IntervalTimer();
		
		try {
			// Determine directory where to put the unzipped files
			String parentDirName = new File(zipFileName).getParent();
			String directoryName = parentDirName;
			if (subDirectory != null) {
				directoryName += "/" + subDirectory;
			}
			
			logger.info("Unzipping file {} into director {}", 
					zipFileName, directoryName);
			
			// Extract the files
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String entryName = ze.getName();
				logger.info("Extracting file {}", entryName);
				File f = new File(directoryName + "/" + entryName);
				
				// Make sure necessary directory exists
				f.getParentFile().mkdirs();
				
				FileOutputStream fos = new FileOutputStream(f);
				int len;
				byte buffer[] = new byte[4096];
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);				
				}
				fos.close();
				
				ze = zis.getNextEntry();			
			}
			
			// Clean things up
			zis.closeEntry();
			zis.close();
			
			logger.info("Successfully extracted files from {} into directory {}. Took {} msec.", 
					zipFileName, directoryName, timer.elapsedMsec());
			return directoryName;
		} catch (Exception e) {
			// Log problem
			logger.error("Error occurred when processing file {}", zipFileName, e);
			return null;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		unzip("/GTFS/sf-muni/09-13-2013/latest.zip", "subdir");

	}
}
