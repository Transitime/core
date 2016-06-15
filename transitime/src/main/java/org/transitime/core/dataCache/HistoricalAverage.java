package org.transitime.core.dataCache;

public class HistoricalAverage {
	
	@Override
	public String toString() {
		return "HistoricalAverage [count=" + count + ", average=" + average + "]";
	}
	public HistoricalAverage() {
		super();
		count=0;
		average=0;	
	}

	private int count;
	
	double average;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public double getAverage() {
		return average;
	}
	public void setAverage(double average) {
		this.average = average;
	}
	
	public void update(double element)
	{
		average=((count*average)+element)/(count+1);		
		count=count+1;		
	}

	
}
