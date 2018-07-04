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
package org.transitclock.applications;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.gtfs.gtfsStructs.GtfsShape;
import org.transitclock.gtfs.gtfsStructs.GtfsStop;
import org.transitclock.gtfs.readers.GtfsShapesReader;
import org.transitclock.gtfs.readers.GtfsStopsReader;
import org.transitclock.gtfs.writers.GtfsShapesWriter;
import org.transitclock.gtfs.writers.GtfsStopsWriter;
import org.transitclock.utils.ChinaGpsOffset;

/**
 * Maps are offset for China such that when GTFS data is plotted it is not
 * accurately located with respect to the streets. This makes it really hard to
 * use ScheduleViewer to debug data. Therefore this application can be used to
 * offset the locations in the stops.txt and the shapes.txt files such that when
 * displayed on a map they will be correct.
 * 
 * A modified copy of all of the GTFS files is made into a new directory. The
 * new directory has a suffix specified by GTFS_OUTPUT_DIR_SUFFIX. So if
 * offsetting the directory /users/msmith/GTFS/agency the resulting files go
 * into /users/msmith/GTFS/agency_mapOffset.
 * 
 * @author SkiBu Smith
 * 
 */
public class OffsetGtfsLocsForChina {

	private static final Logger logger = 
			LoggerFactory.getLogger(OffsetGtfsLocsForChina.class);

	private static final String GTFS_OUTPUT_DIR_SUFFIX = "_mapOffset";

	/********************** Member Functions **************************/

	/**
	 * Copies file from the GTFS directory to the new directory
	 * with GTFS_OUTPUT_DIR_SUFFIX appended to its name.
	 * 
	 * @param fileName
	 * @param gtfsDirectory
	 */
	private static void copyToOffsetDir(String fileName, String gtfsDirectory) {
		String fromFileName = gtfsDirectory + "/" + fileName;
		String toFileName = gtfsDirectory + GTFS_OUTPUT_DIR_SUFFIX + "/"
				+ fileName;
		try {
			Files.copy((new File(fromFileName)).toPath(),
					(new File(toFileName)).toPath());
		} catch (FileAlreadyExistsException e) {
			// Ignore this one because it is OK. It simply means
			// that the file was already copied to the offset
			// directory.
		} catch (NoSuchFileException e) {
			// Ignore this one because it is OK. It simply means 
			// that the file doesn't exist and is likely just an
			// optional one anyways.
		} catch (IOException e) {
			logger.warn(e.getClass().getSimpleName() + 
					" occurred when copying file " + fileName + ". " +  
					": " + e.getMessage());
		}
	}
	
	/**
	 * Copies a GTFS file in the specified directory to a name with "_old" 
	 * appended.
	 * 
	 * @param fileName
	 * @param gtfsDirectory
	 */
	private static void archiveGtfsFile(String fileName, String gtfsDirectory) {
		String oldFileName = gtfsDirectory + "/" + fileName;
		String newFileName = oldFileName + "_old";
		try {
			Files.copy((new File(oldFileName)).toPath(),
					(new File(newFileName)).toPath(), 
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.warn(e.getClass().getSimpleName() + 
					" occurred when copying file " + oldFileName + " to " + 
					newFileName +" . " + ": " + e.getMessage());
		}
	}
	
	/**
	 * Creates new offset GTFS directory, transforms the stops.txt and
	 * shapes.txt files from Geodetic to Mars coordinates and places
	 * results into that directory, and then copies the remaining
	 * GTFS files into that directory.
	 * 
	 * @param gtfsDirectory
	 */
	private static void transform(String gtfsDirectory) {
		// For writing new shapes file
		GtfsShapesWriter shapesWriter = new GtfsShapesWriter(gtfsDirectory
				+ GTFS_OUTPUT_DIR_SUFFIX + "/" + "shapes.txt");

		// Create list of offset GtfsShape objects
		GtfsShapesReader shapesReader = new GtfsShapesReader(gtfsDirectory);
		List<GtfsShape> gtfsShapes = shapesReader.get();
		for (GtfsShape origGtfsShape : gtfsShapes) {
			ChinaGpsOffset.LatLon offsetLoc = 
					ChinaGpsOffset.transform(origGtfsShape.getShapePtLat(), 
							origGtfsShape.getShapePtLon());
			GtfsShape offsetGtfsShape = new GtfsShape(origGtfsShape, 
					offsetLoc.getLat(), offsetLoc.getLon());
			shapesWriter.write(offsetGtfsShape);
		}
		shapesWriter.close();
		
		// For writing new stops file
		GtfsStopsWriter stopsWriter = new GtfsStopsWriter(gtfsDirectory
				+ GTFS_OUTPUT_DIR_SUFFIX + "/" + "stops.txt");
		
		// Create list of offset GtfsStop objects
		GtfsStopsReader stopsReader = new GtfsStopsReader(gtfsDirectory);
		List<GtfsStop> gtfsStops = stopsReader.get();
		for (GtfsStop origGtfsStop : gtfsStops) {
			ChinaGpsOffset.LatLon offsetLoc = 
					ChinaGpsOffset.transform(origGtfsStop.getStopLat(), 
							origGtfsStop.getStopLon());
			GtfsStop offsetGtfsStop = new GtfsStop(origGtfsStop, 
					offsetLoc.getLat(), offsetLoc.getLon());
			stopsWriter.write(offsetGtfsStop);
		}
		stopsWriter.close();

		// Copy all the other files
		copyToOffsetDir("agency.txt", gtfsDirectory);
		copyToOffsetDir("calendar.txt", gtfsDirectory);
		copyToOffsetDir("calendarDates.txt", gtfsDirectory);
		copyToOffsetDir("fare_attributes.txt", gtfsDirectory);
		copyToOffsetDir("fare_rules.txt", gtfsDirectory);
		copyToOffsetDir("frequencies.txt", gtfsDirectory);
		copyToOffsetDir("routes.txt", gtfsDirectory);
		copyToOffsetDir("stop_times.txt", gtfsDirectory);
		copyToOffsetDir("trips.txt", gtfsDirectory);		
	}
	
	/**
	 * Archives the shapes.txt and stops.txt files in the GTFS directory and
	 * then transforms from Geodetic ==> Mars the shapes.txt and stops.txt files
	 * from the GTFS offset directory and puts the results into the regular GTFS
	 * directory.
	 * 
	 * @param gtfsDirectory
	 */
	private static void transformBack(String gtfsDirectory) {
		// Archive the shapes.txt and stops.txt files that are
		// in the original GTFS directory.
		archiveGtfsFile("shapes.txt", gtfsDirectory);
		archiveGtfsFile("stops.txt", gtfsDirectory);
		
		// For writing new shapes file
		GtfsShapesWriter shapesWriter = new GtfsShapesWriter(gtfsDirectory
				+ "/" + "shapes.txt");

		// Create list of offset GtfsShape objects
		GtfsShapesReader shapesReader = new GtfsShapesReader(gtfsDirectory + 
				GTFS_OUTPUT_DIR_SUFFIX);
		List<GtfsShape> gtfsShapes = shapesReader.get();
		for (GtfsShape origGtfsShape : gtfsShapes) {
			ChinaGpsOffset.LatLon offsetLoc = 
					ChinaGpsOffset.transformBack(origGtfsShape.getShapePtLat(), 
							origGtfsShape.getShapePtLon());
			GtfsShape offsetGtfsShape = new GtfsShape(origGtfsShape, 
					offsetLoc.getLat(), offsetLoc.getLon());
			shapesWriter.write(offsetGtfsShape);
		}
		shapesWriter.close();
		
		// For writing new stops file
		GtfsStopsWriter stopsWriter = new GtfsStopsWriter(gtfsDirectory
				+ "/" + "stops.txt");
		
		// Create list of offset GtfsStop objects
		GtfsStopsReader stopsReader = new GtfsStopsReader(gtfsDirectory +
				GTFS_OUTPUT_DIR_SUFFIX);
		List<GtfsStop> gtfsStops = stopsReader.get();
		for (GtfsStop origGtfsStop : gtfsStops) {
			ChinaGpsOffset.LatLon offsetLoc = 
					ChinaGpsOffset.transformBack(origGtfsStop.getStopLat(), 
							origGtfsStop.getStopLon());
			GtfsStop offsetGtfsStop = new GtfsStop(origGtfsStop, 
					offsetLoc.getLat(), offsetLoc.getLon());
			stopsWriter.write(offsetGtfsStop);
		}
		stopsWriter.close();
	}
	
	/**
	 * Converts for specified GTFS directory the shapes.txt and stops.txt
	 * locations from Geodetic ==> Mars coordinates used for China maps. Can
	 * also be used to transform back from Mars ==> Geodetic coordinates.
	 * 
	 * A modified copy of all of the GTFS files is made into a new directory.
	 * The new directory has a suffix specified by GTFS_OUTPUT_DIR_SUFFIX. So if
	 * offsetting the directory /users/msmith/GTFS/agency the resulting files go
	 * into /users/msmith/GTFS/agency_mapOffset.
	 * 
	 * @param args
	 *            If first arg is set to "-back" then does a reverse
	 *            transformation from Mars ==> Geodetic coordinates. The next
	 *            argument is the directory of the GTFS data
	 */
	public static void main(String[] args) {
		if (args[0].equals("-back")) {
			// The GTFS directory is the second arg
			String gtfsDirectory = args[1];
			
			// Do the Mars ==? Geodetic transformation
			transformBack(gtfsDirectory);
		} else {
			// The GTFS directory is the first arg
			String gtfsDirectory = args[0];
			
			// Do the Geodetic ==> Mars transformation
			transform(gtfsDirectory);
		}
		
	}
}
