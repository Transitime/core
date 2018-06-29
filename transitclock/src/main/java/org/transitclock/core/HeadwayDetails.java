package org.transitclock.core;

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
		
		if(vehicleBehindPrediction == null || vehicleAheadPrediction == null)
		{
			throw new Exception("Need two predictions for same stop for different vehicles to calculate headway.");
		}
		if(!vehicleBehindPrediction.getStopId().equals(vehicleAheadPrediction.getStopId()))
		{
			throw new Exception("Cannot calculate headway from predictions for two different stops.");
		}
		if(vehicleBehindPrediction.getVehicleId().equals(vehicleAheadPrediction.getVehicleId()))
		{
			throw new Exception("Cannot calculate headway from two prediction for the same vehicle.");
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
