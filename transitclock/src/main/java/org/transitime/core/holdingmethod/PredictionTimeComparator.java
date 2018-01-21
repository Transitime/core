package org.transitime.core.holdingmethod;

import java.util.Comparator;


import org.transitime.ipc.data.IpcPrediction;
/**
 * @author Sean Óg Crudden
 * Compare the time of two predictions. Used to put predictions in order.
 */
public class PredictionTimeComparator implements Comparator<IpcPrediction> {

	@Override
	public int compare(IpcPrediction o1, IpcPrediction o2) {
	
		
		if(o1.getPredictionTime()==o2.getPredictionTime())
			return 0;
		else if(o1.getPredictionTime()>o2.getPredictionTime())		
			return 1;
		else
			return -1;			
	}
}
