package org.transitclock.core;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcPrediction;

/**
 * @author Sean Óg Crudden 
 * Class to hold details of a single headway.
 *
 */
public class HeadwayDetails {

	private IpcPrediction vehicleAheadPrediction;
	private IpcPrediction vehicleBehindPrediction;

	public IpcPrediction getVehicleAheadPrediction() {
		return vehicleAheadPrediction;
	}

	public IpcPrediction getVehicleBehindPrediction() {
		return vehicleBehindPrediction;
	}

	public HeadwayDetails(IpcPrediction vehicleBehindPrediction, IpcPrediction vehicleAheadPrediction)
			throws Exception {
		super();
		this.vehicleBehindPrediction = vehicleBehindPrediction;
		this.vehicleAheadPrediction = vehicleAheadPrediction;
		if (!(vehicleBehindPrediction != null && vehicleAheadPrediction != null
				&& vehicleBehindPrediction.getStopId().equals(vehicleAheadPrediction.getStopId())
				&& (!vehicleBehindPrediction.getVehicleId().equals(vehicleAheadPrediction.getVehicleId())))) {
			throw new Exception("Need two predictions for same stop for different vehicles to calculate headway.");
		}
	}

	public long getHeadway() {

		return vehicleBehindPrediction.getPredictionTime() - vehicleAheadPrediction.getPredictionTime();

	}

	@Override
	public String toString() {

		return "HeadwayDetails [vehicleAheadPrediction=" + vehicleAheadPrediction + ", vehicleBehindPrediction="
				+ vehicleBehindPrediction + ", getHeadway()=" + getHeadway() + "]";

	}

}
