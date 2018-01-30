package org.transitclock.core.predictiongenerator.kalman;

public class KalmanPredictionResult {
	double duration;
	double filterError;
	public KalmanPredictionResult(double result, double filterError) {
		super();
		this.duration = result;
		this.filterError = filterError;
	}
	/**
	 * @return the result
	 */
	public double getResult() {
		return duration;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(double result) {
		this.duration = result;
	}
	/**
	 * @return the filterError
	 */
	public double getFilterError() {
		return filterError;
	}
	/**
	 * @param filterError the filterError to set
	 */
	public void setFilterError(double filterError) {
		this.filterError = filterError;
	}

}
