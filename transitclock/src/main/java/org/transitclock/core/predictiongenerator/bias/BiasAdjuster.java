package org.transitclock.core.predictiongenerator.bias;

/**
 * @author scrudden
 * Implement this to add bias adjustment if needed.
 *
 */
public interface BiasAdjuster {
	public long adjustPrediction(long prediction);
}
