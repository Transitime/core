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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.webstructs.WebAgency;

/**
 * For doing a query without using Hibernate. By using regular JDBC and avoiding
 * Hibernate can connect to multiple databases of different types.
 *
 * @author SkiBu Smith
 *
 */
public class GenericQuery {

	private static Connection connection;

	private static final Logger logger = LoggerFactory
			.getLogger(GenericQuery.class);

	public static class GenericResult {
		// One long per column. Using a Long so that can handle longs
		public List<Number> numbers;
		public String text;
	}

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbName
	 * @param dbUserName
	 * @param dbPassword
	 * @throws SQLException
	 */
	public GenericQuery(String dbType, String dbHost, String dbName,
			String dbUserName, String dbPassword) throws SQLException {
		connection = getConnection(dbType, dbHost, dbName, dbUserName,
				dbPassword);
	}

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 * @throws SQLException
	 */
	public GenericQuery(String agencyId) throws SQLException {
		WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
		connection = getConnection(agency.getDbType(), agency.getDbHost(),
				agency.getDbName(), agency.getDbUserName(),
				agency.getDbPassword());
	}
	
	/**
	 * Gets a database connection to be used for the query
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbName
	 * @param dbUserName
	 * @param dbPassword
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection(String dbType, String dbHost,
			String dbName, String dbUserName, String dbPassword)
			throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbUserName);
		connectionProps.put("password", dbPassword);

		// GenericQuery will likely be used by a web server. A web server
		// uses Hibernate to load web server related data and Hibernate
		// will be configured for the type of db being used (postGres or mySQL).
		// But when doing a query on an agency might be using any kind of
		// database. To get a connection the proper driver needs to first be
		// loaded. If the database for the agency happens to be different than
		// that used for the web server then need to load in the driver for
		// the agency database manually by using Class.forName().
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("Could not load in db driver for GenericQuery. {}", 
					e.getMessage());
		}
		
		String url = "jdbc:" + dbType + "://" + dbHost + "/" + dbName;
		conn = DriverManager.getConnection(url, connectionProps);
		return conn;
	}

	/**
	 * Performs the specified generic query. A List of GenericResult objects is
	 * returned. All number columns (integer or float) are placed in
	 * GenericResult.numbers. A string column is assumed to be a tooltip and is
	 * put in GenericResult.text.
	 * 
	 * @param sql
	 * @return List of GenericResult. If no data then returns empty list
	 *         (instead of null)
	 * @throws SQLException
	 */
	public List<GenericResult> doQuery(String sql) throws SQLException {
		List<GenericResult> results = new ArrayList<GenericResult>();
		Statement statement = null;

		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			// One result per row
			while (rs.next()) {
				List<Number> numbers = new ArrayList<Number>();
				String text = null;

				for (int i = 1; i <= metaData.getColumnCount(); ++i) {
					int columnType = metaData.getColumnType(i);
					if (columnType == Types.INTEGER
							|| columnType == Types.SMALLINT
							|| columnType == Types.BIGINT) {
						numbers.add(rs.getLong(i));
					} else if (columnType == Types.DOUBLE
							|| columnType == Types.FLOAT) {
						numbers.add(rs.getDouble(i));
					} else if (columnType == Types.VARCHAR) {
						text = rs.getString(i);
					} else {
						logger.warn("Encountered unknown result type {} "
								+ "for columne {}", columnType, i);
					}
				}

				GenericResult result = new GenericResult();
				result.numbers = numbers;
				result.text = text;
				results.add(result);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (statement != null)
				statement.close();
		}

		return results;
	}

	public String getCsvString(String sql) throws SQLException {
		// For putting in the CSV data
		StringBuilder sb = new StringBuilder();

		Statement statement = null;
		
		try {
			// Do the query
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			
			// Handle the CSV file header
			for (int i = 1; i <= metaData.getColumnCount(); ++i) {
				if (i > 1)
					sb.append(',');
				sb.append(metaData.getColumnLabel(i));
			}
			sb.append('\n');
			
			// Handle each row of data
			while (rs.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); ++i) {
					if (i > 1)
						sb.append(',');
					sb.append(rs.getObject(i));
				}
				sb.append('\n');
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (statement != null)
				statement.close();
		}

		return sb.toString();
	}
	
	/**
	 * For debugging.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String agencyId = "mbta";
		
		String sql = "SELECT * FROM routes WHERE configRev=0;";
		GenericQuery query;
		try {
			query = new GenericQuery(agencyId);
			String str = query.getCsvString(sql);
			System.out.println(str);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
