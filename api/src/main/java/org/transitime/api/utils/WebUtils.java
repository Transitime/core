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

package org.transitime.api.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Utilities for web based API.
 *
 * @author SkiBu Smith
 *
 */
public class WebUtils {
    
   /**
    * Convenience method for when need to throw a BAD_REQUEST exception
    * response.
    * 
    * @param s
    *            Message to be provided as part of the response.
    * @return Exception to be thrown
    */
   public static WebApplicationException badRequestException(String s) {
	return new WebApplicationException(
	        Response
	          .status(Status.BAD_REQUEST)
	          .entity(s)
	          .build());
   }


}
