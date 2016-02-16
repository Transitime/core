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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For zipping and unzipping files.
 * 
 * @author SkiBu Smith
 *
 */
public class Zip {

	private static final int BUFFER_SIZE = 4096;
	

	private static final Logger logger = LoggerFactory
			.getLogger(Zip.class);


	/********************** Member Functions **************************/
	
	/**
	 * Unzips the specified file into the specified directory
	 * 
	 * @param zipFileName
	 *            Name of the file to be unzipped
	 * @param subDirectory
	 *            If not null then will put the resulting unzipped files into
	 *            the subdirectory. If null then resulting files end up in same
	 *            directory as the zip file.
	 * @return If successful, directory name where files extracted to. If
	 *         problem then null.
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
			
			logger.info("Unzipping file {} into directory {}", 
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
				byte buffer[] = new byte[BUFFER_SIZE];
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
	 * Zips up list of files into a zip file.
	 * 
	 * @param inputDirectory
	 *            Specifies directory that the inputFileNames are relative to.
	 * @param inputFileNames
	 *            List of files to be put into the zip file. File names should
	 *            be relative to the inputDirectory.
	 * @param fullZipFileName
	 *            The full name of the resulting zip file
	 * @return Name of the resulting zip file that was created, or null if there
	 *         was a problem
	 */
	public static String zip(String inputDirectory,
			List<String> inputFileNames, String fullZipFileName) {
		logger.info("Writing specified files in directory {} into zip file {}",
				inputDirectory, fullZipFileName);
		
		ZipOutputStream out = null;
		
		if (inputDirectory == null) 
			inputDirectory = "";
		
		try {
			// Create the output file
			out = new ZipOutputStream(new FileOutputStream(fullZipFileName));

			byte[] buffer = new byte[BUFFER_SIZE];

			// For every input file to be put into the zip file...
			for (String fileName : inputFileNames) {
				String fullInputFileName = inputDirectory + "/" + fileName;
				
				// Make sure not trying to add the zip file to the zip file
				// recursively! Skip such a file.
				if (fullInputFileName.equals(fullZipFileName)) {
					logger.info("Not writing file {} to zip file {} because "
							+ "that would cause infinite recursion!", 
							fileName, fullZipFileName);
					continue;
				}
				
				logger.info("Writing file {} to zip file {}", 
						fullInputFileName, fullZipFileName );
				
				// Open up the file
				FileInputStream in = new FileInputStream(fullInputFileName);
				BufferedInputStream bufIn = new BufferedInputStream(in);

				// Add entry for input file to the zip file
				out.putNextEntry(new ZipEntry(fileName));

				// Add the data from the input file
				int count;
				while ((count = bufIn.read(buffer, 0, BUFFER_SIZE)) > 0) {
					out.write(buffer, 0, count);
				}
				
				// Close the input file
				in.close();
			}

			// Done with all the files so close the zip file
			out.close();

			// Return that was successful
			logger.info("Successfully wrote zip file \"{}\"", fullZipFileName);			
			return fullZipFileName;
		} catch (IOException e) {
			logger.error("Exception occurred when zipping files. {}",
					e.getMessage(), e);

			// Return that was not successful
			return null;
		}
	}
	
	/**
	 * A recursive method for adding all files in a directory to a list of
	 * fileNames.
	 * 
	 * @param inputDirectory
	 *            Where to find the files. This part of the name is not included
	 *            as part of the file names in the zip file.
	 * @param fileOrDirName
	 *            Name, relative to the inputDirectory, of file or directory to
	 *            be added to the fileNames array.
	 * @param fileNames
	 *            The resulting list of file names.
	 * @param outputFileName
	 *            Name of resulting zip file. The file will be put into the
	 *            directory inputDirectory + "/" subDirectory.
	 */
	private static void addFileOrDir(String inputDirectory, String fileOrDirName,
			List<String> fileNames, String outputFileName) {
		String fullFileOrDirName = inputDirectory + "/" + fileOrDirName;
		File file = new File(fullFileOrDirName);
		if (file.isDirectory()) {
			// It is a directory so handle recursively
			String fileNamesForDir[] = file.list();
			// For each file in the directory...
			for (String subDirFileName : fileNamesForDir) {
				// Call this method recursively
				String fileNameRelativeToInputDir = 
						fileOrDirName + "/" + subDirFileName;
				addFileOrDir(inputDirectory, fileNameRelativeToInputDir,
						fileNames, outputFileName);
			}
		} else {
			// It is a file, not a directory, so add it to list
			logger.debug("Adding file {} to list of files to store in "
					+ "zip file {}", 
					fileOrDirName, outputFileName);
			fileNames.add(fileOrDirName);
		}
	}
	
	/**
	 * Recursively goes through directory and zips all the files into a zip
	 * file.
	 * 
	 * @param inputDirectory
	 *            The name of the directory where to start. This name is not
	 *            included in the file names in the zip file
	 * @param subDirectory
	 *            Appended to inputDirectory to specify what directory to be
	 *            zipped. This part of the file name is included as part of the
	 *            file names in the zip file.
	 * @param outputFileName
	 *            Name of resulting zip file. The file will be put into the
	 *            directory inputDirectory + "/" subDirectory.
	 * @return Name of the resulting zip file that was created, or null if there
	 *         was a problem
	 */
	public static String zip(String inputDirectory, String subDirectory,
			String outputFileName) {
		List<String> fileNames = new ArrayList<String>();

		addFileOrDir(inputDirectory, subDirectory, fileNames, outputFileName);

		String fullOutputFileName = 
				inputDirectory + "/" + subDirectory + "/" + outputFileName;
		return zip(inputDirectory, fileNames, fullOutputFileName);
	}
	

	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		zip("D:/Logs/mbta", "core/2014/08", "FOO.zip");
		unzip("/GTFS/sfmta/09-13-2013/latest.zip", "subdir");		
	}
}
