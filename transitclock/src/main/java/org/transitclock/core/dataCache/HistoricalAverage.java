package org.transitclock.core.dataCache;

import java.io.Serializable;

/**
 * @author Sean Og Crudden
 * 
 */
public class HistoricalAverage  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2103092627208174290L;

	@Override
	public String toString() {
		return "HistoricalAverage [count=" + count + ", average=" + average + "]";
	}
	public HistoricalAverage(double average) {
		count = 1;
		this.average = average;
	}
	public HistoricalAverage(int count, double average) {
		this.count = count;
		this.average = average;
	}

	private int count;
	
	double average;
	
	public int getCount() {
		return count;
	}
	public double getAverage() {
		return average;
	}

	public void updateBad(double element)
	{
		average=((count*average)+element)/(count+1);		
		count=count+1;		
	}

	public HistoricalAverage copyUpdate(double element) {
		double average1=((count*average)+element)/(count+1);
		return new HistoricalAverage(count+1, average1);

	}

	
}
