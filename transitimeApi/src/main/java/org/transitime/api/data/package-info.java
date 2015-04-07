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

/**
 * This package contains the classes for the objects that are serialized into 
 * JSON or XML. There are some important gotchas with respect to using Jersey
 * that should be noted:
 * <p>
 * <ul>
 * <li>Must have a no-args constructor</li>
 * <li>Each member or getter, but not both, must be labeled with either 
 * @XmlElement or @XmlAttribute.</li>
 * <li>If you specify the order that the members of the class should be
 * serialized then you need to specify every single member. Otherwise you
 * get a cryptic Internal Server error with an further explanation.</li>
 * <li>Can rename each element to give it a consistent, clear, and short
 * name. But don't need to make them too short because will be using 
 * compression when sending the data and long element/attribute names
 * compress really well when they are frequently repeated. 
 * </ul>
 * <p>
 * At first thought that could simply handle lists of objects, such as 
 * ApiVehicle, using a regular list. But then the XML element name cannot
 * be overridden using something like @XmlRootElement(name="vehicle") because
 * will still get <vehicleDatas> for the list instead of <vehicles>. This
 * apparently could be dealt with setting the parameter 
 * FEATURE_XMLROOTELEMENT_PROCESSING to true for Tomcat in the web.xml file,
 * but this seemed a bit cumbersome. Plus handling of lists required
 * special methods in the JsonXml class. So for lists simply use an
 * additional data object that contains the list as a member, as in
 * VehicleListData.
 * 
 * 
 * @author SkiBu Smith
 *
 */
package org.transitime.api.data;
