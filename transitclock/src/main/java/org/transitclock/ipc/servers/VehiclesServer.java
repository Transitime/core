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

package org.transitclock.ipc.servers;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.BlocksInfo;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripCache;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.VehicleConfig;
import org.transitclock.ipc.data.IpcActiveBlock;
import org.transitclock.ipc.data.IpcBlock;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.ipc.data.IpcVehicleComplete;
import org.transitclock.ipc.data.IpcVehicleConfig;
import org.transitclock.ipc.data.IpcVehicleGtfsRealtime;
import org.transitclock.ipc.interfaces.VehiclesInterface;
import org.transitclock.ipc.rmi.AbstractServer;

/**
 * Implements the VehiclesInterface interface on the server side such that a
 * VehiclessClient can make RMI calls in order to obtain vehicle information.
 * The vehicle information is provided using org.transitclock.ipc.data.Vehicle
 * objects.
 *
 * @author SkiBu Smith
 *
 */
public class VehiclesServer extends AbstractServer 
	implements VehiclesInterface {

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
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#get()
	 */
	@Override
	public Collection<IpcVehicle> get() throws RemoteException {
		return getSerializableCollection(vehicleDataCache.getVehicles());
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getComplete()
	 */
	@Override
	public Collection<IpcVehicleComplete> getComplete() throws RemoteException {
		return getCompleteSerializableCollection(vehicleDataCache.getVehicles());
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getGtfsRealtime()
	 */
	@Override
	public Collection<IpcVehicleGtfsRealtime> getGtfsRealtime()
			throws RemoteException {
		return getGtfsRealtimeSerializableCollection(vehicleDataCache.getVehicles());
	}
	

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.lang.String)
	 */
	@Override
	public IpcVehicle get(String vehicleId) throws RemoteException {
		return vehicleDataCache.getVehicle(vehicleId);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.lang.String)
	 */
	@Override
	public IpcVehicleComplete getComplete(String vehicleId) throws RemoteException {
		return vehicleDataCache.getVehicle(vehicleId);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.util.List)
	 */
	@Override
	public Collection<IpcVehicle> get(Collection<String> vehicleIds) 
			throws RemoteException {
		return getSerializableCollection(
				vehicleDataCache.getVehicles(vehicleIds));
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.util.List)
	 */
	@Override
	public Collection<IpcVehicleComplete> getComplete(Collection<String> vehicleIds) 
			throws RemoteException {
		return getCompleteSerializableCollection(
				vehicleDataCache.getVehicles(vehicleIds));
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.lang.String)
	 */
	@Override
	public Collection<IpcVehicle> getForRoute(String routeIdOrShortName) 
			throws RemoteException {
		return getSerializableCollection(
				vehicleDataCache.getVehiclesForRoute(routeIdOrShortName));
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.lang.String)
	 */
	@Override
	public Collection<IpcVehicleComplete> getCompleteForRoute(String routeIdOrShortName) 
			throws RemoteException {
		return getCompleteSerializableCollection(
				vehicleDataCache.getVehiclesForRoute(routeIdOrShortName));
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.util.Collection)
	 */
	@Override
	public Collection<IpcVehicle> getForRoute(
			Collection<String> routeIdsOrShortNames) throws RemoteException {
	    return getSerializableCollection(
			vehicleDataCache.getVehiclesForRoute(routeIdsOrShortNames));
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.util.Collection)
	 */
	@Override
	public Collection<IpcVehicleComplete> getCompleteForRoute(
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
			Collection<IpcVehicleComplete> vehicles) {
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
	private Collection<IpcVehicleGtfsRealtime> getGtfsRealtimeSerializableCollection(
			Collection<IpcVehicleComplete> vehicles) {
		// If vehicles is null then return empty array.
		if (vehicles == null)
			return new ArrayList<IpcVehicleGtfsRealtime>();
		
		return new ArrayList<IpcVehicleGtfsRealtime>(vehicles);
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
	private Collection<IpcVehicleComplete> getCompleteSerializableCollection(
			Collection<IpcVehicleComplete> vehicles) {
		// If vehicles is null then return empty array.
		if (vehicles == null)
			return new ArrayList<IpcVehicleComplete>();
		
		if (vehicles instanceof Serializable) { 
			return vehicles;
		} else {
			return new ArrayList<IpcVehicleComplete>(vehicles);
		}			
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocks()
	 */
	@Override
	public Collection<IpcActiveBlock> getActiveBlocks(
			Collection<String> routeIds, int allowableBeforeTimeSecs)
			throws RemoteException {
		// List of data to be returned
		List<IpcActiveBlock> results = 
				new ArrayList<IpcActiveBlock>();
		// Determine all the active blocks
		List<Block> blocks =
				BlocksInfo.getCurrentlyActiveBlocks(routeIds, null,
						allowableBeforeTimeSecs, -1);
		// For each active block determine associated vehicle
		for (Block block : blocks) {
			IpcBlock ipcBlock = new IpcBlock(block);
			
			// If a block doesn't have a vehicle associated with it need
			// to determine which route a block is currently associated with
			// since can't get that info from the vehicle. This way the block
			// can be properly grouped with the associated route even when it
			// doesn't have a vehicle assigned.
			int activeTripIndex = block.activeTripIndex(new Date(), 
					allowableBeforeTimeSecs);
			
			// Determine vehicles associated with the block if there are any
			Collection<String> vehicleIdsForBlock = VehicleDataCache
					.getInstance().getVehiclesByBlockId(block.getId());
			Collection<IpcVehicle> ipcVehiclesForBlock = get(vehicleIdsForBlock);
			
			// Create and add the IpcActiveBlock
			Trip tripForSorting = block.getTrip(activeTripIndex);
			IpcActiveBlock ipcBlockAndVehicle =
					new IpcActiveBlock(ipcBlock, activeTripIndex,
							ipcVehiclesForBlock, tripForSorting);
			results.add(ipcBlockAndVehicle);
		}
		// Sort the results so that ordered by route and then block start time
		IpcActiveBlock.sort(results);
		
		// Return results
		return results;
	}

	/* (non-Javadoc)
   * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocks()
   */
  @Override
  public int getNumActiveBlocks(
      Collection<String> routeIds, int allowableBeforeTimeSecs)
      throws RemoteException {
    // Determine all the active blocks
    List<Block> blocks =
        BlocksInfo.getCurrentlyActiveBlocks(routeIds, null,
            allowableBeforeTimeSecs, -1);
    
    return blocks.size();
  }
  
    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocks()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocksWithoutVehicles(
            Collection<String> routeIds, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
            throws RemoteException {
        // List of data to be returned
        List<IpcActiveBlock> results =
                new ArrayList<IpcActiveBlock>();
        // Determine all the active blocks
        List<Block> blocks =
                BlocksInfo.getCurrentlyActiveBlocks(routeIds, null,
                        allowableBeforeTimeSecs, -1);
        // For each active block determine associated vehicle
        for (Block block : blocks) {
            try{
                IpcBlock ipcBlock = new IpcBlock(block);
                int activeTripIndex = block.activeTripIndex(new Date(),
                        allowableBeforeTimeSecs);


                // Create and add the IpcActiveBlock, skipping the slow vehicle fetching
                Trip tripForSorting = block.getTrip(activeTripIndex);

                if(!includeCanceledTrips && CanceledTripCache.getInstance().isCanceled(tripForSorting.getId())){
                	continue;
				}

                IpcActiveBlock ipcBlockAndVehicle =
                        new IpcActiveBlock(ipcBlock, activeTripIndex,
                                new ArrayList<IpcVehicle>(), tripForSorting);
                results.add(ipcBlockAndVehicle);
            }catch (Exception e){
                logger.warn("Error while fecthing active blocks data (probably hibernate still loading data): " + e.getMessage());
            }
        }
        // Sort the results so that ordered by route and then block start time
        IpcActiveBlock.sort(results);

        // Return results
        return results;
    }


    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocksAndVehiclesByRouteId()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(
            String routeId, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
            throws RemoteException {

        Collection<String> routeIds = new ArrayList<>();
        routeIds.add(routeId);
        return getActiveBlocksAndVehiclesByRouteId(routeIds, allowableBeforeTimeSecs, includeCanceledTrips);
    }
    
    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocksAndVehiclesByRouteName()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteName(
            String routeName, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
            throws RemoteException {

        Session session = HibernateUtils.getSession();
        Criteria criteria = session.createCriteria(Route.class)
            .add(Restrictions.eq("name", routeName))
            .setProjection(Projections.groupProperty("id"));
        List<String> routeIds = criteria.list();
        session.close();
        
        return getActiveBlocksAndVehiclesByRouteId(routeIds, allowableBeforeTimeSecs, includeCanceledTrips);
    }
    
    private Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(
        Collection<String> routeIds, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
        throws RemoteException {

      // List of data to be returned
      List<IpcActiveBlock> results =
              new ArrayList<IpcActiveBlock>();
      // Determine all the active blocks
      List<Block> blocks =
              BlocksInfo.getCurrentlyActiveBlocks(routeIds, null,
                      allowableBeforeTimeSecs, -1);
      // For each active block determine associated vehicle
      for (Block block : blocks) {
          IpcBlock ipcBlock = new IpcBlock(block);
          // If a block doesn't have a vehicle associated with it need
          // to determine which route a block is currently associated with
          // since can't get that info from the vehicle. This way the block
          // can be properly grouped with the associated route even when it
          // doesn't have a vehicle assigned.
          int activeTripIndex = block.activeTripIndex(new Date(),
                  allowableBeforeTimeSecs);
  
          Trip tripForSorting = block.getTrip(activeTripIndex);
          
          // Check that block's active trip is for the specified route
          // (Otherwise, could be a past or future trip)
          if (routeIds != null && !routeIds.isEmpty() && !routeIds.contains(tripForSorting.getRouteId()))
            continue;
          
          // Determine vehicles associated with the block if there are any
          Collection<String> vehicleIdsForBlock = VehicleDataCache
                  .getInstance().getVehiclesByBlockId(block.getId());
          Collection<IpcVehicle> ipcVehiclesForBlock = get(vehicleIdsForBlock);
  
          // Create and add the IpcActiveBlock
          IpcActiveBlock ipcBlockAndVehicle =
                  new IpcActiveBlock(ipcBlock, activeTripIndex,
                          ipcVehiclesForBlock, tripForSorting);
          results.add(ipcBlockAndVehicle);
      }
      // Sort the results so that ordered by route and then block start time
      IpcActiveBlock.sort(results);
  
      // Return results
      return results;
   }

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.VehiclesInterface#getVehicleConfigs()
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
   * @see org.transitclock.ipc.interfaces.VehiclesInterface#getVehiclesForBlocks()
   */
  @Override
	public Collection<IpcVehicle> getVehiclesForBlocks() throws RemoteException {
	  List<String> vehicleIds = new ArrayList<String>();
	  List<Block> blocks = BlocksInfo.getCurrentlyActiveBlocks();
	  for (Block block : blocks) {
	    Collection<String> vehicleIdsForBlock = VehicleDataCache
          .getInstance().getVehiclesByBlockId(block.getId());
	    vehicleIds.addAll(vehicleIdsForBlock);
	  }
	  return get(vehicleIds);
	}
}
