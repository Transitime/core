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

package org.transitclock.core;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a map matcher object that matches avl locations to trip shapes 
 *
 * @author Sean Óg Crudden
 *
 */
public class MapMatcherFactory {

    // The name of the class to instantiate
    private static StringConfigValue className =
            new StringConfigValue("transitclock.core.mapMatcherClass",
                    "org.transitclock.core.barefoot.BareFootMapMatcher",
                    "Specifies the name of the class used for map matching.");

    /********************** Member Functions **************************/

    public static MapMatcher getInstance() {


        try {
            return ClassInstantiator.instantiate(className.getValue(),
                    MapMatcher.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }
}