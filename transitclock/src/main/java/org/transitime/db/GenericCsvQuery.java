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

package org.transitime.db;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * For doing an SQL query and returning the results in CVS format.
 *
 * @author SkiBu Smith
 *
 */
public class GenericCsvQuery extends GenericQuery {

	// For putting in the CSV data
	private StringBuilder sb = new StringBuilder();
	
	// So can determine if first column
	private int numColumns = 0;
	
	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 * @throws SQLException
	 */
	private GenericCsvQuery(String agencyId) throws SQLException {
		super(agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.db.GenericQuery#addColumn(java.lang.String, int)
	 */
	@Override
	protected void addColumn(String columnName, int type) {
		if (numColumns++ > 0)
			sb.append(',');
		sb.append(columnName);
	}

	/* (non-Javadoc)
	 * @see org.transitime.db.GenericQuery#doneWithColumns()
	 */
	@Override
	protected void doneWithColumns() {
		sb.append('\n');
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.db.GenericQuery#addRow(java.util.List)
	 */
	@Override
	protected void addRow(List<Object> values) {
		int column = 0;
		for (Object o : values) {
			// Comma separate the cells
			if (column++ > 0)
				sb.append(',');
			
			// Output value as long as it is not null
			if (o != null) {
				// Strings should be escaped but numbers can be output directly
				if (o instanceof String)
					sb.append(StringEscapeUtils.escapeCsv((String) o));
				else
					sb.append(o);
			}
		}
		sb.append('\n');

	}

	/**
	 * Runs a query and returns result as a CSV string.
	 * 
	 * @param agencyId
	 *            For determining which database to access
	 * @param sql
	 *            The SQL to execute
	 * @return CVS string of results from query
	 * @throws SQLException
	 */
	public static String getCsvString(String agencyId, String sql, Object... parameters) throws SQLException {
		GenericCsvQuery query = new GenericCsvQuery(agencyId);
		query.doQuery(sql,parameters);
		return query.sb.toString();
	}

	/**
	 * For debugging.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String agencyId = "mbta";		
		String sql = "SELECT * FROM routes WHERE configRev=0;";

		try {
			String str = GenericCsvQuery.getCsvString(agencyId, sql);
			System.out.println(str);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
