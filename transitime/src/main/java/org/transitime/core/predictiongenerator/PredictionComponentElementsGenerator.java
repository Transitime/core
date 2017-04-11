package org.transitime.core.predictiongenerator;

import org.transitime.core.Indices;
import org.transitime.core.SpatialMatch;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.AvlReport;

public interface PredictionComponentElementsGenerator {
	/* this generates a prediction for travel time between stops */
	long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState);
	
	long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState);
	
	long expectedTravelTimeFromMatchToEndOfStopPath(AvlReport avlReport, SpatialMatch match);

}