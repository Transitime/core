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

import java.util.List;

/**
 * For specifying a List of Strings parameter that can be read in from xml
 * config file. When parameter set as a command line argument then the
 * ConfigValue.LIST_SEPARATOR is used (";") when need to specify multiple
 * items. 
 * 
 * @author SkiBu Smith
 * 
 */
public class StringListConfigValue extends ConfigValue<List<String>> {
	
	/**
	 * When parameter set as a command line argument then the
	 * ConfigValue.LIST_SEPARATOR is used (";") when need to specify multiple
	 * items.
	 * 
	 * @param id
	 * @param description
	 */
	public StringListConfigValue(String id, String description) {
		super(id, description);
	}

	/**
	 * When parameter set as a command line argument then the
	 * ConfigValue.LIST_SEPARATOR is used (";") when need to specify multiple
	 * items.
	 * 
	 * @param id
	 * @param defaultValue
	 * @param description
	 */
	public StringListConfigValue(String id, List<String> defaultValue,
			String description) {
		super(id, defaultValue, description);
	}
	
	@Override 
	protected List<String> convertFromString(List<String> dataList) {
		return dataList;
	}

}
