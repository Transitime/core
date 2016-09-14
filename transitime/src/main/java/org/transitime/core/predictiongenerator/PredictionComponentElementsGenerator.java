package org.transitime.core.predictiongenerator;

import org.transitime.core.Indices;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.data.IpcPrediction;

public interface PredictionComponentElementsGenerator {
	/* this generates a prediction for travel time between stops */
	long getTravelTimeForPath(Indices indices, AvlReport avlReport);
	
	long getStopTimeForPath(Indices indices, AvlReport avlReport);
	
	boolean hasDataForPath(Indices indices, AvlReport avlReport);
}