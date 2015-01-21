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

package org.transitime.reports;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.GenericQuery;
import org.transitime.db.GenericQuery.GenericResult;
import org.transitime.reports.ChartJsonBuilder.RowBuilder;
import org.transitime.utils.IntervalTimer;

/**
 * For providing data to a Google scatter chart when need to specify specific
 * SQL for retrieving data from the database. Since any SQL statement can be
 * used this feed can be used for generating a variety of scatter charts.
 *
 * @author SkiBu Smith
 *
 */
public class JsonDataFeed {

    private static final Logger logger = LoggerFactory
	    .getLogger(JsonDataFeed.class);

    /********************** Member Functions **************************/

    /**
     * Adds the JSON columns definition for a Google chart.
     * 
     * @param builder
     * @param results
     */
    private static void addCols(ChartJsonBuilder builder,
	    List<GenericResult> results) {
	if (results == null || results.isEmpty()) {
	    logger.error("Called JsonDataFeed.getCols() but results is empty");
	    return;
	}
	
	// Add required type:"number" for each column of data
	GenericResult firstResult = results.get(0);
	for (int i=0; i<firstResult.numbers.size(); ++i) {
	    builder.addNumberColumn();
	}
	
	// If there is text associated with the GenericResult then add tooltip 
	// column
	if (firstResult.text != null) {
	    builder.addTooltipColumn();
	}
    }
    
    /**
     * Adds the JSON rows definition for a Google chart.
     * 
     * @param builder
     * @param results
     */
    private static void addRows(ChartJsonBuilder builder,
	    List<GenericResult> results) {
	if (results == null || results.isEmpty()) {
	    logger.error("Called JsonDataFeed.getRows() but results is empty");
	    return;
    	}
    
	IntervalTimer timer = new IntervalTimer();
	
	for (GenericResult row : results) {
	    // Start building up the row
	    RowBuilder rowBuilder = builder.newRow();
	    
	    // Add each cell for the row
	    for (Number number : row.numbers) {
		rowBuilder.addRowElement(number);
	    }
	    
	    // Add tooltip text if there is any
	    if (row.text != null) {
		rowBuilder.addRowElement(row.text);
	    }
	}
	  
	logger.debug("JsonDataFeed query took {}msec rows={}", 
		timer.elapsedMsec(), results);
    }
    
    /**
     * Converts results of SQL query into a JSON for a Google chart.
     * 
     * @param results
     * @return JSON string of results, or null if there are no results
     */
    public static String getJsonData(List<GenericResult> results) {
	// If no results then return null
	if (results == null || results.isEmpty())
	    return null;
	
	ChartJsonBuilder builder = new ChartJsonBuilder();
	addCols(builder, results);
	addRows(builder, results);
	
	String jsonString = builder.getJson();
	return jsonString;
    }
    
    /**
     * Does SQL query and returns JSON formatted results.
     * 
     * @param sql
     * @param dbType
     * @param dbHost
     * @param dbName
     * @param dbUserName
     * @param dbPassword
     * @return
     */
    public static String getJsonData(String sql, String dbType, String dbHost,
	    String dbName, String dbUserName, String dbPassword) {
	    List<GenericResult> results;
	    try {
		GenericQuery query = new GenericQuery(dbType, dbHost, dbName,
		    dbUserName, dbPassword);

		results = query.doQuery(sql);
	    } catch (SQLException e) {
		logger.error("Error getting data. ", e);
		return null;
	    }
	    		
	    String resultsStr = getJsonData(results);
	    return resultsStr;
    }
    
    public static void main(String[] args) {
	String dbType = "postgresql";// "mysql";
	String dbHost = "sfmta.c3zbap9ppyby.us-west-2.rds.amazonaws.com";// "localhost";
	String dbName = "mbta";
	String dbUserName = "transitime";// "root";
	String dbPassword = "transitime";

	String sql = "SELECT "
		+ "     to_char(arrivalDepartureTime-predictionReadTime, 'SSSS')::integer as predLength, "
		// + "     predictionAccuracyMsecs/1000 as predAccuracy, "
		+ "     abs(predictionAccuracyMsecs/1000) as absPredAccuracy, "
		+ "     format(E'Stop=%s tripId=%s\\\\narrDepTime=%s predTime=%s predReadTime=%s\\\\nvehicleId=%s source=%s', "
		+ "       stopId, tripId, "
		+ "       to_char(arrivalDepartureTime, 'MM/DD/YYYY HH:MM:SS.MS'),"
		+ "       to_char(predictedTime, 'HH:MM:SS.MS'),"
		+ "       to_char(predictionReadTime, 'HH:MM:SS.MS'),"
		+ "       vehicleId, predictionSource) AS tooltip "
		+ " FROM predictionaccuracy "
		+ "WHERE arrivaldeparturetime BETWEEN '2014-10-31' AND '2014-11-01' "
		+ "  AND arrivalDepartureTime-predictionReadTime < '00:15:00' "
		+ "  AND routeId='CR-Providence' "
		+ "  AND predictionSource='Transitime';";

	try {
	    GenericQuery query = new GenericQuery(dbType, dbHost, dbName,
		    dbUserName, dbPassword);

	    List<GenericResult> results = query.doQuery(sql);
	    
	    String resultsStr = getJsonData(results);
	    System.out.println("Results:\n" + resultsStr);
	    
	} catch (SQLException e) {
	    logger.error("Error processing SQL=\"{}\"", sql, e);
	}
    }

}
