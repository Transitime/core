package org.transitclock.core.predictiongenerator.datafilter;

import org.transitclock.ipc.data.IpcArrivalDeparture;
/**
 * 
 * @author scrudden
 * Interface to implement to filter out unwanted dwell time data.
 */

public interface DwellTimeDataFilter {
	
	public boolean filter(IpcArrivalDeparture arrival,  IpcArrivalDeparture departure);
	
}
