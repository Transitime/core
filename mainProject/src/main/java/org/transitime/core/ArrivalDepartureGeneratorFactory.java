/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.core;

import org.transitime.config.StringConfigValue;
import org.transitime.utils.ClassInstantiator;

/**
 * For instantiating a ArrivalDepartureGenerator object that generates
 * arrival/departure data when a new match is generated for a vehicle. The class
 * to be instantiated can be set using the config variable
 * transitime.core.arrivalDepartureGeneratorClass
 * 
 * @author SkiBu Smith
 * 
 */
public class ArrivalDepartureGeneratorFactory {

	// The name of the class to instantiate
	private static StringConfigValue className = 
			new StringConfigValue("transitime.core.arrivalDepartureGeneratorClass", 
					"org.transitime.core.ArrivalDepartureGeneratorDefaultImpl");

	private static ArrivalDepartureGenerator singleton = null;

	/********************** Member Functions **************************/

	public static ArrivalDepartureGenerator getInstance() {
		// If the PredictionGenerator hasn't been created yet then do so now
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(), 
					ArrivalDepartureGenerator.class);
		}
		
		return singleton;
	}
}
