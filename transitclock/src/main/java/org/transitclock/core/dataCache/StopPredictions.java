package org.transitclock.core.dataCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.transitclock.db.structs.PredictionForStopPath;
public class StopPredictions implements Serializable {		
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -6487148805894879790L;
	
	public List <PredictionForStopPath> predictions = null;

	
	public StopPredictions(List<PredictionForStopPath> predictions) {
		super();
		this.predictions = predictions;
	}


	public List<PredictionForStopPath> getPredictions() {
		return predictions;
	}


	public void setPredictions(List<PredictionForStopPath> predictions) {
		this.predictions = predictions;
	}
	
	public void addPrediction(PredictionForStopPath prediction)
	{
		if(this.predictions==null)
		{
			predictions=new ArrayList<PredictionForStopPath>();
		}
		predictions.add(prediction);
	}


	public StopPredictions() {
		super();		
	}

}
