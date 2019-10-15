package org.transitclock.core.predictiongenerator.bias;

public interface BiasAdjuster {
	public long adjustPrediction(long prediction);
}
