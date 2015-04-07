/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.ipc.servers;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.BlocksInfo;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.VehicleConfig;
import org.transitime.ipc.data.IpcBlock;
import org.transitime.ipc.data.IpcCompleteVehicle;
import org.transitime.ipc.data.IpcGtfsRealtimeVehicle;
import org.transitime.ipc.data.IpcVehicle;
import org.transitime.ipc.data.IpcActiveBlock;
import org.transitime.ipc.data.IpcVehicleConfig;
import org.transitime.ipc.interfaces.VehiclesInterface;
import org.transitime.ipc.rmi.AbstractServer;

/**
 * Implements the VehiclesInterface interface on the server side such that a
 * VehiclessClient can make RMI calls in order to obtain vehicle information.
 * The vehicle information is provided using org.transitime.ipc.data.Vehicle
 * objects.
 *
 * @author SkiBu Smith
 *
 */
public class VehiclesServer extends AbstractServer 
	implements VehiclesInterface {

	private static LongConfigValue dwelltime =
			new LongConfigValue("transitime.find.dwelltime", 	new Long("30000"),				
					"This is the max you would expect a bus to be at a stop while moving normally.");
	
	private static LongConfigValue fuzzytime =
			new LongConfigValue("transitime.find.fuzzytime", 	new Long("180000"),				
					"This is the amount around a time that we look for an arrival or a departure.");

	// Should only be accessed as singleton class
	private static VehiclesServer singleton;
	
	// The VehicleDataCache associated with the singleton.
	private VehicleDataCache vehicleDataCache;

	private static final Logger logger = 
			LoggerFactory.getLogger(VehiclesServer.class);

	/********************** Member Functions **************************/

	/**
	 * Starts up the VehiclesServer so that RMI calls can query for predictions.
	 * This will automatically cause the object to continue to run and serve
	 * requests.
	 * 
	 * @param agencyId
	 * @param predictionManager
	 * @return the singleton PredictionsServer object
	 */
	public static VehiclesServer start(
			String agencyId, VehicleDataCache vehicleManager) {
		if (singleton == null) {
			singleton = new VehiclesServer(agencyId);
			singleton.vehicleDataCache = vehicleManager;
		}
		
		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error("Tried calling VehiclesServer.start() for " +
					"agencyId={} but the singleton was created for agencyId={}", 
					agencyId, singleton.getAgencyId());
			return null;
		}
		
		return singleton;
	}
	
	/*
	 * Constructor. Made private so that can only be instantiated by
	 * get(). Doesn't actually do anything since all the work is done in
	 * the superclass constructor.
	 * 
	 * @param projectId
	 *            for registering this object with the rmiregistry
	 */
	private VehiclesServer(String projectId) {
		super(projectId, VehiclesInterface.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#get()
	 */
	@Override
	public Collection<IpcVehicle> get() throws RemoteException {
		return getSerializableCollection(vehicleDataCache.getVehicles());
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getComplete()
	 */
	@Override
	public Collection<IpcCompleteVehicle> getComplete() throws RemoteException {
		return getCompleteSerializableCollection(vehicleDataCache.getVehicles());
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getGtfsRealtime()
	 */
	@Override
	public Collection<IpcGtfsRealtimeVehicle> getGtfsRealtime()
			throws RemoteException {
		return getGtfsRealtimeSerializableCollection(vehicleDataCache.getVehicles());
	}
	

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#get(java.lang.String)
	 */
	@Override
	public IpcVehicle get(String vehicleId) throws RemoteException {
		return vehicleDataCache.getVehicle(vehicleId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#get(java.lang.String)
	 */
	@Override
	public IpcCompleteVehicle getComplete(String vehicleId) throws RemoteException {
		return vehicleDataCache.getVehicle(vehicleId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#get(java.util.List)
	 */
	@Override
	public Collection<IpcVehicle> get(Collection<String> vehicleIds) 
			throws RemoteException {
		return getSerializableCollection(
				vehicleDataCache.getVehicles(vehicleIds));
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#get(java.util.List)
	 */
	@Override
	public Collection<IpcCompleteVehicle> getComplete(Collection<String> vehicleIds) 
			throws RemoteException {
		return getCompleteSerializableCollection(
				vehicleDataCache.getVehicles(vehicleIds));
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getForRoute(java.lang.String)
	 */
	@Override
	public Collection<IpcVehicle> getForRoute(String routeIdOrShortName) 
			throws RemoteException {
		return getSerializableCollection(
				vehicleDataCache.getVehiclesForRoute(routeIdOrShortName));
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getForRoute(java.lang.String)
	 */
	@Override
	public Collection<IpcCompleteVehicle> getCompleteForRoute(String routeIdOrShortName) 
			throws RemoteException {
		return getCompleteSerializableCollection(
				vehicleDataCache.getVehiclesForRoute(routeIdOrShortName));
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getForRoute(java.util.Collection)
	 */
	@Override
	public Collection<IpcVehicle> getForRoute(
			Collection<String> routeIdsOrShortNames) throws RemoteException {
	    return getSerializableCollection(
			vehicleDataCache.getVehiclesForRoute(routeIdsOrShortNames));
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getForRoute(java.util.Collection)
	 */
	@Override
	public Collection<IpcCompleteVehicle> getCompleteForRoute(
			Collection<String> routeIdsOrShortNames) throws RemoteException {
	    return getCompleteSerializableCollection(
			vehicleDataCache.getVehiclesForRoute(routeIdsOrShortNames));
	}

	/*
	 * This class returns Collections of Vehicles that are to be serialized.
	 * But sometimes these collections come from Map<K, T>.values(), which
	 * is a Collection that is not serializable. For such non-serializable
	 * collections this method returns a serializable version.
	 */
	private Collection<IpcVehicle> getSerializableCollection(
			Collection<IpcCompleteVehicle> vehicles) {
		// If vehicles is null then return empty array
		if (vehicles == null)
			return new ArrayList<IpcVehicle>();
		
		return new ArrayList<IpcVehicle>(vehicles);
	}

	/**
	 * This class returns Collection of Vehicles that are to be serialized. But
	 * sometimes these collections come from Map<K, T>.values(), which is a
	 * Collection that is not serializable. For such non-serializable
	 * collections this method returns a serializable version. If vehicles
	 * parameter is null then an empty array is returned.
	 * 
	 * @param vehicles
	 *            Original, possible not serializable, collection of vehicles.
	 *            Can be null.
	 * @return Serializable Collection if IpcGtfsRealtimeVehicle objects.
	 */
	private Collection<IpcGtfsRealtimeVehicle> getGtfsRealtimeSerializableCollection(
			Collection<IpcCompleteVehicle> vehicles) {
		// If vehicles is null then return empty array.
		if (vehicles == null)
			return new ArrayList<IpcGtfsRealtimeVehicle>();
		
		return new ArrayList<IpcGtfsRealtimeVehicle>(vehicles);
	}

	/**
	 * This class returns Collection of Vehicles that are to be serialized. But
	 * sometimes these collections come from Map<K, T>.values(), which is a
	 * Collection that is not serializable. For such non-serializable
	 * collections this method returns a serializable version. If vehicles
	 * parameter is null then an empty array is returned.
	 * 
	 * @param vehicles
	 *            Original, possible not serializable, collection of vehicles.
	 *            Can be null.
	 * @return Serializable Collection if IpcCompleteVehicle objects.
	 */
	private Collection<IpcCompleteVehicle> getCompleteSerializableCollection(
			Collection<IpcCompleteVehicle> vehicles) {
		// If vehicles is null then return empty array.
		if (vehicles == null)
			return new ArrayList<IpcCompleteVehicle>();
		
		if (vehicles instanceof Serializable) { 
			return vehicles;
		} else {
			return new ArrayList<IpcCompleteVehicle>(vehicles);
		}			
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getActiveBlocks()
	 */
	@Override
	public Collection<IpcActiveBlock> getActiveBlocks(
			Collection<String> routeIds, int allowableBeforeTimeSecs)
			throws RemoteException {
		// List of data to be returned
		Collection<IpcActiveBlock> results = 
				new ArrayList<IpcActiveBlock>();
		
		// Determine all the active blocks
		List<Block> blocks = BlocksInfo.getCurrentlyActiveBlocks(routeIds,
				allowableBeforeTimeSecs);
		
		// For each active block determine associated vehicle
		for (Block block : blocks) {
			IpcBlock ipcBlock = new IpcBlock(block);
			
			// If a block doesn't have a vehicle associated with it need
			// to determine which route a block is currently associated with
			// since can't get that info from the vehicle. This way the block
			// can be properly grouped with the associated route even when it
			// doesn't have a vehicle assigned.
			int activeTripIndex = block.activeTripIndex(new Date());
			
			// Determine vehicles associated with the block if there are any
			Collection<String> vehicleIdsForBlock = VehicleDataCache
					.getInstance().getVehiclesByBlockId(block.getId());
			Collection<IpcVehicle> ipcVehiclesForBlock = get(vehicleIdsForBlock);
			
			// Create and add the IpcBlockAndVehicle
			IpcActiveBlock ipcBlockAndVehicle = new IpcActiveBlock(
					ipcBlock, activeTripIndex, ipcVehiclesForBlock);
			results.add(ipcBlockAndVehicle);
		}
		
		// Return results
		return results;
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getVehicleConfigs()
	 */
	@Override
	public Collection<IpcVehicleConfig> getVehicleConfigs()
			throws RemoteException {
		Collection<IpcVehicleConfig> result = new ArrayList<IpcVehicleConfig>();
		for (VehicleConfig vehicleConfig : 
				VehicleDataCache.getInstance().getVehicleConfigs()) {
			result.add(new IpcVehicleConfig(vehicleConfig));
		}

		return result;
	}
	

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getVechicleLocation(java.lang.String, long)
	 */
	@Override
	public Location getVechicleLocation(String vehicleId, long time)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.VehiclesInterface#getLastStopOnRoute(java.lang.String, long)
	 */
	@Override
	public Stop getLastStopOnRoute(String vehicleId, long time)
			throws RemoteException {
		
		Long timeToFind=new Long(time);
		
		Long fuzzySize=new Long(fuzzytime.getValue());
		
		Long stopDwellTime=new Long(dwelltime.getValue());								
		
		List<ArrivalDeparture> results = ArrivalDeparture.getArrivalsDeparturesFromDb(new Date(timeToFind-fuzzySize), new Date(timeToFind+fuzzySize), vehicleId);
						
		ArrivalDeparture closest=null;
		
		long closestInMilliseconds=fuzzySize;
		/*
		 * TODO speed up this search as when loads of calls will be a bottleneck 
		 * */
		for(ArrivalDeparture result:results)
		{
			if(result.isDeparture())
			{
				if(result.getDate().getTime()<timeToFind+stopDwellTime)
				{
					if(closest==null)
					{
						closest=result;
					}
					else if(result.getDate().getTime()-timeToFind<closestInMilliseconds) {
						closestInMilliseconds=result.getDate().getTime()-timeToFind;
						closest=result;
					}
				}
			}				
		}
		logger.debug("Looking for departure closet to : "+new Date(timeToFind));
		logger.debug("Closest departure : "+ closest);
		if(closest!=null)
			return closest.getStop();
		else
			return null;
		
	}

}
