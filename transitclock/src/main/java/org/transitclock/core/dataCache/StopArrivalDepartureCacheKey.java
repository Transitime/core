package org.transitclock.core.dataCache;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
/**
 * @author Sean Og Crudden
 * 
 */
public class StopArrivalDepartureCacheKey implements Serializable {
	
	
	private static final long serialVersionUID = 2466653739981305005L;
	private String stopid;
	private Date date;
	public StopArrivalDepartureCacheKey(String stopid, Date date) {
		super();
		setDate(date);
		this.stopid=stopid;
	}
	public String getStopid() {
		return stopid;
	}

	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		this.date = calendar.getTime();				
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((stopid == null) ? 0 : stopid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StopArrivalDepartureCacheKey other = (StopArrivalDepartureCacheKey) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (stopid == null) {
			if (other.stopid != null)
				return false;
		} else if (!stopid.equals(other.stopid))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "StopArrivalDepartureCacheKey [stopid=" + stopid + ", date=" + date + "]";
	}
	
}
