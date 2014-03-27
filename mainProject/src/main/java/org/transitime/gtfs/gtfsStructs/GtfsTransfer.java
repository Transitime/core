/**
 * 
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
package org.transitime.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author SkiBu Smith
 *
 */
public class GtfsTransfer extends GtfsBase {

	private final String fromStopId;
	private final String toStopId;
	private final String transferType;
	private final Integer minTransferTime;

	/********************** Member Functions **************************/

	/**
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsTransfer(CSVRecord record, boolean supplemental, String fileName)
			throws NumberFormatException {
		super(record, supplemental, fileName);

		fromStopId = getRequiredValue(record, "from_stop_id");
		toStopId = getRequiredValue(record, "to_stop_id");
		transferType = getRequiredValue(record, "transfer_type");
		
		String timeStr = getOptionalValue(record, "min_transfer_time");
		if (timeStr != null)
			minTransferTime = Integer.parseInt(timeStr);
		else
			minTransferTime = null;
	}

	public String getFromStopId() {
		return fromStopId;
	}

	public String getToStopId() {
		return toStopId;
	}

	public String getTransferType() {
		return transferType;
	}

	public Integer getMinTransferTime() {
		return minTransferTime;
	}

	@Override
	public String toString() {
		return "GtfsTransfer ["
				+ "lineNumber=" + lineNumber 
				+", fromStopId=" + fromStopId + 
				", toStopId=" + toStopId + 
				", transferType=" + transferType
				+ ", minTransferTime=" + minTransferTime + "]";
	}
	
	
}
