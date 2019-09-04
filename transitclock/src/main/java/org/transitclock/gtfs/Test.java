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
package org.transitclock.gtfs;

import java.util.List;

import org.transitclock.gtfs.gtfsStructs.GtfsAgency;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendar;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendarDate;
import org.transitclock.gtfs.gtfsStructs.GtfsFareAttribute;
import org.transitclock.gtfs.gtfsStructs.GtfsFareRule;
import org.transitclock.gtfs.gtfsStructs.GtfsFeedInfo;
import org.transitclock.gtfs.gtfsStructs.GtfsFrequency;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.gtfs.gtfsStructs.GtfsShape;
import org.transitclock.gtfs.gtfsStructs.GtfsStop;
import org.transitclock.gtfs.gtfsStructs.GtfsStopTime;
import org.transitclock.gtfs.gtfsStructs.GtfsTransfer;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;
import org.transitclock.gtfs.readers.GtfsAgencyReader;
import org.transitclock.gtfs.readers.GtfsCalendarDatesReader;
import org.transitclock.gtfs.readers.GtfsCalendarReader;
import org.transitclock.gtfs.readers.GtfsFareAttributesReader;
import org.transitclock.gtfs.readers.GtfsFareRulesReader;
import org.transitclock.gtfs.readers.GtfsFeedInfosReader;
import org.transitclock.gtfs.readers.GtfsFrequenciesReader;
import org.transitclock.gtfs.readers.GtfsRoutesReader;
import org.transitclock.gtfs.readers.GtfsRoutesSupplementReader;
import org.transitclock.gtfs.readers.GtfsShapesReader;
import org.transitclock.gtfs.readers.GtfsStopTimesReader;
import org.transitclock.gtfs.readers.GtfsStopsReader;
import org.transitclock.gtfs.readers.GtfsTransfersReader;
import org.transitclock.gtfs.readers.GtfsTripsReader;

/**
 *
 * @author SkiBu Smith
 *
 */
public class Test {

	/********************** Member Functions **************************/

	/**
	 * @param args First arg specifies gtfs files directory
	 */
	public static void main(String[] args) {
		// The directory where to find the gtfs files is
		// specified in first command line args.
		String gtfsDir = args[0];
		String gtfsSupplementDir = args[1];
		
		// Create a title formatter
		TitleFormatter titleFormatter = 
				new TitleFormatter("/GTFS/sfmta/titlesReplacements.txt", true);
		
		GtfsRoutesSupplementReader routesSupplementReader = new GtfsRoutesSupplementReader(gtfsSupplementDir);
		List<GtfsRoute> gtfsRoutesSupplement = routesSupplementReader.get();
		System.out.println("\nRoutes Supplement:");
		for (GtfsRoute r : gtfsRoutesSupplement) {
			System.out.println("route=" + r);
		}
		
		GtfsRoutesReader routesReader = new GtfsRoutesReader(gtfsDir);
		List<GtfsRoute> gtfsRoutes = routesReader.get();		
		System.out.println("\nRoutes:");
		for (GtfsRoute r : gtfsRoutes) {
			String routeName;
			if (gtfsDir.contains("sfmta")) {
				routeName = r.getRouteShortName();
				if (r.getRouteLongName() != null && !r.getRouteLongName().isEmpty())
					routeName += "-" + titleFormatter.processTitle(r.getRouteLongName());
			} else {
				if (!r.getRouteLongName().isEmpty())
					routeName = r.getRouteLongName();
				else
					routeName = r.getRouteShortName();
			}
			System.out.println("route=" + r + " ProcessedName=" + routeName);
		}

		GtfsAgencyReader agencyReader = new GtfsAgencyReader(gtfsDir);
		List<GtfsAgency> gtfsAgencies = agencyReader.get();
		System.out.println("\nAgencies:");
		for (GtfsAgency a : gtfsAgencies)
			System.out.println("agency=" + a);
	
		titleFormatter.logRegexesThatDidNotMakeDifference();
		
		GtfsStopsReader stopsReader = new GtfsStopsReader(gtfsDir);
		List<GtfsStop> gtfsStops = stopsReader.get();
		System.out.println("\nStops:");
		for (GtfsStop s : gtfsStops) {
			System.out.println("stop=" + s + " ProcessedName=" + titleFormatter.processTitle(s.getStopName()));
		}
		
		GtfsTripsReader tripsReader = new GtfsTripsReader(gtfsDir);
		List<GtfsTrip> gtfsTrips = tripsReader.get();
		System.out.println("\nTrips:");
		for (GtfsTrip t : gtfsTrips)
			System.out.println("trip=" + t);

		GtfsStopTimesReader stopTimesReader = new GtfsStopTimesReader(gtfsDir);
		List<GtfsStopTime> gtfsStopTimes = stopTimesReader.get();
		System.out.println("\nStop Times:");
		for (GtfsStopTime st : gtfsStopTimes)
			System.out.println("stopTime=" + st);

		GtfsCalendarReader calendarReader = new GtfsCalendarReader(gtfsDir);
		List<GtfsCalendar> gtfsCalendars = calendarReader.get();
		System.out.println("\nCalendar:");
		for (GtfsCalendar o : gtfsCalendars)
			System.out.println("calendar=" + o);
		
		GtfsCalendarDatesReader calendarDatesReader = new GtfsCalendarDatesReader(gtfsDir);
		List<GtfsCalendarDate> gtfsCalendarDates = calendarDatesReader.get();
		System.out.println("\nCalendar Dates:");
		for (GtfsCalendarDate o : gtfsCalendarDates)
			System.out.println("calendar date=" + o);

		GtfsFareAttributesReader fareAttributesReader = new GtfsFareAttributesReader(gtfsDir);
		List<GtfsFareAttribute> gtfsFareAttributes = fareAttributesReader.get();
		System.out.println("\nFare Attributes:");
		for (GtfsFareAttribute o : gtfsFareAttributes)
			System.out.println("fare attribute=" + o);

		GtfsFareRulesReader fareRulesReader = new GtfsFareRulesReader(gtfsDir);
		List<GtfsFareRule> gtfsFareRules = fareRulesReader.get();
		System.out.println("\nFare Rules:");
		for (GtfsFareRule o : gtfsFareRules)
			System.out.println("fare rule=" + o);
		
		GtfsShapesReader shapesReader = new GtfsShapesReader(gtfsDir);
		List<GtfsShape> gtfsShapes = shapesReader.get();
		System.out.println("\nShapes:");
		for (GtfsShape o : gtfsShapes)
			System.out.println("shape=" + o);

		GtfsFrequenciesReader frequenciesReader = new GtfsFrequenciesReader(gtfsDir);
		List<GtfsFrequency> gtfsFrequencies = frequenciesReader.get();
		System.out.println("\nFrequencies:");
		for (GtfsFrequency o : gtfsFrequencies)
			System.out.println("frequency=" + o);

		GtfsTransfersReader transfersReader = new GtfsTransfersReader(gtfsDir);
		List<GtfsTransfer> gtfsTransfers = transfersReader.get();
		System.out.println("\nTransfers:");
		for (GtfsTransfer o : gtfsTransfers)
			System.out.println("frequency=" + o);

		GtfsFeedInfosReader feedInfosReader = new GtfsFeedInfosReader(gtfsDir);
		List<GtfsFeedInfo> gtfsFeedInfos = feedInfosReader.get();
		System.out.println("\nFeed Infos:");
		for (GtfsFeedInfo o : gtfsFeedInfos)
			System.out.println("feed_info=" + o);

	}

}
