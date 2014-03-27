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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.Agency;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Calendar;
import org.transitime.db.structs.CalendarDate;
import org.transitime.db.structs.FareAttribute;
import org.transitime.db.structs.FareRule;
import org.transitime.db.structs.Frequency;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.Transfer;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.utils.IntervalTimer;

/**
 * Writes the GTFS data contained in a GtfsData object to the database.
 * 
 * @author SkiBu Smith
 *
 */
public class DbWriter {

	private final GtfsData gtfsData;
	// The session is a member so don't have to pass it in to all the methods
	private Session session;
	private int counter = 0;
	private static final Logger logger = LoggerFactory
			.getLogger(DbWriter.class);

	/********************** Member Functions **************************/

	public DbWriter(GtfsData gtfsData) {
		this.gtfsData = gtfsData;
	}
	
	/**
	 * Actually writes data to database (once transaction closed).
	 * Uses Hibernate batching so don't use as much memory.
	 * @param object
	 */
	private void writeObject(Object object) {
		session.saveOrUpdate(object);

		// Since can writing large amount of data should use Hibernate 
		// batching to make sure don't run out memory.
		counter++;
		if (counter % HibernateUtils.BATCH_SIZE == 0) {
			session.flush();
			session.clear();
		}
	}
	
	/**
	 * Goes through the collections in GtfsData and writes the objects
	 * to the database.
	 */
	private void actuallyWriteData() {
		// Get rid of old data. Getting rid of trips, trip patterns, and blocks
		// is a bit complicated. Need to delete them in proper order because
		// of the foreign keys. Because appear to need to use plain SQL
		// to do so successfully (without reading in objects and then
		// deleting them, which takes too much time and memory). Therefore
		// deleting of this data is done here before writing the data.
		logger.info("Deleting old blocks and associated trips from database...");
		Block.deleteFromSandboxRev(session);

		logger.info("Deleting old trips from database...");
		Trip.deleteFromSandboxRev(session);

		logger.info("Deleting old trip patterns from database...");
		TripPattern.deleteFromSandboxRev(session);
		
		// Now write the data to the database.
		// First write the Blocks. This will also write the Trips, TripPatterns,
		// Paths, and TravelTimes since those all have been configured to be
		// cascade=CascadeType.ALL .
		logger.info("Saving {} blocks (plus associated trips) to database...", 
				gtfsData.getBlocks().size());
		int c = 0;
		for (Block block : gtfsData.getBlocks()) {
			logger.debug("Saving block #{} with blockId={} serviceId={} blockId={}",
					++c, block.getId(), block.getServiceId(), block.getId());
			writeObject(block);
		}
		
		logger.info("Saving routes to database...");
		Route.deleteFromSandboxRev(session);
		for (Route route : gtfsData.getRoutes()) {
			writeObject(route);
		}
		
		logger.info("Saving stops to database...");
		Stop.deleteFromSandboxRev(session);
		for (Stop stop : gtfsData.getStops()) {
			writeObject(stop);
		}
		
		logger.info("Saving agencies to database...");
		Agency.deleteFromSandboxRev(session);
		for (Agency agency : gtfsData.getAgencies()) {
			writeObject(agency);
		}

		logger.info("Saving calendars to database...");
		Calendar.deleteFromSandboxRev(session);
		for (Calendar calendar : gtfsData.getCalendars()) {
			writeObject(calendar);
		}
		
		logger.info("Saving calendar dates to database...");
		CalendarDate.deleteFromSandboxRev(session);
		for (CalendarDate calendarDate : gtfsData.getCalendarDates()) {
			writeObject(calendarDate);
		}
		
		logger.info("Saving fare rules to database...");
		FareRule.deleteFromSandboxRev(session);
		for (FareRule fareRule : gtfsData.getFareRules()) {
			writeObject(fareRule);
		}
		
		logger.info("Saving fare attributes to database...");
		FareAttribute.deleteFromSandboxRev(session);
		for (FareAttribute fareAttribute : gtfsData.getFareAttributes()) {
			writeObject(fareAttribute);
		}
		
		logger.info("Saving frequencies to database...");
		Frequency.deleteFromSandboxRev(session);
		for (Frequency frequency : gtfsData.getFrequencies()) {
			writeObject(frequency);
		}
		
		logger.info("Saving transfers to database...");
		Transfer.deleteFromSandboxRev(session);
		for (Transfer transfer : gtfsData.getTransfers()) {
			writeObject(transfer);
		}
	}
	
	/**
	 * Writes the data for the collections that are part of the GtfsData
	 * object passed in to the constructor.
	 */
	public void write(SessionFactory sessionFactory) {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Writing GTFS data to database...");

		session = sessionFactory.openSession();		
		Transaction tx = session.beginTransaction();
		
		// Do the low-level processing
		try {
			actuallyWriteData();
			
			// Done writing data so commit it
			tx.commit();
		} catch (HibernateException e) {
			logger.error("Error writing configuration data to db.", e);
		} finally {
			// Always make sure session gets closed
			session.close();
		}

		// Let user know what is going on
		logger.info("Finished writing GTFS data to database . Took {} msec.",timer.elapsedMsec());		
	}
}
