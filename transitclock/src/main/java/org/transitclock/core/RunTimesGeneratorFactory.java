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

package org.transitclock.core;

import org.transitclock.config.StringConfigValue;
import org.transitclock.core.reporting.RunTimeGenerator;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a RunTimeGenerator object that generates runtime info when
 * a new match is generated for a vehicle. The class to be instantiated can be
 * set using the config variable transitclock.core.runTimesGeneratorClass
 * 
 * @author SkiBu Smith
 * 
 */
public class RunTimesGeneratorFactory {

	// The name of the class to instantiate
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.runTimesGeneratorClass",
					"org.transitclock.core.reporting.RunTimeGenerator",
					"Specifies the name of the class used for generating " +
					"runtimes data.");

	private static RunTimeGenerator singleton = null;

	/********************** Member Functions **************************/

	public static RunTimeGenerator getInstance() {
		// If the PredictionGenerator hasn't been created yet then do so now
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(),
					RunTimeGenerator.class);
		}
		
		return singleton;
	}

}
