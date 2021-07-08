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

package org.transitclock.reports;

import org.transitclock.db.GenericQuery;
import org.transitclock.reports.ChartJsonBuilder.RowBuilder;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * For providing data to a Google scatter chart when need to specify specific
 * SQL for retrieving data from the database. Since any SQL statement can be
 * used this feed can be used for generating a variety of scatter charts.
 *
 * @author SkiBu Smith
 *
 */
public class ChartGenericJsonQuery extends GenericQuery {

	private ChartJsonBuilder jsonBuilder = new ChartJsonBuilder();

	/********************** Member Functions **************************/

	/**
	 * @param agencyId
	 * @throws SQLException
	 */
	public ChartGenericJsonQuery(String agencyId) throws SQLException {
		super(agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.db.GenericQuery#addColumn(java.lang.String, int)
	 */
	@Override
	protected void addColumn(String columnName, int type) {
		if (columnName.equals("tooltip"))
			jsonBuilder.addTooltipColumn();
    else if (type == Types.NUMERIC || type == Types.INTEGER
        || type == Types.SMALLINT || type == Types.BIGINT
        || type == Types.DECIMAL
        || type == Types.FLOAT || type == Types.DOUBLE)
			jsonBuilder.addNumberColumn(columnName);
		else if (type == Types.VARCHAR)
			jsonBuilder.addStringColumn(columnName);
		else
			logger.error("Unknown type={} for columnName={}", type, columnName);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.db.GenericQuery#addRow(java.util.List)
	 */
	@Override
	protected void addRow(List<Object> values) {
		// Start building up the row
		RowBuilder rowBuilder = jsonBuilder.newRow();

		// Add each cell in the row
		for (Object o : values) {
			rowBuilder.addRowElement(o);
		}	
	}
	
	/**
	 * Does SQL query and returns JSON formatted results.
	 * 
	 * @param agencyId
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	public static String getJsonString(String agencyId, String sql, Date... parameters) 
			throws SQLException {
		ChartGenericJsonQuery query = new ChartGenericJsonQuery(agencyId);
						
		query.doQuery(sql, (Object[]) parameters);
		// If query returns empty set then should return null!
		if (query.getNumberOfRows() != 0)			
			return query.jsonBuilder.getJson();
		else
			return null;
	}

	/**
	 * For debugging
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String agencyId = "mbtaAWS";

		String sql = "SELECT "
				+ "     to_char(arrivalDepartureTime-predictionReadTime, 'SSSS')::integer as predLength, "
				// + "     predictionAccuracyMsecs/1000 as predAccuracy, "
				+ "     abs(predictionAccuracyMsecs/1000) as absPredAccuracy, "
				+ "     format(E'Stop=%s tripId=%s\\narrDepTime=%s predTime=%s predReadTime=%s\\nvehicleId=%s source=%s', "
				+ "       stopId, tripId, "
				+ "       to_char(arrivalDepartureTime, 'MM/DD/YYYY HH:MM:SS.MS'),"
				+ "       to_char(predictedTime, 'HH:MM:SS.MS'),"
				+ "       to_char(predictionReadTime, 'HH:MM:SS.MS'),"
				+ "       vehicleId, predictionSource) AS tooltip "
				+ " FROM predictionaccuracy "
				+ "WHERE arrivaldeparturetime BETWEEN '2014-10-31' AND '2014-11-01' "
				+ "  AND arrivalDepartureTime-predictionReadTime < '00:15:00' "
				+ "  AND routeId='CR-Providence' "
				+ "  AND predictionSource='TransitClock';";

		try {
			String str = ChartGenericJsonQuery.getJsonString(agencyId, sql);
			System.out.println(str);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
