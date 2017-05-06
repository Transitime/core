package org.transitime.core.holdingmethod;

import java.util.List;

import org.transitime.core.VehicleState;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.HoldingTime;
import org.transitime.ipc.data.IpcPrediction;
/**
 * @author Sean Óg Crudden
 */
public interface HoldingTimeGenerator {
	public List<ControlStop> getControlPointStops();
	public HoldingTime generateHoldingTime(VehicleState vehicleState, ArrivalDeparture event);	
	public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction);
}
