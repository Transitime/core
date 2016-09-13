package org.transitime.ipc.data;

import java.io.Serializable;

import org.transitime.core.dataCache.HistoricalAverage;

/**
 * @author Sean Og Crudden
 * Represents an historical average.
 *
 */
public class IpcHistoricalAverage implements Serializable{
	
	
	public IpcHistoricalAverage(HistoricalAverage historicalAverage) {
		super();
		
		if(historicalAverage!=null)
		{
			this.count = historicalAverage.getCount();
			this.average = historicalAverage.getAverage();
		}
	}


	public Integer getCount() {
		return count;
	}


	public void setCount(Integer count) {
		this.count = count;
	}


	public Double getAverage() {
		return average;
	}


	public void setAverage(Double average) {
		this.average = average;
	}


	private static final long serialVersionUID = -1285357644186049157L;


	private Integer count=0;
	

	private Double average=0.0;
}
