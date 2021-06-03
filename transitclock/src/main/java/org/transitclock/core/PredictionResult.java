package org.transitclock.core;

public class PredictionResult {

  private final long prediction;
  private final Algorithm algorithm;

  public PredictionResult(long prediction, Algorithm algorithm) {
    this.prediction = prediction;
    this.algorithm = algorithm;
  }

  public long getPrediction() {
    return prediction;
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }
}
