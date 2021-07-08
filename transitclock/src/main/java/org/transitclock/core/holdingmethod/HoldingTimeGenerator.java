package org.transitclock.core.holdingmethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.HoldingTime;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcPrediction;
/**
 * @author Sean Óg Crudden
 */
public interface HoldingTimeGenerator {
	public List<ControlStop> getControlPointStops();
	public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcArrivalDeparture event);	
	public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction);
	
	public void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture);
}
