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
package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcDoubleSummaryStatistics;
import org.transitclock.utils.MathUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.DoubleSummaryStatistics;

/**
 * For outputting average run time value

 * @author Lenny Caraballo
 *
 */
@XmlRootElement
public class ApiAverageRunTime {

	@XmlAttribute
	private Double averageRunTime;

	@XmlAttribute
	private int numberOfTrips;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiAverageRunTime() {
	}

	/**
	 * Creates API to display average run time value.
	 * @param summaryStatistics
	 */
	public ApiAverageRunTime(IpcDoubleSummaryStatistics summaryStatistics) {
		this.numberOfTrips = (int) summaryStatistics.getCount();
		if(numberOfTrips == 0){
			this.averageRunTime = null;
		} else{
			this.averageRunTime = MathUtils.round(summaryStatistics.getAverage(), 0);
		}

	}
	
}
