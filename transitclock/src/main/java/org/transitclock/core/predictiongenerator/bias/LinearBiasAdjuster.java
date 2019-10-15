package org.transitclock.core.predictiongenerator.bias;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.utils.Time;

/**
 * @author scrudden
 * 
 * This will adjust a prediction based on a percentage which increases linearly as the horizon gets bigger. 
 * 
 * The rate of increase of the percentage can be set using the constructor.
 *
 */
public class LinearBiasAdjuster implements BiasAdjuster {

	private double rate=-Double.NaN;
	
	
	private static DoubleConfigValue rateChangePercentage = new DoubleConfigValue(
			"org.transitclock.core.predictiongenerator.bias.linear.rate", -0.0006,
			"Rate at which percentage adjustment changes with horizon.");
	
	
	public LinearBiasAdjuster() {
		super();
		this.rate=rateChangePercentage.getValue();
	}
	public LinearBiasAdjuster(double rateChangePercentage) {
		super();
		this.rate = rateChangePercentage;
	}
	private double percentage=Double.NaN;
		
	/* going to adjust by a larger percentage as horizon gets bigger.*/
	
	@Override
	public long adjustPrediction(long prediction) {
		
		
		percentage = (prediction/100)*rate;
		
		double new_prediction = prediction+((percentage/100)*prediction);			
		return (long) new_prediction;
	}
	public double getRate() {
		return rate;
	}
		
	@Override
	public String toString() {
		return "LinearBiasAdjuster [rate=" + rate + ", percentage=" + percentage + "]";
	}
	public double getPercentage() {
		return percentage;
	}
	
	public static void main(String [ ] args)
	{
		LinearBiasAdjuster adjuster=new LinearBiasAdjuster(0.0006);
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
	
	
	
}
