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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.transitime.db.GenericQuery;

/**
 * For web server. Allows a query on an agency db to be easily run.
 * 
 * @author Michael Smith
 *
 */
public class GenericJsonQuery extends GenericQuery {

	private StringBuilder strBuilder = new StringBuilder();
	private List<String> columnNames = new ArrayList<String>();
	private boolean firstRow = true;
	
	/**
	 * @param agencyId
	 * @throws SQLException
	 */
	private GenericJsonQuery(String agencyId) throws SQLException {
		super(agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.db.GenericQuery#addColumn(java.lang.String, int)
	 */
	@Override
	protected void addColumn(String columnName, int type) {
		// Keep track of names of all columns
		columnNames.add(columnName);
	}

	private void addRowElement(int i, double value) {
		strBuilder.append(value);
	}
	
	private void addRowElement(int i, long value) {
		strBuilder.append(value);
	}
	
	private void addRowElement(int i, boolean value) {
		strBuilder.append(value);
	}
	
	private void addRowElement(int i, String value) {
		strBuilder.append("\"").append(value).append("\"");
	}
	
	private void addRowElement(int i, Timestamp value) {
		strBuilder.append("\"").append(value).append("\"");
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.db.GenericQuery#addRow(java.util.List)
	 */
	@Override
	protected void addRow(List<Object> values) {
		if (!firstRow)			
			strBuilder.append(",\n");
		firstRow = false;
		
		strBuilder.append('{');
		
		// Add each cell in the row
		boolean firstElementInRow = true;
		for (int i=0; i<values.size(); ++i) {
			Object o = values.get(i);
			// Can't output null attributes
			if (o == null)
				continue;
			
			if (!firstElementInRow)
				strBuilder.append(",");
			firstElementInRow = false;
			
			// Output name of attribute
			strBuilder.append("\"").append(columnNames.get(i)).append("\":");
			
			// Output value of attribute
			if (o instanceof BigDecimal || o instanceof Double || o instanceof Float) {
				addRowElement(i, ((Number) o).doubleValue());
			} else if (o instanceof Number) {
				addRowElement(i, ((Number) o).longValue());
			} else if (o instanceof String) {
				addRowElement(i, (String) o);
			} else if (o instanceof Timestamp) {
				addRowElement(i, ((Timestamp) o));
			} else if (o instanceof Boolean) {
				addRowElement(i, ((Boolean) o));
			}
		}
		
		strBuilder.append('}');
	}
	/**
	 * Does SQL query and returns JSON formatted results.
	 * 
	 * @param agencyId
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	public static String getJsonString(String agencyId, String sql, Object...parameters) {
		// Add the rows from the query to the JSON string
		try {
			GenericJsonQuery query = new GenericJsonQuery(agencyId);
			
			// Start the JSON
			query.strBuilder.append("{\"data\": [\n");

			query.doQuery(sql, parameters);

			// Finish up the JSON
			query.strBuilder.append("]}");
						
			return query.strBuilder.toString();
		} catch (SQLException e) {
			return e.getMessage();
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
	public static String getJsonString(String agencyId, String sql) {
		// Add the rows from the query to the JSON string
		try {
			GenericJsonQuery query = new GenericJsonQuery(agencyId);
			
			// Start the JSON
			query.strBuilder.append("{\"data\": [\n");

			query.doQuery(sql);

			// Finish up the JSON
			query.strBuilder.append("]}");
						
			return query.strBuilder.toString();
		} catch (SQLException e) {
			return e.getMessage();
		}
	}

	public static void main(String[] args) {
		String agencyId = "sfmta";
		
		String sql = "SELECT * FROM avlreports ORDER BY time DESC LIMIT 5";
		String str = GenericJsonQuery.getJsonString(agencyId, sql);
		System.out.println(str);
	}
}
