/**
 * 
 */
package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.StopPathPredictionCacheFactory;
import org.transitclock.db.structs.PredictionForStopPath;
import org.transitclock.ipc.data.IpcPredictionForStopPath;
import org.transitclock.ipc.interfaces.PredictionAnalysisInterface;
import org.transitclock.ipc.rmi.AbstractServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Óg Crudden 
 * Server to allow stored travel time predictions to be queried.
 * TODO May not be set to run by default as really only for analysis of predictions.
 * TODO This needs to be changed to also work with frequency based services.
 */
public class PredictionAnalysisServer extends AbstractServer implements PredictionAnalysisInterface {
	// Should only be accessed as singleton class
	private static PredictionAnalysisServer singleton;

	private static final Logger logger = LoggerFactory.getLogger(PredictionAnalysisServer.class);

	protected PredictionAnalysisServer(String agencyId) {
		super(agencyId, PredictionAnalysisInterface.class.getSimpleName());

	}

	/**
	 * Starts up the PredictionAnalysisServer so that RMI calls can be used to query
	 * travel times prediction stored. This will automatically cause the object to continue to run and
	 * serve requests.
	 * 
	 * @param agencyId
	 * @return the singleton PredictionAnalysisServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static PredictionAnalysisServer start(String agencyId) {
		if (singleton == null) {
			singleton = new PredictionAnalysisServer(agencyId);
		}
		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error(
					"Tried calling PredictionAnalysisInterface.start() for "
							+ "agencyId={} but the singleton was created for agencyId={}",
					agencyId, singleton.getAgencyId());
			return null;
		}	
		return singleton;
	}

	
	
	@Override
	public List<IpcPredictionForStopPath> getRecordedTravelTimePredictions(String tripId, Integer stopPathIndex,
			Date startdate, Date enddate, String algorithm) throws RemoteException {
		List<PredictionForStopPath> result = PredictionForStopPath.getPredictionForStopPathFromDB(startdate, enddate, algorithm, tripId, stopPathIndex);
		List<IpcPredictionForStopPath> results=new ArrayList<IpcPredictionForStopPath>();
		for(PredictionForStopPath prediction:result)
		{
			IpcPredictionForStopPath ipcPrediction=new IpcPredictionForStopPath(prediction);
			results.add(ipcPrediction);
		}
		
		
		return results;
	}

	@Override
	public List<IpcPredictionForStopPath> getCachedTravelTimePredictions(String tripId, Integer stopPathIndex,
			Date startdate, Date enddate, String algorithm) throws RemoteException {
		StopPathCacheKey key=new StopPathCacheKey(tripId,stopPathIndex,true);
		List<PredictionForStopPath> predictions = StopPathPredictionCacheFactory.getInstance().getPredictions(key);
		List<IpcPredictionForStopPath> results=new ArrayList<IpcPredictionForStopPath>();
		if(predictions!=null)
		{
			for(PredictionForStopPath prediction:predictions)
			{
				IpcPredictionForStopPath ipcPrediction=new IpcPredictionForStopPath(prediction);
				if(algorithm!=null&&algorithm.length()>0)
				{
					if(algorithm.equals(prediction.getAlgorithm()))
					{
						results.add(ipcPrediction);
					}
				}else
				{				
					results.add(ipcPrediction);
				}
			}
		}
		return results;
	}

}
