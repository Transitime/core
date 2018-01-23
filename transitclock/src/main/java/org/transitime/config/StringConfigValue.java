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

import java.util.List;

/**
 * For specifying a String parameter that can be read in from xml config file.
 * 
 * @author SkiBu Smith
 */
public class StringConfigValue extends ConfigValue<String> {
	
	/**
	 * Constructor. For when there is no default value. Error will occur when
	 * parameter is initialized if it is not configured.
	 * 
	 * @param id
	 * @param description
	 */
	public StringConfigValue(String id, String description) {
		super(id, description);
	}
	
	/**
	 * Constructor for when there is a default value.
	 * 
	 * @param id
	 * @param defaultValue
	 *            Default value, but can be set to null for if null is a
	 *            reasonable default.
	 * @param description
	 */
	public StringConfigValue(String id, String defaultValue, 
			String description) {
		super(id, defaultValue, description);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param defaultValue
	 * @param description
	 * @param okToLogValue
	 *            If false then won't log value in log file. This is useful for
	 *            passwords and such.
	 */
	public StringConfigValue(String id, String defaultValue, 
			String description, boolean okToLogValue) {
		super(id, defaultValue, description, okToLogValue);
	}

	@Override 
	protected String convertFromString(List<String> dataList) {
		return dataList.get(0);
	}
	
	/**
	 * So that can use the StringConfigValue directly as a String without
	 * explicitly calling getValue().
	 */
	public String toString() {
		return getValue();
	}
	
}
