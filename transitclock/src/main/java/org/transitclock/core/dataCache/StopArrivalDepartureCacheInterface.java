package org.transitclock.core.dataCache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.transitclock.applications.Core;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Departure;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

public abstract class StopArrivalDepartureCacheInterface {

	private static final Logger logger = LoggerFactory.getLogger(StopArrivalDepartureCacheInterface.class);

	abstract public List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

	abstract public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

	abstract public void populateCacheFromDb(List<ArrivalDeparture> results);
	abstract protected void putAll(Map<StopArrivalDepartureCacheKey, StopEvents> map);
	abstract public StopArrivalDepartureCacheKey putArrivalDepartureInMemory(Map<StopArrivalDepartureCacheKey, StopEvents> map,
																																					 ArrivalDeparture arrivalDeparture);

	public void defaultPopulateCacheFromDb(List<ArrivalDeparture> results) {
		Map<StopArrivalDepartureCacheKey, StopEvents> map = new HashMap();
		try {
			for (ArrivalDeparture result : results) {
				putArrivalDepartureInMemory(map, result);
				//TODO might be better with its own populateCacheFromdb
				DwellTimeModelCacheFactory.getInstance().addSample(result);
			}
		} catch (Throwable t) {
			logger.error("StopArrivalDepartureCacheInterface failed with {}", t, t);
		}
		this.putAll(map);
	}

	/**
	 * Force departures to be later than arrivals.
	 * @param results
	 * @return
	 */
	public static List<ArrivalDeparture> smoothArrivalDepartures(List<ArrivalDeparture> results) {
		if (!StopArrivalDepartureCacheFactory.enableSmoothinng()) return results;

		List<ArrivalDeparture> filtered = new ArrayList<>(results.size());

		int adjustedCount = 0;
		int totalCount = 0;
		String lastTripId = null;
		Long lastTime = null;
		for (ArrivalDeparture result : results) {
			totalCount++;

			// as we are adding A/Ds, smooth any negative arrivals
			if (lastTime != null && lastTripId != null
							&& lastTripId.equals(result.getTripId())
							&& result.getTime() <= lastTime) {
				int adjustment = 0;  // departure can have same time as arrival
				if (result.isArrival()) {
					adjustment = 1; // arrival needs to be greater than last departure
				}
				filtered.add(createArrivalDeparture(result, lastTime + adjustment));
				if (adjustment > 0)
					adjustedCount++;
				lastTime = lastTime + adjustment;
			} else {
				filtered.add(result);
				lastTime = result.getTime();
			}
			if (lastTripId == null || !lastTripId.equals(result.getTripId())) {
				lastTripId = result.getTripId();
			}
		}

		logger.info("ADJUSTED {}% of {} entries", ((double)adjustedCount / totalCount)*100, totalCount);
		return filtered;
	}

	/**
	 * modify the time of the
	 * @param ad
	 * @param lastTime
	 * @return
	 */
	public static ArrivalDeparture createArrivalDeparture(ArrivalDeparture ad, Long lastTime) {
		if (ad.isArrival()) {
			return createArrival((Arrival)ad, lastTime);
		}
		return createDeparture((Departure) ad, lastTime);
	}

	public static Arrival createArrival(Arrival ad, Long lastTime) {
		return ad.withUpdatedTime(new Date(lastTime));
	}

	public static Departure createDeparture(Departure ad, long lastTime) {
		return ad.withUpdatedTime(new Date(lastTime));
	}

	public static List<ArrivalDeparture> createArrivalDeparturesCriteria(Criteria criteria, Date startDate, Date endDate) {
		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate))
						.addOrder(Order.asc("tripId"))
						.addOrder(Order.asc("stopPathIndex"))
						.addOrder(Order.desc("isArrival"))
						.list();
		if (!StopArrivalDepartureCacheFactory.enableSmoothinng()) return results;
		return smoothArrivalDepartures(results);
	}

	public static List<ArrivalDeparture> createArrivalDeparturesReverseCriteria(Criteria criteria, Date startDate, Date endDate) {
		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate))
						.addOrder(Order.asc("tripId"))
						.addOrder(Order.desc("stopPathIndex"))
						.addOrder(Order.asc("isArrival"))
						.list();
		return results;
	}

	/**
	 * Use the existing cache to ensure the departure is not before
	 * the arrival
	 * @param departure
	 * @return a departure with a time greater than the arrival time
	 */
	public Departure verifyDeparture(Departure departure) {
		if (!StopArrivalDepartureCacheFactory.enableVerification()) return departure;

		StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(departure.getStopId(), new Date(Time.getStartOfDay(departure.getAvlTime())));
		List<IpcArrivalDeparture> stopHistory = getStopHistory(key);
		ArrivalDeparture arrivalForDeparture = findArrivalForDeparture(stopHistory, departure);
		if (arrivalForDeparture == null) {
			logger.debug("no arrival found for departure {}", departure);
			return departure;
		}
		if (arrivalForDeparture.getTime() >= departure.getTime()) {
			logger.debug("adjusting departure time by {}", arrivalForDeparture.getTime() - departure.getTime() + 1);
			return createDeparture(departure, arrivalForDeparture.getTime() + 1);
		}
		return departure;
	}

	public Arrival verifyArrival(Arrival arrival) {
		if (!StopArrivalDepartureCacheFactory.enableVerification()) return arrival;

		Trip trip = Core.getInstance().getDbConfig().getTrip(arrival.getTripId());
		if (trip == null) {
			return arrival;
		}
		// go back one to stop path to find last departure
		StopPath stopPath = trip.getStopPath(arrival.getStopPathIndex() - 1);
		if (stopPath == null) {
			return arrival;
		}
		StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(stopPath.getStopId(), new Date(Time.getStartOfDay(arrival.getAvlTime())));

		List<IpcArrivalDeparture> stopHistory = getStopHistory(key);
		ArrivalDeparture lastDeparture = findLastDeparture(stopHistory, stopPath.getStopId(), arrival.getVehicleId(), arrival.getStopPathIndex());
		if (lastDeparture == null) {
			logger.debug("no previous departure for arrival {}", arrival);
			return arrival;
		}
		if (arrival.getTime() <= lastDeparture.getTime()) {
			logger.debug("adjusting arrival time by {}", arrival.getTime() - lastDeparture.getTime() + 1);
			return createArrival(arrival, lastDeparture.getTime() + 1);
		}
		return arrival;
	}

	private ArrivalDeparture findLastDeparture(List<IpcArrivalDeparture> stopHistory, String departureStopId, String arrivalVehicleId, int arrivalStopPathIndex) {
		if (stopHistory == null) return null;
		ArrivalDeparture lastArrivalDeparture = null;
		for (IpcArrivalDeparture ad : stopHistory) {
			if (ad.getVehicleId().equals(arrivalVehicleId)
							&& ad.getStopId().equals(departureStopId)
							&& ad.getStopPathIndex() < arrivalStopPathIndex) {
				if (lastArrivalDeparture == null || ad.getStopPathIndex() > lastArrivalDeparture.getStopPathIndex()) {
					lastArrivalDeparture = createArrivalDeparture(ad);
				}
			}
		}
		return lastArrivalDeparture;
	}

	private ArrivalDeparture findArrivalForDeparture(List<IpcArrivalDeparture> stopHistory, Departure departure) {
		if (stopHistory == null) return null;
		for (IpcArrivalDeparture ad : stopHistory) {
			if (ad.getVehicleId().equals(departure.getVehicleId())
							&& ad.getStopId().equals(departure.getStopId())
							&& ad.getTripId().equals(departure.getTripId())
							&& ad.getStopPathIndex() == departure.getStopPathIndex()) {
				return createArrivalDeparture(ad);
			}
		}
		return null;
	}

	private ArrivalDeparture createArrivalDeparture(IpcArrivalDeparture ad) {
		Block block = Core.getInstance().getDbConfig().getBlock(ad.getServiceId(), ad.getBlockId());
		Trip trip = block.getTrip(ad.getTripId());
		int tripIndex = block.getTripIndex(trip);

		if (ad.isArrival()) {
			Arrival a = new Arrival(ad.getVehicleId(),
							ad.getTime(),
							ad.getAvlTime(),
							block,
							tripIndex,
							ad.getStopPathIndex(),
							ad.getFreqStartTime(),
							null /* stopPathId not present */,
							ad.isScheduleAdherenceStop());
			return a;
		}
		Departure d = null;
		try {
			d = new Departure(ad.getVehicleId(),
					ad.getTime(),
					ad.getAvlTime(),
					block,
					tripIndex,
					ad.getStopPathIndex(),
					ad.getFreqStartTime(),
					ad.getDwellTime(),
					null, /* stopPathId not present */
					ad.isScheduleAdherenceStop());
		} catch(Exception e){
			e.printStackTrace();
		}
		return d;
	}


}