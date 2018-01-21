package org.transitime.core.dataCache;

import java.util.Comparator;

import org.transitime.ipc.data.IpcPrediction;
public class PredictionComparator implements Comparator<IpcPrediction> {	
	

	@Override
	public int compare(IpcPrediction p1, IpcPrediction p2) {
		
		if(p1.getPredictionTime()>p2.getPredictionTime())
		{
			 return 1;
		}else if(p1.getPredictionTime()< p2.getPredictionTime())
		{
			 return -1;
		}
		return 0;
	}
}
