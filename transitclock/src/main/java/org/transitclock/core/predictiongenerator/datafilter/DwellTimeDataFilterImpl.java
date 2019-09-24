package org.transitclock.core.predictiongenerator.datafilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;

import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;
/**
 * 
 * @author scrudden
 * Filter by 
 * schedule adherence min and max
 * dwell tiime min and max
 *
 */
public class DwellTimeDataFilterImpl implements DwellTimeDataFilter {
	private static LongConfigValue maxDwellTimeAllowedInModel = new LongConfigValue(
			"transitclock.prediction.dwell.maxDwellTimeAllowedInModel", (long) (2 * Time.MS_PER_MIN),
			"Max dwell time to be considered in  algotithm.");
	private static LongConfigValue minDwellTimeAllowedInModel = new LongConfigValue(
			"transitclock.prediction.dwell.minDwellTimeAllowedInModel", (long) 0,
			"Min dwell time to be considered in  algotithm.");
	private static IntegerConfigValue minSceheduleAdherence = new IntegerConfigValue(
			"transitclock.prediction.dwell.minSceheduleAdherence", (int) (10 * Time.SEC_PER_MIN),
			"If schedule adherence of vehicle is outside this then not considerd in dwell  algorithm.");
	private static IntegerConfigValue maxSceheduleAdherence = new IntegerConfigValue(
			"transitclock.prediction.dwell.maxSceheduleAdherence", (int) (10 * Time.SEC_PER_MIN),
			"If schedule adherence of vehicle is outside this then not considerd in dwell algorithm.");
	private static final Logger logger = LoggerFactory.getLogger(DwellTimeDataFilterImpl.class);

	@Override
	public boolean filter(IpcArrivalDeparture arrival, IpcArrivalDeparture departure) {
		if (arrival != null && departure != null) {
			long dwelltime = departure.getTime().getTime() - arrival.getTime().getTime();

			if (departure.getScheduledAdherence() != null && departure.getScheduledAdherence()
					.isWithinBounds(minSceheduleAdherence.getValue(), maxSceheduleAdherence.getValue())) {
				// TODO Arrival schedule adherence appears not to be set much. So
				// only stop if set and outside range.
				if (arrival.getScheduledAdherence() == null || arrival.getScheduledAdherence()
						.isWithinBounds(minSceheduleAdherence.getValue(), maxSceheduleAdherence.getValue())) {
					if (dwelltime < maxDwellTimeAllowedInModel.getValue()
							&& dwelltime > minDwellTimeAllowedInModel.getValue()) {
						return false;
					} else {
						logger.warn("Dwell time {} outside allowable range for {}.", dwelltime, departure);
					}
				} else {
					logger.warn("Schedule adherence outside allowable range. " + arrival);
				}
			} else {
				logger.warn("Schedule adherence outside allowable range. " + departure);
			}
		} else {
			logger.warn("Arrival and/or departure not set.");
		}
		return true;
	}

}
