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
package org.transitclock.db.hibernate;

import org.hibernate.cfg.Configuration;
import org.transitclock.db.structs.*;
import org.transitclock.db.webstructs.ApiKey;
import org.transitclock.db.webstructs.WebAgency;

/**
 * 
 * Yes, this is a nuisance. If Hibernate Session class is used instead of the
 * JPA EntityManager class then all annotated classes have to be explicitly
 * added to the Configuration object. Don't want to get into using the JPA
 * EntityManager class for now (just trying to learn Hibernate based on books
 * such as Harnessing Hibernate). So need to do this.
 * 
 * These annotated classes could also be listed in the hibernate.cfg.xml config
 * file but that would be difficult to maintain. Each application might already
 * have a modified hibernate config file. Don't want to have to have each app
 * modify their config file when new annotated classes are added. Therefore it
 * is best to configure these classes programmatically.
 * 
 * @author SkiBu Smith
 * 
 */
public class AnnotatedClassesList {

	// List here all the annotated classes that can be stored in the db
	private static Class<?>[] classList = new Class[] {
		ActiveRevisions.class,
		Agency.class,
		Arrival.class,
		AvlReport.class,
		Block.class,
		Calendar.class,
		CalendarDate.class,
		ConfigRevision.class,
		DbTest.class,
		Prediction.class,
		Departure.class,
		FareAttribute.class,
		FareRule.class,
		FeedInfo.class,
		Frequency.class,
		Location.class,
		Match.class,
		MeasuredArrivalTime.class,
		MonitoringEvent.class,
		PredictionAccuracy.class,
		Route.class,
		RouteDirection.class,
		RunTimesForRoutes.class,
		RunTimesForStops.class,
		Stop.class,
		StopPath.class,
		Transfer.class,
		TravelTimesForStopPath.class,
		PredictionForStopPath.class,		
		TravelTimesForTrip.class,
		Trip.class,
		TripPattern.class,
		VehicleEvent.class,
		PredictionEvent.class,
		VehicleConfig.class,
		VehicleState.class,
		HoldingTime.class,
		Headway.class,
		// For website
		ApiKey.class,
		WebAgency.class,
		// for traffic data
		TrafficSensor.class,
		TrafficPath.class,
		TrafficSensorData.class
	};
	
	/********************** Member Functions **************************/
	
	/**
	 * Adds the classes listed within this method to the Hibernate
	 * configuration. Needed so that the class can be used with
	 * Hibernate.
	 * 
	 * @param config
	 */
	public static void addAnnotatedClasses(Configuration config) {
		// Add all the annotated classes to the config
		for (@SuppressWarnings("rawtypes") Class c : classList) {
			HibernateUtils.logger.debug("Adding to Hibernate config the annotated class {}", 
					c.getName());
			config.addAnnotatedClass(c);
		}
	}
}
