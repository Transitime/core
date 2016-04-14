package org.transitime.core.predictiongenerator;

import org.transitime.core.Indices;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.data.IpcPrediction;

public interface PredictionComponentElementsGenerator {
	/* this generates a prediction for travel time between stops */
	long getTravelTimeForPath(Indices indices, AvlReport avlReport);
	
	/* this generates a prediction for the time spent at a stop. */	
	IpcPrediction generatePredictionForStop(AvlReport avlReport,
			Indices indices, long predictionTime, boolean useArrivalTimes,
			boolean affectedByWaitStop, boolean isDelayed,
			boolean lateSoMarkAsUncertain);
}