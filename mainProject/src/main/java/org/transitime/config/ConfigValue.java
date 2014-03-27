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
package org.transitime.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for storing a single param. These params are read from an
 * XML file at both startup and when data needs to be reread. Goal is to make
 * the accessing of the data fast, since it might be accessed in loops. Yet
 * also want to be able to reread the data when needed. Also works when need
 * a list of data.
 * 
 * @author SkiBu Smith
 * 
 */
public abstract class ConfigValue<T> {
	// Name of the param. Also used as Java property name (e.g. -Dtransitime.limit 2)
	protected final String id;
	
	// Value to use if not specified in config file
	protected final T defaultValue;
	
	// The current value
	protected volatile T value;

	// Separator used for when a parameter specified by a Java property
	// has multiple elements.
	private static final String LIST_SEPARATOR = ";";
	
	// Use the main Config logger since don't to have two separate ones.
	private static final Logger logger= LoggerFactory.getLogger(Config.class);
	
	/**
	 * An exception for when a parameter is being read in
	 * @author SkiBu Smith
	 *
	 */
	public static class ConfigParamException extends Exception {
		// Needed since subclass Exception is serializable. Otherwise get warning. 
		private static final long serialVersionUID = 1L;

		private ConfigParamException(String msg) {
			super(msg);
		}
	}

	/**
	 * Stores info associated with ConfigValue. Outputs error message if
	 * id param null or already used.
	 * 
	 * @param id
	 * @param defaultValue
	 */
	public ConfigValue(String id, T defaultValue) {
		// Store params
		this.id = id;
		this.defaultValue = defaultValue;
		
		ArrayList<ConfigValue<?>> configValuesList = Config.getConfigValuesList();

		// Make sure params ok. Can't throw an exception in constructor because
		// constructor called statically and it would be difficult to handle properly.
		// So just output error messages if there is a problem.
		if (id == null) {
			logger.error("Using a null id when creating a ConfigValue.", new Exception());
			return;
		}
		// FIXME I think need to remove this check since could be rereading the
		// config files so should expect to encounter params more once.
		if (configValuesList.contains(id)) {
			logger.error("For config parameter id \"{}\" is used more than once.", id);
			return;
		}
		
		// Add this new param to the config values list so have a record of all params
		configValuesList.add(this);
		
		// Determine the value of this parameter
		try {
			readValue(Config.getConfigFileData());
		} catch (ConfigParamException e) {
			logger.error("Exception when reading in parameter {}", id, e);
		}
	}

	/**
	 * Gets the value. Intended to be fast because might be used in loops. 
	 * Therefore there is no locking.
	 * @return the current value of the param.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Returns the ID of the parameter. This can be useful for outputting as
	 * part of error messages so that user can see what parameter needs to
	 * be modified when there is a problem.
	 * @return
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Values need to be converted from string to the value. This is done in
	 * different ways depending on the type (Integer, Float, String, etc).
	 * Therefore this method needs to be abstract and the real work is done in
	 * the appropriate superclass.
	 * 
	 * @param dataStr
	 */
	abstract protected T convertFromString(List<String> dataStr);

	/**
	 * Reads value from the config file data and stores it
	 * @param configData HashMap of data read in from config file. The
	 * key is the name of the param. The value is a List so that
	 * can better handle params that have multiple elements. If null
	 * then default values will be used.
	 */
	public void readValue(HashMap<String, List<String>> configData) 
			throws ConfigParamException {
		List<String> dataList = null;
		
		// System properties override config files or defaults
		String systemPropertyStr = System.getProperty(id);
		if (systemPropertyStr != null) {
			String dataArray[] = systemPropertyStr.split(LIST_SEPARATOR);
			dataList = Arrays.asList(dataArray);
		}
		
		// If system property not set then see if set via configData
		if (dataList == null && configData != null) {
			// Get the string data from the config file
			dataList = configData.get(id);
		}
		
		// If string data exists then convert it to proper type.
		if (dataList != null) {
			try {
				value = convertFromString(dataList);
				// Got a good value from config file so can return
				return;
			} catch (NumberFormatException e) {
				// There was a problem converting. 
				// Determine className for error message. Want to just show Integer 
				// or Float but couldn't get something to work such as T.class.getName().
				// So getting class name such as IntegerConfigValue.
				String className = this.getClass().getSimpleName(); 
				logger.error("Exception " + e.getMessage() + 
						" occurred when converting parameter \"" + id + "\"" +
						" value of " + (dataList.size() == 1 ? dataList.get(0) : dataList) + 
						" to a " + className);
			}
		} 
	
		// Param not properly defined in config file so use default.	
		// If default value not defined then throw an error
		if (defaultValue == null) {
			String msg = "Error occurred when reading parameter \"" + id + "\"" +
					". No valid value was set in config file and no default was specified.";
			logger.error(msg);
			throw new ConfigParamException(msg);
		}
		
		// Use the default value.
		value = defaultValue;
	}

	/**
	 * So that the value is returned instead of the default toString() which
	 * returns the object ID. This makes debugging easier.
	 */
	@Override
	public String toString() {
		return value.toString();
	}

}
