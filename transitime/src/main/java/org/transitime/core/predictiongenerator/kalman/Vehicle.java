package org.transitime.core.predictiongenerator.kalman;

public class Vehicle {
	private String licence=null;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((licence == null) ? 0 : licence.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vehicle other = (Vehicle) obj;
		if (licence == null) {
			if (other.licence != null)
				return false;
		} else if (!licence.equals(other.licence))
			return false;
		return true;
	}

	/**
	 * @return the licence
	 */
	public String getLicence() {
		return licence;
	}

	public Vehicle(String licence) {
		super();
		this.licence = licence;
	}
	
}
