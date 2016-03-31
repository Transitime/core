package org.transitime.core.predictiongenerator;

import org.transitime.core.Indices;
import org.transitime.db.structs.AvlReport;

public interface PredictionGenerator {
	long getTravelTimeForPath(Indices indices, AvlReport avlReport);
}