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
package org.transitime.ipc.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.transitime.db.structs.Calendar;

/**
 * A calendar object for ipc via RMI
 * 
 * @author SkiBu Smith
 *
 */
public class IpcCalendar implements Serializable {

	private final String serviceId;
	private final boolean monday;
	private final boolean tuesday;
	private final boolean wednesday;
	private final boolean thursday;
	private final boolean friday;
	private final boolean saturday;
	private final boolean sunday;
	private final String startDate;
	private final String endDate;
	
	private static final long serialVersionUID = 7248540190574905163L;
	private static final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
	
	public IpcCalendar(Calendar calendar) {
		super();
		this.serviceId = calendar.getServiceId();
		this.monday = calendar.getMonday();
		this.tuesday = calendar.getTuesday();
		this.wednesday = calendar.getWednesday();
		this.thursday = calendar.getThursday();
		this.friday = calendar.getFriday();
		this.saturday = calendar.getSaturday();
		this.sunday = calendar.getSunday();
		this.startDate = formatter.format(calendar.getStartDate());
		this.endDate = formatter.format(calendar.getEndDate());
	}

	@Override
	public String toString() {
		return "IpcCalendar [" 
				+ "serviceId=" + serviceId 
				+ ", monday=" + monday
				+ ", tuesday=" + tuesday 
				+ ", wednesday=" + wednesday
				+ ", thursday=" + thursday 
				+ ", friday=" + friday
				+ ", saturday=" + saturday 
				+ ", sunday=" + sunday
				+ ", startDate=" + startDate 
				+ ", endDate=" + endDate 
				+ "]";
	}

	public String getServiceId() {
		return serviceId;
	}

	public boolean isMonday() {
		return monday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public boolean isFriday() {
		return friday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public boolean isSunday() {
		return sunday;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

}
