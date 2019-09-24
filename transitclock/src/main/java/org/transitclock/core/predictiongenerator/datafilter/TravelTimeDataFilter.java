package org.transitclock.core.predictiongenerator.datafilter;

import org.transitclock.ipc.data.IpcArrivalDeparture;
/**
 * 
 * @author scrudden
 * Interface to implement to filter out unwanted travel time data.
 */
public interface TravelTimeDataFilter {
	public boolean filter(IpcArrivalDeparture departure,  IpcArrivalDeparture arrival);
}
