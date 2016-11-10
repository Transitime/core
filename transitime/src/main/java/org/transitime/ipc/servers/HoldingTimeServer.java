/**
 * 
 */
package org.transitime.ipc.servers;

import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.dataCache.HoldingTimeCache;
import org.transitime.core.dataCache.HoldingTimeCacheKey;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.HoldingTime;
import org.transitime.ipc.data.IpcHoldingTime;
import org.transitime.ipc.interfaces.HoldingTimeInterface;
import org.transitime.ipc.interfaces.PredictionAnalysisInterface;
import org.transitime.ipc.rmi.AbstractServer;

/**
 * @author Sean Og Crudden Server to allow stored travel time predictions to be queried.
 * TODO May not be set to run by default as really only for analysis of predictions.
 */
public class HoldingTimeServer extends AbstractServer implements HoldingTimeInterface {
	// Should only be accessed as singleton class
	private static HoldingTimeServer singleton;

	private static final Logger logger = LoggerFactory.getLogger(HoldingTimeServer.class);

	protected HoldingTimeServer(String agencyId) {
		super(agencyId, HoldingTimeInterface.class.getSimpleName());

	}

	/**
	 * Starts up the HoldingTimeServer so that RMI calls can be used to query
	 * holding times stored in he cache. This will automatically cause the object to continue to run and
	 * serve requests.
	 * 
	 * @param agencyId
	 * @return the singleton PredictionAnalysisServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static HoldingTimeServer start(String agencyId) {
		if (singleton == null) {
			singleton = new HoldingTimeServer(agencyId);
		}
		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error(
					"Tried calling HoldingTimeServer.start() for "
							+ "agencyId={} but the singleton was created for agencyId={}",
					agencyId, singleton.getAgencyId());
			return null;
		}	
		return singleton;
	}

	@Override
	public IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId) throws RemoteException {
		
		if(tripId==null)
		{
			if(VehicleDataCache.getInstance().getVehicle(vehicleId)!=null)
			{
				tripId=VehicleDataCache.getInstance().getVehicle(vehicleId).getTripId();				
			}				
		}
		if(stopId!=null && vehicleId!=null && tripId!=null)
		{		
			HoldingTimeCacheKey key=new HoldingTimeCacheKey(stopId,vehicleId, tripId );
			HoldingTime result = HoldingTimeCache.getInstance().getHoldingTime(key);
			if(result!=null)
				return new IpcHoldingTime(result);										
		}
		return null;
	}

	@Override
	public IpcHoldingTime getHoldTime(String stopId, String vehicleId) throws RemoteException {
		
		String tripId=null;
		if(VehicleDataCache.getInstance().getVehicle(vehicleId)!=null)
		{
			tripId=VehicleDataCache.getInstance().getVehicle(vehicleId).getTripId();				
		}	
		if(stopId!=null && vehicleId!=null && tripId!=null)
		{		
			HoldingTimeCacheKey key=new HoldingTimeCacheKey(stopId,vehicleId, tripId );
			HoldingTime result = HoldingTimeCache.getInstance().getHoldingTime(key);
			if(result!=null)
				return new IpcHoldingTime(result);										
		}
		return null;
	}

	
	
	

}
