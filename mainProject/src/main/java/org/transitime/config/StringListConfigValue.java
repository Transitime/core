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
 * For specifying a List of Strings parameter that can be read in from xml
 * config file. When parameter set as a command line argument then the
 * ConfigValue.LIST_SEPARATOR is used (":") when need to specify multiple
 * items. 
 * 
 * @author SkiBu Smith
 * 
 */
public class StringListConfigValue extends ConfigValue<List<String>> {
	/**
	 * No default is specified. If value not in config file then error occurs.
	 * @param configValuesList
	 * @param id
	 */
	public StringListConfigValue(String id) {
		super(id, null);
	}
	
	public StringListConfigValue(String id, List<String> defaultValue) {
		super(id, defaultValue);
	}
	
	@Override protected List<String> convertFromString(List<String> dataList) {
		return dataList;
	}

}
