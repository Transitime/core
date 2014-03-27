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
 * For specifying a Double parameter that can be read in from xml config file.
 * 
 * @author SkiBu Smith
 *
 */
public class DoubleConfigValue extends ConfigValue<Double> {
	/**
	 * No default is specified. If value not in config file then error occurs.
	 * @param configValuesList
	 * @param id
	 */
	public DoubleConfigValue(String id) {
		super(id, null);
	}
	
	/**
	 * 
	 * @param configValuesList
	 * @param id
	 * @param defaultValue
	 */
	public DoubleConfigValue(String id, Double defaultValue) {
		super(id, defaultValue);
	}
	
	@Override protected Double convertFromString(List<String> dataList) {
		return Double.valueOf(dataList.get(0));
	}	
}

