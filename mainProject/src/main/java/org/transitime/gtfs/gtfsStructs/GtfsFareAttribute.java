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
 * A GTFS fare_attributes object.
 * @author SkiBu Smith
 *
 */
public class GtfsFareAttribute extends GtfsBase {

	private final String fareId;
	private final float price;
	private final String currencyType;
	private final String paymentMethod;
	private final String transfers;
	private final Integer transferDuration;
	
	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsFareAttribute object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsFareAttribute(CSVRecord record, boolean supplemental, String fileName) 
			throws NumberFormatException {
		super(record, supplemental, fileName);

		fareId = getRequiredValue(record, "fare_id");
		price = Float.parseFloat(getRequiredValue(record, "price"));
		currencyType = getRequiredValue(record, "currency_type");
		paymentMethod = getRequiredValue(record, "payment_method");
		// Note: "transfers" is listed as required in the GTFS doc but it can
		// be empty. Therefore it is actually optional.
		transfers = getOptionalValue(record, "transfers");
		
		String transferDurationStr =
				getOptionalValue(record, "transfer_duration");
		if (transferDurationStr != null)
			transferDuration = Integer.parseInt(transferDurationStr);
		else
			transferDuration = null;
	}

	public String getFareId() {
		return fareId;
	}

	public float getPrice() {
		return price;
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public String getTransfers() {
		return transfers;
	}

	public Integer getTransferDuration() {
		return transferDuration;
	}

	@Override
	public String toString() {
		return "GtfsFareAttribute ["
				+"lineNumber=" + lineNumber
				+ ", fareId=" + fareId 
				+ ", price=" + price
				+ ", currencyType=" + currencyType 
				+ ", paymentMethod="	+ paymentMethod 
				+ ", transfers=" + transfers
				+ ", transferDuration=" + transferDuration + "]";
	}

	
}
