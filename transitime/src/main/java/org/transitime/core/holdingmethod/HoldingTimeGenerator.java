package org.transitime.core.holdingmethod;

import java.util.List;

import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.HoldingTime;
import org.transitime.db.structs.Prediction;
import org.transitime.db.structs.Stop;
import org.transitime.ipc.data.IpcPrediction;
/**
 * @author Sean Óg Crudden
 */
public interface HoldingTimeGenerator {
	public List<ControlStop> getControlPointStops();
	public HoldingTime generateHoldingTime(ArrivalDeparture event);	
	public HoldingTime generateHoldingTime(IpcPrediction arrivalPrediction);
}
