package org.transitclock.core.predictiongenerator.bias;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

public class ExponentialBiasAdjuster implements BiasAdjuster {
	private double percentage=Double.NaN;
	
	private double number=Double.NaN;
	
	private static DoubleConfigValue baseNumber = new DoubleConfigValue(
			"org.transitclock.core.predictiongenerator.bias.exponential.basenumber", 1.1,
			"Base number to be raised to the power of the horizon minutes.");
	
	private static IntegerConfigValue updown = new IntegerConfigValue(
			"org.transitclock.core.predictiongenerator.bias.exponential.updown", -1,
			"Is the adjustment up or down? Set +1 or -1.");
	@Override
	public long adjustPrediction(long prediction) {

	
		double tothepower=(prediction/1000)/60;
		percentage = Math.pow(number, tothepower)-1;
		
		double new_prediction = prediction + (updown.getValue()*(((percentage/100)*prediction)));			
		return (long) new_prediction;
	}
	public ExponentialBiasAdjuster() {
		super();
		this.number=baseNumber.getValue();
	}
	public static void main(String [ ] args)
	{
		ExponentialBiasAdjuster adjuster=new ExponentialBiasAdjuster();
		long result = adjuster.adjustPrediction( 20*Time.MS_PER_MIN);
		System.out.println("Percentage is :"+adjuster.getPercentage() +" giving a result to :"+Math.round(result/Time.MS_PER_SEC));
		
		result=adjuster.adjustPrediction( 15*Time.MS_PER_MIN);
		System.out.println("Percentage is :"+adjuster.getPercentage() +" giving a result to :"+Math.round(result/Time.MS_PER_SEC));
		
		result=adjuster.adjustPrediction( 10*Time.MS_PER_MIN);
		System.out.println("Percentage is :"+adjuster.getPercentage() +" giving a result to :"+Math.round(result/Time.MS_PER_SEC));
		
		result=adjuster.adjustPrediction( 5*Time.MS_PER_MIN);
		System.out.println("Percentage is :"+adjuster.getPercentage() +" giving a result to :"+Math.round(result/Time.MS_PER_SEC));
		
		result=adjuster.adjustPrediction( 1*Time.MS_PER_MIN);
		System.out.println("Percentage is :"+adjuster.getPercentage() +" giving a result to :"+Math.round(result/Time.MS_PER_SEC));
		
	}
	public double getPercentage() {
		return percentage;
	}
	
	
	public double getNumber() {
		return number;
	}
	@Override
	public String toString() {
		return "ExponentialBiasAdjuster [percentage=" + percentage + ", number=" + number + "]";
	}
	
}
