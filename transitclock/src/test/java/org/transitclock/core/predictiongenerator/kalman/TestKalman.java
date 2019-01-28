package org.transitclock.core.predictiongenerator.kalman;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestKalman {
	static KalmanPredictionResult result;
	@Test
	public void test1() {
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 380, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 420, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 400, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 400, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  0);
								
			System.out.println(result);
			
		} catch (Exception e) {			
			System.out.println("Whoops");
			e.printStackTrace();
		}		
		
	}
	
	@Test
	public void test2() {
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 420, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 400, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 380, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 400, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  result.getFilterError());
			
			if(result!=null)
			{
				System.out.println(result);
				
			}else
			{
				System.out.println("No result.");
			}
		} catch (Exception e) {			
			System.out.println("Whoops");
			e.printStackTrace();
		}		
	}
	@Test
	public void test3() {
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 400, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 380, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 420, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 400, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  result.getFilterError());
			
			if(result!=null)
			{
				System.out.println(result);
				
			}else
			{
				System.out.println("No result.");
			}
		} catch (Exception e) {			
			System.out.println("Whoops");
			e.printStackTrace();
		}		
	}
	@Test
	public void test4() {
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 380, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 420, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 400, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 400, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  result.getFilterError());
			
			if(result!=null)
			{
				System.out.println(result);
				
			}else
			{
				System.out.println("No result.");
			}
		} catch (Exception e) {			
			System.out.println("Whoops");
			e.printStackTrace();
		}		
	}
	@Test
	public void test5() {
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 420, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 400, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 380, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 300, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  result.getFilterError());
			
			if(result!=null)
			{
				System.out.println(result);
				
			}else
			{
				System.out.println("No result.");
			}
		} catch (Exception e) {			
			System.out.println("Whoops");
			e.printStackTrace();
		}		
	}
	@Test
	public void test6() {
		KalmanPrediction kalmanPrediction=new KalmanPrediction();
		
		Vehicle vehicle=new Vehicle("RIY 30");
		
		VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle); 
		VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 400, vehicle);
		VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 380, vehicle);
		VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 420, vehicle);
		
		VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 500, vehicle);
				
		
		TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);  
		TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
		TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);
		
		TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);							
				
		TripSegment historical_segments_k[]={ts_day_1_k, ts_day_2_k,ts_day_3_k};
						
		TripSegment last_vehicle_segment=ts_day_0_k_1;
					
		try {
			result = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,  result.getFilterError());
			
			if(result!=null)
			{
				System.out.println(result);
				
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
