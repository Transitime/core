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

/**
 * Defines the interface for generating headway information. To create headway info using
 * an alternate method simply implement this interface and configure
 * HeadwayGeneratorFactory to instantiate the new class when a
 * HeadwayGenerator is needed.
 *
 * @author SkiBu Smith
 *
 */
public interface HeadwayGenerator {
	/**
	 * Generates headway info. This interface likely will need to be changed in
	 * the future to return the headways generated such that the MatchProcessor
	 * can manage them and store them away.
	 * 
	 * @param vehicleState
	 */
	public void generate(VehicleState vehicleState);
}
