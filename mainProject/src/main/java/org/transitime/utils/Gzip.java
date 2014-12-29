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
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For compressing and uncompressing gzip (.gz) files.
 *
 * @author SkiBu Smith
 *
 */
public class Gzip {

	private static final Logger logger = LoggerFactory
			.getLogger(Gzip.class);

	/********************** Member Functions **************************/

	/**
	 * Decompresses .gz file. Deletes the original .gz file.
	 * 
	 * @param gzipFileName
	 *            Name of the compressed file. Should end with .gz
	 */
    public static void decompressGzipFile(String gzipFileName) {
        try {
            FileInputStream fis = new FileInputStream(gzipFileName);
            GZIPInputStream gis = new GZIPInputStream(fis);
            String outputFileName = gzipFileName.substring(0, 
            		gzipFileName.lastIndexOf(".gz"));
            FileOutputStream fos = new FileOutputStream(outputFileName);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            //close resources
            fos.close();
            gis.close();
            
            // Delete the old .gz file
            File compressedFile = new File(gzipFileName);
            compressedFile.delete();
        } catch (IOException e) {
            logger.error("Exception when decompressing gzip file \"{}\"", 
            		gzipFileName, e);
        }         
    }
 
	/**
	 * Compresses specified file using gzip. Deletes the original file.
	 * Resulting file name will be fileName + ".gz".
	 * 
	 * @param fileName
	 * @return name of compressed file
	 */
    public static String compressGzipFile(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            String outputFileName = fileName + ".gz";
            FileOutputStream fos = new FileOutputStream(outputFileName);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
            
            // Delete the old file
            File uncompressedFile = new File(fileName);
            uncompressedFile.delete();
            
            return outputFileName;
        } catch (IOException e) {
            logger.error("Exception when compressing file \"{}\"", 
            		fileName, e);
            return null;
        }         
    }
 
    public static boolean isGzipFile(File file) {
    	String fileName = file.getName();
    	return fileName.endsWith(".gz");
    }
}
