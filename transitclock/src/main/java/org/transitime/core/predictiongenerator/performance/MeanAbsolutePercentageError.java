package org.transitime.core.predictiongenerator.performance;

import java.util.List;

/**
 * @author Sean Og Crudden
 * https://en.wikipedia.org/wiki/Mean_absolute_percentage_error
 */
public class MeanAbsolutePercentageError {
	private Long actualDuration = null;
	private List<Long> predictions = null; 
	
	public MeanAbsolutePercentageError(Long actualDuration, List<Long> predictions) {
		this.actualDuration=actualDuration;
		this.predictions=predictions;
		
	}
	public Long getMAPE() throws Exception
	{
		if(actualDuration !=null && predictions!=null && predictions.size()>0 )
		{					
			Long totalDifference= 0L;
			
			for(Long prediction:predictions)
			{
				totalDifference=totalDifference+Math.abs(actualDuration-prediction);
			}
			return totalDifference/predictions.size();
		}else
		{	
			throw new Exception("All paramters must be set.");
		}
	}
}
