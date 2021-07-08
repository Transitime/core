package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;
/**
 * 
 * @author scrudden
 * Returns the model that is to be used to estimate dwell time for a stop.
 *
 */
public class DwellTimeModelFactory {
		// The name of the class to instantiate
		private static StringConfigValue className = 
				new StringConfigValue("transitclock.core.dwelltime.model", 
						"org.transitclock.core.predictiongenerator.scheduled.dwell.DwellAverage",
						"Specifies the name of the class used to predict dwell.");
		
		
		/********************** Member Functions **************************/
		public static DwellModel getInstance() {			
			
			return ClassInstantiator.instantiate(className.getValue(), 
						DwellModel.class);								
		}
}
