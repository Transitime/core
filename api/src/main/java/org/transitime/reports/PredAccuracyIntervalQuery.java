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
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.statistics.Statistics;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class PredAccuracyIntervalQuery extends PredictionAccuracyQuery {

    // If fewer than this many datapoints for a prediction bucket then 
    // stats are not provided since really can't determine standard
    // deviation or percentages for such situation.
    private static final int MIN_DATA_POINTS_PER_PRED_BUCKET = 5;
    
    private static final Logger logger = LoggerFactory
	    .getLogger(PredAccuracyIntervalQuery.class);

    /********************** Member Functions **************************/

    /**
     * Creates connection to database.
     * 
     * @param dbType
     * @param dbHost
     * @param dbName
     * @param dbUserName
     * @param dbPassword
     * @throws SQLException
     */
    public PredAccuracyIntervalQuery(String dbType, String dbHost,
	    String dbName, String dbUserName, String dbPassword)
	    throws SQLException {
	super(dbType, dbHost, dbName, dbUserName, dbPassword);
    }
    
    /**
     * Goes through data array (must already be sorted!) and determines the
     * index of the array that corresponds to the minimum element. For example,
     * if the fraction is specified as 0.70 which means that want to know the
     * minimum value such that 70% of the predictions are between the min and
     * the max, then will return the index for the item whose index is at
     * (100%-70%)/2 = 15% in the array.
     * 
     * @param data
     *            Sorted list of data
     * @param percentage
     *            The percentage (0.0 - 100.0%) of prediction accuracy data that
     *            should be between the min and the max
     * @return Value of the desired element or null if fraction not valid
     */
    private Long getMin(List<Integer> data, double percentage) {
	if (percentage == 0.0 || Double.isNaN(percentage))
	    return null;
	
	double fraction = percentage / 100.0;
	
	int index = (int) (data.size() * (1-fraction) / 2);
	return (long) data.get(index);    
    }
    
    /**
     * Goes through data array (must already be sorted!) and determines the
     * index of the array that corresponds to the maximum element. For example,
     * if the fraction is specified as 0.70 which means that want to know the
     * minimum value such that 70% of the predictions are between the min and
     * the max, then will return the index for the item whose index is at
     * 85% in the array.
     * 
     * @param data
     *            Sorted list of data
     * @param percentage
     *            The percentage (0.0 - 100.0%) of prediction accuracy data that
     *            should be between the min and the max
     * @return Value of the desired element or null if fraction not valid
     */
    private Long getMax(List<Integer> data, double percentage) {
	if (percentage == 0.0 || Double.isNaN(percentage))
	    return null;
	
	double fraction = percentage / 100.0;

	int index = (int) (data.size() * (fraction + (1-fraction) / 2));
	return (long) data.get(index);    
    }
    
    /**
     * Gets the column definition in JSON string format so that chart the data
     * using Google charts. The column definition describes the contents of each
     * column but doesn't actually contain the data itself.
     * 
     * @param intervalsType
     * @param intervalPercentage1
     * @param intervalPercentage2
     * @return The column portion of the JSON string
     */
    @Override
    protected String getCols(IntervalsType intervalsType,
	    double intervalPercentage1, double intervalPercentage2) {
	if (map.isEmpty())
	    return null;
	
	// Start column definition
	StringBuilder result = new StringBuilder();
	result.append("\n  \"cols\": [");

	// Column for x axis, which is prediction length bucket
	result.append("\n    {\"type\": \"number\"}");

	for (String source : map.keySet()) {
	    // The average result for the source
	    result.append(",\n    {\"type\": \"number\", \"label\":\"" + source + "\"}");
	    
	    // The first interval
	    result.append(",\n    {\"type\": \"number\", \"p\":{\"role\":\"interval\"} }");
	    result.append(",\n    {\"type\": \"number\", \"p\":{\"role\":\"interval\"} }");

	    // The second interval. But only want to output it if there actually
	    // should be a second interval. Otherwise if use nulls for the second
	    // interval Google chart doesn't draw even the first interval.
	    if (intervalsType != IntervalsType.PERCENTAGE
		    || (!Double.isNaN(intervalPercentage2) 
			    && intervalPercentage2 != 0.0)) {
		result.append(",\n    {\"type\": \"number\", \"p\":{\"role\":\"interval\"} }");
		result.append(",\n    {\"type\": \"number\", \"p\":{\"role\":\"interval\"} }");
	    }
	}
	
	// Finish up column definition
	result.append("\n  ]");
	
	// Return column definition
	return result.toString();
    }
    
    /**
     * Gets the row definition in JSON string format so that chart the data
     * using Google charts. The row definition contains the actual data.
     * 
     * @param intervalsType
     *            Specifies whether should output for intervals standard
     *            deviation info, percentage info, or both.
     * @param intervalPercentage1
     *            For when outputting intervals as fractions. Not used if
     *            intervalsType is STD_DEV.
     * @param intervalPercentage2
     *            For when outputting intervals as fractions. Only used if
     *            intervalsType is PERCENTAGE.
     * @return The row portion of the JSON string
     */
    @Override
    protected String getRows(IntervalsType intervalsType,
	    double intervalPercentage1, double intervalPercentage2) {
	// If something is really wrong then complain
	if (map.isEmpty()) {
	    logger.error("Called PredictionAccuracyQuery.getRows() but there "
	    	+ "is no data in the map.");
	    return null;
	}
	
	StringBuilder result = new StringBuilder();
	result.append("\n  \"rows\": [");

	// For each prediction length bucket...
	boolean firstRow = true;
	for (int predBucketIdx=0; 
		predBucketIdx<=MAX_PRED_LENGTH/PREDICTION_LENGTH_BUCKET_SIZE; 
		++predBucketIdx) {
	    // Deal with comma for end of previous row
	    if (!firstRow)
		result.append(",");
	    firstRow = false;
	    
	    double horizontalValue = 
		    predBucketIdx * PREDICTION_LENGTH_BUCKET_SIZE/60.0;
	    result.append("\n    {\"c\": [{\"v\": " + horizontalValue + "}");
	    
	    // Add prediction mean and intervals data for each source
	    for (String source : map.keySet()) {
		// Determine mean and standard deviation for this source
		List<List<Integer>> dataForSource = map.get(source);
		List<Integer> listForPredBucket = null;
		if (dataForSource != null && dataForSource.size() > predBucketIdx)
		    listForPredBucket = dataForSource.get(predBucketIdx);
		
		// Sort the prediction accuracy data so that can call
		// getMin() and getMax() using necessary sort list.
		if (listForPredBucket != null)
		    Collections.sort(listForPredBucket);

		// Log some info for debugging
		logger.info("For source {} for prediction bucket minute {} "
			+ "sorted datapoints={}", source, horizontalValue,
			listForPredBucket);
		    
		// If there is enough data then handle stats for this prediction
		// bucket. If there are fewer than 
		// MIN_DATA_POINTS_PER_PRED_BUCKET datapoints for the bucket 
		// then can determine standard deviation and the percentage 
		// based min and max intervals would not be valid either. This
		// would cause an unsightly and inappropriate necking of data
		// for this bucket. 
		if (listForPredBucket != null
			&& listForPredBucket.size() >= MIN_DATA_POINTS_PER_PRED_BUCKET) {
		    // Determine the mean
		    double dataForPredBucket[] = 
			    Statistics.toDoubleArray(listForPredBucket);
		    double mean = Statistics.mean(dataForPredBucket);
		    
		    // Determine the standard deviation and handle special case
		    // of when there is only a single data point such that the
		    // standard deviation is NaN.
		    double stdDev = Statistics.getSampleStandardDeviation(
			    dataForPredBucket, mean);
		    if (Double.isNaN(stdDev))
			stdDev = 0.0;
		    
		    // Output the mean value
		    result.append(",{\"v\": " + Math.round(mean) + "}");
		    
		    // Output the first interval values. Using int instead of 
		    // double because that is enough precision and then the 
		    // tooltip info looks lot better. 
		    Long intervalMin;
		    Long intervalMax;
		    if (intervalsType == IntervalsType.PERCENTAGE) {
			intervalMin = getMin(listForPredBucket, intervalPercentage1);
			intervalMax = getMax(listForPredBucket, intervalPercentage1);
		    } else {
			// Use single standard deviation
			intervalMin = Math.round(mean-stdDev);
		    	intervalMax = Math.round(mean+stdDev);
		    }
		    
		    result.append(",{\"v\": " + intervalMin + "}");
		    result.append(",{\"v\": " + intervalMax + "}");

		    // Output the second interval values
		    if (intervalsType == IntervalsType.PERCENTAGE) {
			// Chart can't seem to handle null values for an interval
			// so if intervalFraction2 is not set then don't put
			// out this interval info.
			if (intervalPercentage2 == 0.0 || Double.isNaN(intervalPercentage2))
				continue;
			
			intervalMin = getMin(listForPredBucket, intervalPercentage2);
			intervalMax = getMax(listForPredBucket, intervalPercentage2);			
		    } else if (intervalsType == IntervalsType.BOTH) {
			// Use percentage but since also displaying results 
			// for a single deviation use a fraction that 
			// corresponds, which is 0.68.
			intervalMin = getMin(listForPredBucket, 0.68);
			intervalMax = getMax(listForPredBucket, 0.68);
		    } else {
			// Using standard deviation for second interval. Use
			// 1.5 standard deviations, which corresponds to 86.6%
			intervalMin = Math.round(mean - 1.5*stdDev);
		    	intervalMax = Math.round(mean + 1.5*stdDev);
		    }
		    result.append(",{\"v\": " + intervalMin + "}");
		    result.append(",{\"v\": " + intervalMax + "}");
		} else {
		    // Handle situation when there isn't enough data in the 
		    // prediction bucket.
		    result.append(",{\"v\": null},{\"v\": null},{\"v\": null}");
		}
	    } // End of for each source
	    
	    // Finish up row
	    result.append("]}");
	}
	// Finish up rows section
	result.append(" \n ]");
	return result.toString();
    }
    
    /**
     * For debugging
     * 
     * @param args
     */
    public static void main(String args[]) {
	String beginDate = "11-03-2014";
	String endDate = "11-06-2014";
	String beginTime = "00:00:00";
	String endTime = "23:59:59";
	String routeIds[] = {"CR-Providence"};
	String source = "Transitime";
	
	String dbType = "postgresql";// "mysql";
	String dbHost = "sfmta.c3zbap9ppyby.us-west-2.rds.amazonaws.com";// "localhost";
	String dbName = "mbta";
	String dbUserName = "transitime";// "root";
	String dbPassword = "transitime";

	try {
	    PredictionAccuracyQuery query = new PredAccuracyIntervalQuery(dbType,
		    dbHost, dbName, dbUserName, dbPassword);
	    String jsonString = query.getJson(beginDate, endDate, beginTime,
		    endTime, routeIds, source, null, IntervalsType.BOTH, 0.68,
		    0.80);
	    System.out.println(jsonString);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
