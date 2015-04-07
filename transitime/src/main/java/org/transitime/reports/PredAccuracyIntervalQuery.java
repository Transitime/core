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
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.reports.ChartJsonBuilder.RowBuilder;
import org.transitime.statistics.Statistics;

/**
 * For doing SQL query and generating JSON data for a prediction accuracy
 * intervals chart.
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
	 * Creates connection to database for the specified agency
	 * 
	 * @param agencyId
	 * @throws SQLException
	 */
	public PredAccuracyIntervalQuery(String agencyId) throws SQLException {
		super(agencyId);
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

		int index = (int) (data.size() * (1 - fraction) / 2);
		return (long) data.get(index);
	}

	/**
	 * Goes through data array (must already be sorted!) and determines the
	 * index of the array that corresponds to the maximum element. For example,
	 * if the fraction is specified as 0.70 which means that want to know the
	 * minimum value such that 70% of the predictions are between the min and
	 * the max, then will return the index for the item whose index is at 85% in
	 * the array.
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

		int index = (int) (data.size() * (fraction + (1 - fraction) / 2));
		return (long) data.get(index);
	}

	/**
	 * Adds the column definition in JSON string format so that chart the data
	 * using Google charts. The column definition describes the contents of each
	 * column but doesn't actually contain the data itself.
	 * 
	 * @param builder
	 * @param intervalsType
	 * @param intervalPercentage1
	 * @param intervalPercentage2
	 */
	private void addCols(ChartJsonBuilder builder, IntervalsType intervalsType,
			double intervalPercentage1, double intervalPercentage2) {
		if (map.isEmpty()) {
			logger.error("Called PredAccuracyIntervalQuery.getCols() but there "
					+ "is no data in the map.");
			return;
		}

		builder.addNumberColumn();
		for (String source : map.keySet()) {
			// The average result for the source
			builder.addNumberColumn(source);

			// The first interval
			builder.addIntervalColumn();
			builder.addIntervalColumn();

			// The second interval. But only want to output it if there actually
			// should be a second interval. Otherwise if use nulls for the
			// second
			// interval Google chart doesn't draw even the first interval.
			if (intervalsType != IntervalsType.PERCENTAGE
					|| (!Double.isNaN(intervalPercentage2) && intervalPercentage2 != 0.0)) {
				builder.addIntervalColumn();
				builder.addIntervalColumn();
			}
		}
	}

	/**
	 * Adds the row definition in JSON string format so that chart the data
	 * using Google charts. The row definition contains the actual data.
	 * 
	 * @param builder
	 * @param intervalsType
	 *            Specifies whether should output for intervals standard
	 *            deviation info, percentage info, or both.
	 * @param intervalPercentage1
	 *            For when outputting intervals as fractions. Not used if
	 *            intervalsType is STD_DEV.
	 * @param intervalPercentage2
	 *            For when outputting intervals as fractions. Only used if
	 *            intervalsType is PERCENTAGE.
	 */
	private void addRows(ChartJsonBuilder builder, IntervalsType intervalsType,
			double intervalPercentage1, double intervalPercentage2) {
		// If something is really wrong then complain
		if (map.isEmpty()) {
			logger.error("Called PredAccuracyIntervalQuery.getRows() but there "
					+ "is no data in the map.");
			return;
		}

		// For each prediction length bucket...
		for (int predBucketIdx = 0; predBucketIdx <= MAX_PRED_LENGTH
				/ PREDICTION_LENGTH_BUCKET_SIZE; ++predBucketIdx) {
			// Start building up the row
			RowBuilder rowBuilder = builder.newRow();
			double predBucketSecs = predBucketIdx
					* PREDICTION_LENGTH_BUCKET_SIZE / 60.0;
			rowBuilder.addRowElement(predBucketSecs);

			// Add prediction mean and intervals data for each source
			for (String source : map.keySet()) {
				// Determine mean and standard deviation for this source
				List<List<Integer>> dataForSource = map.get(source);
				List<Integer> listForPredBucket = null;
				if (dataForSource != null
						&& dataForSource.size() > predBucketIdx)
					listForPredBucket = dataForSource.get(predBucketIdx);

				// Sort the prediction accuracy data so that can call
				// getMin() and getMax() using necessary sort list.
				if (listForPredBucket != null)
					Collections.sort(listForPredBucket);

				// Log some info for debugging
				logger.info("For source {} for prediction bucket minute {} "
						+ "sorted datapoints={}", source, predBucketSecs,
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
					double dataForPredBucket[] = Statistics
							.toDoubleArray(listForPredBucket);
					double mean = Statistics.mean(dataForPredBucket);

					// Determine the standard deviation and handle special case
					// of when there is only a single data point such that the
					// standard deviation is NaN.
					double stdDev = Statistics.getSampleStandardDeviation(
							dataForPredBucket, mean);
					if (Double.isNaN(stdDev))
						stdDev = 0.0;

					// Output the mean value
					rowBuilder.addRowElement(Math.round(mean));

					// Output the first interval values. Using int instead of
					// double because that is enough precision and then the
					// tooltip info looks lot better.
					Long intervalMin;
					Long intervalMax;
					if (intervalsType == IntervalsType.PERCENTAGE) {
						intervalMin = getMin(listForPredBucket,
								intervalPercentage1);
						intervalMax = getMax(listForPredBucket,
								intervalPercentage1);
					} else {
						// Use single standard deviation
						intervalMin = Math.round(mean - stdDev);
						intervalMax = Math.round(mean + stdDev);
					}

					rowBuilder.addRowElement(intervalMin);
					rowBuilder.addRowElement(intervalMax);

					// Output the second interval values
					if (intervalsType == IntervalsType.PERCENTAGE) {
						// Chart can't seem to handle null values for an
						// interval
						// so if intervalFraction2 is not set then don't put
						// out this interval info.
						if (intervalPercentage2 == 0.0
								|| Double.isNaN(intervalPercentage2))
							continue;

						intervalMin = getMin(listForPredBucket,
								intervalPercentage2);
						intervalMax = getMax(listForPredBucket,
								intervalPercentage2);
					} else if (intervalsType == IntervalsType.BOTH) {
						// Use percentage but since also displaying results
						// for a single deviation use a fraction that
						// corresponds, which is 0.68.
						intervalMin = getMin(listForPredBucket, 0.68);
						intervalMax = getMax(listForPredBucket, 0.68);
					} else {
						// Using standard deviation for second interval. Use
						// 1.5 standard deviations, which corresponds to 86.6%
						intervalMin = Math.round(mean - 1.5 * stdDev);
						intervalMax = Math.round(mean + 1.5 * stdDev);
					}
					rowBuilder.addRowElement(intervalMin);
					rowBuilder.addRowElement(intervalMax);
				} else {
					// Handle situation when there isn't enough data in the
					// prediction bucket.
					rowBuilder.addRowNullElement();
					rowBuilder.addRowNullElement();
					rowBuilder.addRowNullElement();
				}
			} // End of for each source
		}
	}

	/**
	 * Performs the query and returns the data in an JSON string so that it can
	 * be used for a chart.
	 *
	 * @param beginDateStr
	 *            Begin date for date range of data to use.
	 * @param endDateStr
	 *            End date for date range of data to use. Since want to include
	 *            data for the end date, 1 day is added to the end date for the
	 *            query.
	 * @param beginTimeStr
	 *            For specifying time of day between the begin and end date to
	 *            use data for. Can thereby specify a date range of a week but
	 *            then just look at data for particular time of day, such as 7am
	 *            to 9am, for those days. Set to null or empty string to use
	 *            data for entire day.
	 * @param endTimeStr
	 *            For specifying time of day between the begin and end date to
	 *            use data for. Can thereby specify a date range of a week but
	 *            then just look at data for particular time of day, such as 7am
	 *            to 9am, for those days. Set to null or empty string to use
	 *            data for entire day.
	 * @param routeIds
	 *            Specifies which routes to do the query for. Can be null for
	 *            all routes or an array of route IDs.
	 * @param predSource
	 *            The source of the predictions. Can be null or "" (for all),
	 *            "Transitime", or "Other"
	 * @param predType
	 *            Whether predictions are affected by wait stop. Can be "" (for
	 *            all), "AffectedByWaitStop", or "NotAffectedByWaitStop".
	 * @param intervalsType
	 *            Specifies whether should output for intervals standard
	 *            deviation info, percentage info, or both.
	 * @param intervalPercentage1
	 *            For when outputting intervals as percentages. Not used if
	 *            intervalsType is STD_DEV.
	 * @param intervalPercentage2
	 *            For when outputting intervals as percentages. Only used if
	 *            intervalsType is PERCENTAGE.
	 * @return the full JSON string contain both columns and rows info, or null
	 *         if no data returned from query
	 * @throws SQLException
	 * @throws ParseException
	 */
	public String getJson(String beginDateStr, String endDateStr,
			String beginTimeStr, String endTimeStr, String routeIds[],
			String predSource, String predType, IntervalsType intervalsType,
			double intervalPercentage1, double intervalPercentage2)
			throws SQLException, ParseException {
		// Actually perform the query
		doQuery(beginDateStr, endDateStr, beginTimeStr, endTimeStr, routeIds,
				predSource, predType);

		// If query returned no data then simply return null so that
		// can easily see that there is a problem
		if (map.isEmpty()) {
			return null;
		}

		ChartJsonBuilder builder = new ChartJsonBuilder();
		addCols(builder, intervalsType, intervalPercentage1,
				intervalPercentage2);
		addRows(builder, intervalsType, intervalPercentage1,
				intervalPercentage2);

		String jsonString = builder.getJson();
		return jsonString;
	}

	/**
	 * For debugging
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String beginDate = "11-03-2014";
		String endDate = "11-06-2014";
		String beginTime = null;
		String endTime = null;
		String routeIds[] = { "CR-Providence" };
		String source = "Transitime";

		String agencyId = "mbta";

		try {
			PredAccuracyIntervalQuery query = new PredAccuracyIntervalQuery(
					agencyId);
			String jsonString = query.getJson(beginDate, endDate, beginTime,
					endTime, routeIds, source, null, IntervalsType.BOTH, 0.68,
					0.80);
			System.out.println(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
