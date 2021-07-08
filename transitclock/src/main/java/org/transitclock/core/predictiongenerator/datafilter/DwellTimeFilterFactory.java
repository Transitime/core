package org.transitclock.core.predictiongenerator.datafilter;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class DwellTimeFilterFactory {

	/**
	 * 
	 * @author scrudden Returns the filter that is used to exclude bad dwell time
	 *         data.
	 *
	 */
	
	private static DwellTimeDataFilter singleton = null;

	// The name of the class to instantiate
	private static StringConfigValue className = new StringConfigValue("transitclock.core.predictiongenerator.datafilter.dwelltime",
			"org.transitclock.core.predictiongenerator.datafilter.DwellTimeDataFilterImpl",
			"Specifies the name of the class used to filter dwell times.");

	public static DwellTimeDataFilter getInstance() {

		if(singleton==null)
			singleton=ClassInstantiator.instantiate(className.getValue(), DwellTimeDataFilter.class);
		return singleton;
	}

}
