package org.transitime.core.holdingmethod;

import org.transitime.config.StringConfigValue;
import org.transitime.core.holdingmethod.HoldingTimeGenerator;
import org.transitime.utils.ClassInstantiator;
/**
 * @author Sean Óg Crudden
 */
public class HoldingTimeGeneratorFactory {
	// The name of the class to instantiate
		private static StringConfigValue className = 
				new StringConfigValue("transitime.core.holdingTimeGeneratorClass", 
						"org.transitime.core.holdingmethod.HoldingTimeGeneratorDefaultImpl",
						"Specifies the name of the class used for generating " +
						"holding times.");

		private static HoldingTimeGenerator singleton = null;
		
		/********************** Member Functions **************************/

		public static HoldingTimeGenerator getInstance() {
			// If the PredictionGenerator hasn't been created yet then do so now
			if (singleton == null) {
				if(className.getValue().length()>0)
					singleton = ClassInstantiator.instantiate(className.getValue(), 
							HoldingTimeGenerator.class);
			}
			
			return singleton;
		}
}
