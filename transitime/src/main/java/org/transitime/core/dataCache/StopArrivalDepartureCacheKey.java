package org.transitime.core.dataCache;

import java.io.Serializable;
import java.util.Date;

public class StopArrivalDepartureCacheKey implements Serializable {
	
	
	private static final long serialVersionUID = 2466653739981305005L;
	private String stopid;
	private Date date;
	public StopArrivalDepartureCacheKey(String stopid, Date date) {
		super();
		this.stopid = stopid;
		this.date = date;
	}
	public String getStopid() {
		return stopid;
	}
	public void setStopid(String stopid) {
		this.stopid = stopid;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
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
