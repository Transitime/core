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
 * For specifying an Integer parameter that can be read in from xml config file.
 * 
 * @author SkiBu Smith
 */
public class IntegerConfigValue extends ConfigValue<Integer> {
	
	/**
	 * Constructor for when there is no default value.
	 * 
	 * @param id
	 * @param description
	 */
	public IntegerConfigValue(String id, String description) {
		super(id, description);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param defaultValue
	 */
	public IntegerConfigValue(String id, Integer defaultValue, String description) {
		super(id, defaultValue, description);
	}
	
	@Override 
	protected Integer convertFromString(List<String> dataList) {
		return Integer.valueOf(dataList.get(0).trim());
	}	
}
