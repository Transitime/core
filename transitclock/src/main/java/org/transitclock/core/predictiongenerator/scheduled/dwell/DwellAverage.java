package org.transitclock.core.predictiongenerator.scheduled.dwell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.statistics.Statistics;
/**
 * 
 * @author scrudden
 * This is a running average for dwell times. 
 */
public class DwellAverage implements DwellModel, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7514794134817837972L;
	 			
	private static IntegerConfigValue samplesize= new IntegerConfigValue("transitclock.prediction.dwell.average.samplesize", 5 , "Max number of samples to keep for mean calculation.");
	private static DoubleConfigValue fractionLimitForStopTimes=new DoubleConfigValue("transitclock.prediction.dwell.average.fractionlimit", 0.7, "For when determining stop times. Throws out outliers if they are less than 0.7 or greater than 1/0.7 of the average."); 
	
	private List<Integer> values=new ArrayList<Integer>();
	// For this model headway or demand is not taken into account.
	@Override
	public void putSample(Integer value, Integer headway, Integer demand) 
	{	
					
		if(values.size() < samplesize.getValue())
		{
			values.add(value);
			Collections.rotate(values,1);
		}else
		{
			Collections.rotate(values,1);
			values.set(0, value);
		}
	}

	@Override
	public Integer predict(Integer headway, Integer demand) {
				
		return Statistics
		.filteredMean(values, 
				fractionLimitForStopTimes.getValue());		 
	}
	public static void main(String[] args)
	{
		DwellAverage average=new DwellAverage();
		average.putSample(new Integer(1), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(3), null, null);
		average.putSample(new Integer(4), null, null);
		average.putSample(new Integer(5), null, null);
		average.putSample(new Integer(6), null, null);
		average.putSample(new Integer(7), null, null);
		average.putSample(new Integer(8), null, null);
		average.putSample(new Integer(9), null, null);
		average.putSample(new Integer(10), null, null);
		average.putSample(new Integer(11), null, null);
		average.putSample(new Integer(12), null, null);
		
		System.out.println(average.predict(null, null));
		
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(9), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(11), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		average.putSample(new Integer(2), null, null);
		
		System.out.println(average.predict(null, null));
	}

}
