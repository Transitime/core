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
package org.transitclock.config;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.logging.Markers;

/**
 * Abstract class for storing a single param. These params are read from the
 * command line as Java properties or from an XML file at startup. Goal is to
 * make the accessing of the data fast, since it might be accessed in loops. Yet
 * also want to be able to reread the data when needed. Also works when need a
 * list of data.
 * 
 * @author SkiBu Smith
 * 
 */
public abstract class ConfigValue<T> {
	// Name of the param. Also used as Java property name 
	// (e.g. -Dtransitclock.limit=2)
	protected final String id;
	
	// Value to use if not specified in config file. Can be null.
	protected final T defaultValue;
	
	protected final boolean defaultValueConfigured;
	
	// The current value
	protected volatile T value;

	// Description of the parameter that can be used in log files and such
	protected final String description;
	
	// Don't want to log value if a password or such
	protected final boolean okToLogValue;
	
	// Separator used for when a parameter specified by a Java property
	// has multiple elements.
	private static final String LIST_SEPARATOR = ";";
	
	// Use the main Config logger since don't to have two separate ones.
	private static final Logger logger = LoggerFactory.getLogger(ConfigFileReader.class);
	
	/**
	 * An exception for when a parameter is being read in
	 * @author SkiBu Smith
	 *
	 */
	public static class ConfigParamException extends Exception {
		// Needed since subclass Exception is serializable. Otherwise get 
		// warning. 
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
	 * @param description
	 */
	public ConfigValue(String id, String description) {
		// Store params
		this.id = id;
		this.defaultValue = null;
		this.defaultValueConfigured = false;
		this.description = description;
		this.okToLogValue = true;
		commonConstructor();
	}

	/**
	 * Stores info associated with ConfigValue. Outputs error message if
	 * id param null or already used. 
	 * 
	 * @param id
	 * @param defaultValue
	 * @param description
	 */
	public ConfigValue(String id, T defaultValue, String description) {
		// Store params
		this.id = id;
		this.defaultValue = defaultValue;
		this.defaultValueConfigured = true;
		this.description = description;
		this.okToLogValue = true;
		commonConstructor();
	}

	/**
	 * Stores info associated with ConfigValue. Outputs error message if id
	 * param null or already used.
	 * 
	 * @param id
	 * @param defaultValue
	 * @param description
	 * @param okToLogValue
	 *            If false then won't log value in log file. This is useful for
	 *            passwords and such.
	 */
	public ConfigValue(String id, T defaultValue, String description,
			boolean okToLogValue) {
		// Store params
		this.id = id;
		this.defaultValue = defaultValue;
		this.defaultValueConfigured = true;
		this.description = description;
		this.okToLogValue = okToLogValue;
		commonConstructor();
	}

	/**
	 * The common code used for the constructors.
	 */
	private void commonConstructor() {
		// Make sure params ok. Can't throw an exception in constructor because
		// constructor called statically and it would be difficult to handle
		// properly. So just output error messages if there is a problem.
		if (id == null) {
			logger.error("Using a null id when creating a ConfigValue.", 
					new Exception());
			return;
		}
		
		// Determine the value of this parameter
		try {
			readValue();
		} catch (ConfigParamException e) {
			logger.error("Exception when reading in parameter {}", id, e);
		}
		
		// Log the information about the parameter so that users can see what 
		// values are being used. But don't do so if configured to not log the
		// value, which could be important for passwords and such.
		if (okToLogValue) {
			if ((value==null && defaultValue==null) 
					|| value.equals(defaultValue)) {
				logger.info("Config param {}=\"{}\" (the default value). {}", 
						id,	value, description == null ? "" : description);
			} else {
				logger.info("Config param {}=\"{}\" instead of the default " +
						"of \"{}\". {}", 
						id, value,
						defaultValue, description == null ? "" : description);
			}
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
	 * Reads value from the config data and stores it.
	 * Allow this to be public to allow re-interpretation of values for
	 * unit tests.
	 */
	 public void readValue()
			throws ConfigParamException {
		List<String> dataList = null;
		
		// System properties override config files or defaults
		String systemPropertyStr = System.getProperty(id);
		if (systemPropertyStr != null) {
			String dataArray[] = systemPropertyStr.split(LIST_SEPARATOR);
			dataList = Arrays.asList(dataArray);
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
				logger.error("Exception {} occurred when converting parameter \"{}\"" +
						" value of {} to a {}",
						e.getMessage(), 
						id,
						(dataList.size() == 1 ? dataList.get(0) : dataList),
						className);
			}
		} 
	
		// Param not properly defined in config file so use default.	
		// If default value not defined then log an error and send
		// out e-mail since this is a serious problem.
		if (defaultValue == null && !defaultValueConfigured) {
			logger.error(Markers.email(),
					"When reading parameter \"{}\" for agencyId={} no valid "
					+ "value was configured and no default was "
					+ "specified so resulting value is null.", 
					id, AgencyConfig.getAgencyId());
		}
		
		// Use the default value.
		value = defaultValue;
	}

	/**
	 * Returns the description of the parameter.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * So that the value is returned instead of the default toString() which
	 * returns the object ID. This makes debugging easier.
	 */
	@Override
	public String toString() {
		if (value == null)
			return null;
		
		return value.toString();
	}

}
