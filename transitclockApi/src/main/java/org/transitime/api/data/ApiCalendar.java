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

import org.transitime.ipc.data.IpcCalendar;

/**
 * A GTFS calendar
 * 
 * @author SkiBu Smith
 *
 */
public class ApiCalendar {

	@XmlAttribute
	private String serviceId;
	
	@XmlAttribute
	private boolean monday;
	
	@XmlAttribute
	private boolean tuesday;
	
	@XmlAttribute
	private boolean wednesday;
	
	@XmlAttribute
	private boolean thursday;
	
	@XmlAttribute
	private boolean friday;
	
	@XmlAttribute
	private boolean saturday;
	
	@XmlAttribute
	private boolean sunday;
	
	@XmlAttribute
	private String startDate;
	
	@XmlAttribute
	private String endDate;
	
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiCalendar() {
	}

	public ApiCalendar(IpcCalendar ipcCalendar) {
		this.serviceId = ipcCalendar.getServiceId();
		this.monday = ipcCalendar.isMonday();
		this.tuesday = ipcCalendar.isTuesday();
		this.wednesday = ipcCalendar.isWednesday();
		this.thursday = ipcCalendar.isThursday();
		this.friday = ipcCalendar.isFriday();
		this.saturday = ipcCalendar.isSaturday();
		this.sunday = ipcCalendar.isSunday();
		this.startDate = ipcCalendar.getStartDate();
		this.endDate = ipcCalendar.getEndDate();
	}

}
