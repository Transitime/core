package org.transitclock.core.predictiongenerator.datafilter;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class TravelTimeFilterFactory {

	/**
	 * 
	 * @author scrudden 
	 * Returns the filter that is used to exclude bad travel time data.
	 *         
	 */
	
	private static TravelTimeDataFilter singleton = null;

	// The name of the class to instantiate
	private static StringConfigValue className = new StringConfigValue("transitclock.core.predictiongenerator.datafilter.traveltime",
			"org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilterImpl",
			"Specifies the name of the class used to filter travel times.");
	
	public static TravelTimeDataFilter getInstance() {

		if (singleton == null) {
			singleton=ClassInstantiator.instantiate(className.getValue(), TravelTimeDataFilter.class);
		}
		return singleton;
	}

}
