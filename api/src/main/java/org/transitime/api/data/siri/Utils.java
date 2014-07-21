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

package org.transitime.api.data.siri;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class Utils {

    // Defines how times should be output in Siri
    private static final DateFormat siriDateTimeFormat =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    
    // Defines how dates should be output in Siri
    private static final DateFormat siriDateFormat =
		new SimpleDateFormat("yyyy-MM-dd");
    
    /********************** Member Functions **************************/

    public static String formattedTime(long epochTime) {
	return siriDateTimeFormat.format(new Date(epochTime));
    }

    public static String formattedDate(long epockTime) {
	return siriDateFormat.format(new Date(epockTime));
    }
}
