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

package org.transitime.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Declares that all classes in package com.gontuseries.university will
 * be searched for being a root-resource class with methods annotated
 * with @Path indicating that it handles requests. 
 *
 * Uses "" for the @ApplicationPath so that this class does not actually
 * affect the URI. Instead, the URI is the application name plus what
 * is defined in the root-resource class.
 * 
 * @author SkiBu Smith
 *
 */
@ApplicationPath("")
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
 	// Register all root-resource classes in package that handle @Path 
	// requests
         packages("org.transitime.api.rootResources");   
     }

}