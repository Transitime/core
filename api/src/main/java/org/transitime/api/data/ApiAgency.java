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

package org.transitime.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitime.db.structs.Agency;

/**
 * Contains API info for an agency.
 *
 * @author SkiBu Smith
 *
 */
public class ApiAgency {

    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String name;
    
    @XmlAttribute
    private String url;
    
    @XmlAttribute
    private String timezone;
    
    @XmlAttribute
    private String lang;
    
    @XmlAttribute
    private String phone;
    
    @XmlAttribute
    private String fareUrl;
   
    @XmlElement
    private ApiExtent extent;

    @XmlAttribute
    private int configRev;
    
    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiAgency() {}

    public ApiAgency(Agency agency) {
	this.id = agency.getId();
	this.name = agency.getName();
	this.url = agency.getUrl();
	this.timezone = agency.getTimeZoneStr();
	this.lang = agency.getLang();
	this.phone = agency.getPhone();
	this.fareUrl = agency.getFareUrl();
	this.extent = new ApiExtent(agency.getExtent());
	this.configRev = agency.getConfigRev();
    }
}
