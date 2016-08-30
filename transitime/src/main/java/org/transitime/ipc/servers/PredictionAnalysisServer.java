/**
 * 
 */
package org.transitime.ipc.servers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.PredictionForStopPath;
import org.transitime.ipc.data.IpcPredictionForStopPath;
import org.transitime.ipc.interfaces.PredictionAnalysisInterface;
import org.transitime.ipc.rmi.AbstractServer;

/**
 * @author Sean Og Crudden Server to allow stored travel time predictions to be queried.
 * TODO May not be set to run by default as really only for analysis of predictions.
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

}
