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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Headway;


/**
 *
 * @author SkiBu Smith
 *
 */
public class HeadwayGeneratorDefaultImpl implements HeadwayGenerator {

	private static final Logger logger = 
			LoggerFactory.getLogger(HeadwayGeneratorDefaultImpl.class);

	/********************** Member Functions 
	 * @return **************************/

	/* (non-Javadoc)
	 * @see org.transitclock.core.HeadwayGenerator#generate(org.transitclock.core.VehicleState)
	 */
	@Override
	public Headway generate(VehicleState vehicleState) {		
		// FIXME Still needs to be implemented!!
		logger.debug("HeadwayGeneratorDefaultImpl.generate() still needs to " +
				"be implemented");
		return null;
		
		
	}
}
