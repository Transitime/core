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
import org.transitime.ipc.data.Route;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class RouteSummaryData {

    @XmlAttribute
    private String id;
    
    @XmlAttribute(name="rShrtNm")
    private String shortName;
    
    @XmlAttribute(name="rName")
    private String name;
    
    /********************** Member Functions **************************/

    protected RouteSummaryData() {}
    
    public RouteSummaryData(Route route) {
	this.id = route.getId();
	this.shortName = route.getShortName();
	this.name = route.getName();
    }

}
