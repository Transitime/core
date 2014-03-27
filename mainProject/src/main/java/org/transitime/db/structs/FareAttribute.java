/**
 * 
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
package org.transitime.db.structs;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.gtfs.DbConfig;
import org.transitime.gtfs.gtfsStructs.GtfsFareAttribute;

/**
 * Contains data from the fareattributes.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="FareAttributes")
public class FareAttribute implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String fareId;
	
	@Column
	private final float price;
	
	@Column(length=3)
	private final String currencyType;
	
	@Column
	private final String paymentMethod;
	
	@Column
	private final String transfers;
	
	@Column
	private final Integer transferDuration;

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = -7167133655095348572L;

	/********************** Member Functions **************************/

	public FareAttribute(GtfsFareAttribute gf) {
		configRev = DbConfig.SANDBOX_REV;
		fareId = gf.getFareId();
		price = gf.getPrice();
		currencyType = gf.getCurrencyType();
		paymentMethod = gf.getPaymentMethod();
		transfers = gf.getTransfers();
		transferDuration = gf.getTransferDuration();
	}
	
	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private FareAttribute() {
		configRev = 0;
		fareId = null;
		price = Float.NaN;
		currencyType = null;
		paymentMethod = null;
		transfers = null;
		transferDuration = null;		
	}
	
	/**
	 * Deletes rev 0 from the FareAttributes table
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE FareAttribute WHERE configRev=0";
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of FareAttribute objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<FareAttribute> getFareAttributes(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM FareAttribute " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((currencyType == null) ? 0 : currencyType.hashCode());
		result = prime * result + configRev;
		result = prime * result + ((fareId == null) ? 0 : fareId.hashCode());
		result = prime * result
				+ ((paymentMethod == null) ? 0 : paymentMethod.hashCode());
		result = prime * result + Float.floatToIntBits(price);
		result = prime
				* result
				+ ((transferDuration == null) ? 0 : transferDuration
						.hashCode());
		result = prime * result
				+ ((transfers == null) ? 0 : transfers.hashCode());
		return result;
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FareAttribute other = (FareAttribute) obj;
		if (currencyType == null) {
			if (other.currencyType != null)
				return false;
		} else if (!currencyType.equals(other.currencyType))
			return false;
		if (configRev != other.configRev)
			return false;
		if (fareId == null) {
			if (other.fareId != null)
				return false;
		} else if (!fareId.equals(other.fareId))
			return false;
		if (paymentMethod == null) {
			if (other.paymentMethod != null)
				return false;
		} else if (!paymentMethod.equals(other.paymentMethod))
			return false;
		if (Float.floatToIntBits(price) != Float.floatToIntBits(other.price))
			return false;
		if (transferDuration == null) {
			if (other.transferDuration != null)
				return false;
		} else if (!transferDuration.equals(other.transferDuration))
			return false;
		if (transfers == null) {
			if (other.transfers != null)
				return false;
		} else if (!transfers.equals(other.transfers))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FareAttribute ["
				+ "configRev=" + configRev 
				+ ", fareId=" + fareId
				+ ", price=" + price 
				+ ", currencyType=" + currencyType
				+ ", paymentMethod=" + paymentMethod 
				+ ", transfers=" + transfers 
				+ ", transferDuration=" + transferDuration 
				+ "]";
	}

	/************************* Getter Methods **************************/
	
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the fareId
	 */
	public String getFareId() {
		return fareId;
	}

	/**
	 * @return the price
	 */
	public float getPrice() {
		return price;
	}

	/**
	 * @return the currencyType
	 */
	public String getCurrencyType() {
		return currencyType;
	}

	/**
	 * @return the paymentMethod
	 */
	public String getPaymentMethod() {
		return paymentMethod;
	}

	/**
	 * @return the transfers
	 */
	public String getTransfers() {
		return transfers;
	}

	/**
	 * @return the transferDuration
	 */
	public Integer getTransferDuration() {
		return transferDuration;
	}
	
	
}
