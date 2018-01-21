/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains predictions for multiple stops. Has information for agency as well
 * since intended to be used when providing predictions by location for
 * multiple agencies.
 * 
 * @author Michael
 *
 */
@XmlRootElement(name = "preds")
public class ApiNearbyPredictionsForAgencies {
	
	@XmlElement(name = "agencies")
	private List<ApiPredictions> predictionsForAgency;

	/**
	 * Constructor. Method addPredictionsForAgency() called to actually add
	 * data.
	 */
	public ApiNearbyPredictionsForAgencies() {
		predictionsForAgency = new ArrayList<ApiPredictions>();
	}

	/**
	 * Adds predictions for an agency.
	 * 
	 * @param apiPreds
	 */
	public void addPredictionsForAgency(ApiPredictions apiPreds) {
		predictionsForAgency.add(apiPreds);
	}
}
