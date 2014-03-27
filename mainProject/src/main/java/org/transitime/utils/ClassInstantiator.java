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

package org.transitime.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for instantiating a class by name.
 *
 * @author SkiBu Smith
 *
 */
public class ClassInstantiator {

	private static final Logger logger = 
			LoggerFactory.getLogger(ClassInstantiator.class);

	/********************** Member Functions **************************/

	/**
	 * Instantiates the named class using reflection and a no-arg constructor.
	 * If could not instantiate the class then an error is logged and null is
	 * returned.
	 * 
	 * @param className
	 *            Name of the class to be instantiated
	 * @param clazz
	 *            So can do a cast to make sure the className is for the desired
	 *            class and so can get desired class name for logging errors
	 * @return The instantiated object, or null if it could not be instantiated
	 */
	public static <T> T instantiate(String className, Class<T> clazz) {
		try {
			// Instantiate the object for the specified className
			Class<?> theClass = Class.forName(className);
			Object uncastInstance = theClass.newInstance();
			
			// Make sure the created object is of the proper class. If it is not
			// then a ClassCastException is thrown.
			T instance = clazz.cast(uncastInstance);
			
			// Return the instantiated object
			return instance;
		} catch (ClassCastException e) {
			logger.error("Could not cast {} to a {}", className, clazz.getName(), e);
			return null;
		} catch (ClassNotFoundException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException e) {
			logger.error("Could not instantiate class {}. ", className, e);
			return null;
		}
	}
}
