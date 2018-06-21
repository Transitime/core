package org.transitclock.api.data;

import java.util.Comparator;

import org.transitclock.ipc.data.IpcPrediction;
/**
 * A simple comparator for ordering IpcPrediction.
 * In this way, the main class IcpPrediction is not modified.
 * @author vperez
 *
 */
public class IpcPredictionComparator implements Comparator<IpcPrediction> {

	/**
	 * In this moment, only sequence is needed by GtfsRtTripFeed 
	 */
	@Override
	public int compare(IpcPrediction o1, IpcPrediction o2) {
		// TODO Auto-generated method stub
		return o1.getGtfsStopSeq()-o2.getGtfsStopSeq();
	}

}
