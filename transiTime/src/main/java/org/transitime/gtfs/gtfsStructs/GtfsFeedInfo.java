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

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitime.utils.csv.CsvBase;


/**
 * A GTFS feed_info object. 
 * @author SkiBu Smith
 *
 */
public class GtfsFeedInfo extends CsvBase {

	private final String feedPublisherName;
	private final String feedPublisherUrl;
	private final String feedLang;
	private final String feedStartDate;
	private final String feedEndDate;
	private final String feedVersion;

	/********************** Member Functions **************************/

	/**
	 * Creates a GtfsFeedInfo object by reading the data
	 * from the CSVRecord.
	 * @param record
	 * @param supplemental
	 * @param fileName for logging errors
	 */
	public GtfsFeedInfo(CSVRecord record, boolean supplemental, String fileName) 
			throws ParseException {
		super(record, supplemental, fileName);

		feedPublisherName = getRequiredValue(record, "feed_publisher_name");
		feedPublisherUrl = getRequiredValue(record, "feed_publisher_url");
		feedLang = getRequiredValue(record, "feed_lang");		
		feedStartDate = getOptionalValue(record, "feed_start_date");
		feedEndDate = getOptionalValue(record, "feed_end_date");
		feedVersion = getOptionalValue(record, "feed_version");
	}

	public String getFeedPublisherName() {
		return feedPublisherName;
	}

	public String getFeedPublisherUrl() {
		return feedPublisherUrl;
	}

	public String getFeedLang() {
		return feedLang;
	}

	public String getFeedStartDate() {
		return feedStartDate;
	}

	public String getFeedEndDate() {
		return feedEndDate;
	}

	public String getFeedVersion() {
		return feedVersion;
	}
	
	@Override
	public String toString() {
		return "GtfsFeedInfo [" 
				+ "lineNumber=" + lineNumber 
				+ ", feedPublisherName=" + feedPublisherName
				+ ", feedPublisherUrl=" + feedPublisherUrl 
				+ ", feedLang=" + feedLang 
				+ ", feedStartDate=" + feedStartDate
				+ ", feedEndDate=" + feedEndDate 
				+ ", feedVersion="	+ feedVersion 
				+ "]";
	}

}
