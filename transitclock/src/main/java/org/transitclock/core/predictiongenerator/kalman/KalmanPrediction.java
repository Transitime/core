package org.transitclock.core.predictiongenerator.kalman;

/**
 * @author Sean Óg Crudden
 *
 */
public class KalmanPrediction 
{	
		
	/**
	 * @param last_vehicle_segment The last vehicles info for the time taken to cover the same segment
	 * @param historical_segments The last 3 days for info relating to the time taken for the vehicle handling the same service/trip took. 
	 * @param last_prediction_error From the previous segments calculation result. (I am 99.9% sure you just start the chain of calcuations with an estimate)
	 * @return KalmanPredictionResult which contains the predicted time and the last_prediction_error to be used in the next prediction calculation.
	 * @throws Exception
	 */
	public KalmanPredictionResult predict(TripSegment last_vehicle_segment,TripSegment historical_segments[], double last_prediction_error) throws Exception
	{
		KalmanPredictionResult result=null; 								
						
		double average=historicalAverage(historical_segments);
		
		double variance = historicalVariance(historical_segments, average);
						
		double gain=gain(average, variance, last_prediction_error );	
		
		double loop_gain=1-gain;
		
		result=new KalmanPredictionResult(prediction(gain, loop_gain, historical_segments, last_vehicle_segment),filterError( variance, gain));
						
		return result;				
	}
	private double historicalAverage(TripSegment historical_segments[]) throws Exception	
	{
		if(historical_segments.length>0)
		{
			long total=0;
			for(int i=0;i<historical_segments.length;i++)
			{
				long duration=historical_segments[i].getDestination().getTime()-historical_segments[i].getOrigin().getTime();
				total=total+duration;
			}
			return (double) (total/historical_segments.length);
		}else
		{
			throw new Exception("Cannot average nothing");
		}				
	}
	private double historicalVariance(TripSegment historical_segments[], double average)		
	{			
		double total=0;
		
		for(int i=0;i<historical_segments.length;i++)
		{
			long duration=historical_segments[i].getDestination().getTime()-historical_segments[i].getOrigin().getTime();
			
			double diff=duration-average;
			
			double long_diff_squared=diff*diff;
			
			total=total+long_diff_squared;
		}		
		return total/historical_segments.length;		
	}
	private double filterError(double variance, double loop_gain)
	{
		return variance*loop_gain;
	}
	
	private double gain(double average, double variance, double last_prediction_error )		
	{
		double gain=(last_prediction_error+variance)/(last_prediction_error+(2*variance));
		return gain;				
	}
	
	private double prediction(double gain, double loop_gain, TripSegment historical_segments[],  TripSegment last_vechicle_segment)	
	{
		/* TODO This may be better use the historical average rather than just the vehicle on previous day. This would damping issues with last days value being dramtically different. */
		long historical_duration=historical_segments[historical_segments.length-1].getDestination().getTime()-historical_segments[historical_segments.length-1].getOrigin().getTime();
		
		long last_vehicle_duration=last_vechicle_segment.getDestination().getTime()-last_vechicle_segment.getOrigin().getTime();
				
		double prediction=(loop_gain*last_vehicle_duration)+(gain*historical_duration);
		
		return prediction;	
	}	
	public static void main(String [ ] args)
	{
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 380, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 420, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 400, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 300, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			KalmanPredictionResult result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  72.40);
			
			if(result!=null)
			{
				if((result.getResult() > 355 && result.getResult() < 356) && (result.getFilterError()>149 && result.getFilterError()<150))
				{
					System.out.println("Successful Kalman Filter Prediction.");
				}else
				{
					System.out.println("UnSuccessful Kalman Filter Prediction.");
				}
			}else
			{
				System.out.println("No result.");
			}
		} catch (Exception e) {			
			System.out.println("Whoops");
			e.printStackTrace();
		}		
	}
}
