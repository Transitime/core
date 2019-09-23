package org.transitclock.core.dataCache;

import java.io.Serializable;

public class KalmanError implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6732432800590510672L;

	// This is the error values itself.
	private Double error=Double.NaN;
	
	//This is the number of times it has been updated.
	private Integer updates=null;

	public KalmanError(Double error) {
		super();
		setError(error);
	}

	public KalmanError() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Double getError() {
		return error;
	}

	public void setError(Double error) {
		if(this.error.compareTo(error)!=0)
		{
			this.error = error;
			incrementUpdates();
		}
	}

	public Integer getUpdates() {
		return updates;
	}

	private void incrementUpdates()
	{
		if(this.updates!=null)
			this.updates=this.updates+1;
		else
			this.updates=new Integer(0);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + ((updates == null) ? 0 : updates.hashCode());
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
		KalmanError other = (KalmanError) obj;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (updates == null) {
			if (other.updates != null)
				return false;
		} else if (!updates.equals(other.updates))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "KalmanError [error=" + error + ", updates=" + updates + "]";
	}
			
}
