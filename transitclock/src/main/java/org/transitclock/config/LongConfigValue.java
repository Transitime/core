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
 * For specifying a Long parameter that can be read in from xml config file.
 * 
 * @author SkiBu Smith
 */
public class LongConfigValue extends ConfigValue<Long> {
	
	/**
	 * Constructor. For when no default value.
	 * 
	 * @param id
	 * @param description
	 */
	public LongConfigValue(String id, String description) {
		super(id, description);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param defaultValue
	 * @param description
	 */
	public LongConfigValue(String id, Long defaultValue, String description) {
		super(id, defaultValue, description);
	}
	
	@Override 
	protected Long convertFromString(List<String> dataList) {
		return Long.valueOf(dataList.get(0).trim());
	}	
}

