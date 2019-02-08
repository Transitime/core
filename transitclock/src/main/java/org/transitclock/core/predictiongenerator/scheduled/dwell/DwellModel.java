package org.transitclock.core.predictiongenerator.scheduled.dwell;
/**
 * 
 * @author scrudden
 * To be implemented if you want to create an algorithm to predict dwell time. 
 * The possible inputs to any such algorithms are dwelltime, headway and demand.
 *
 */
public interface DwellModel {	
	public Integer predict(Integer headway, Integer demand);
	public void putSample(Integer dwelltime, Integer headway, Integer demand);		
}
