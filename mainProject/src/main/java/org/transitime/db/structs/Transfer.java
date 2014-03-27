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
import org.transitime.gtfs.gtfsStructs.GtfsTransfer;

/**
 * Contains data from the transfers.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="Transfers")
public class Transfer implements Serializable {

	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String fromStopId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String toStopId;
	
	@Column(length=1)
	private final String transferType;
	
	@Column
	private final Integer minTransferTime;

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = 644790964103354772L;

	/********************** Member Functions **************************/

	public Transfer(GtfsTransfer gt) {
		configRev = DbConfig.SANDBOX_REV;
		fromStopId = gt.getFromStopId();
		toStopId = gt.getToStopId();
		transferType = gt.getTransferType();
		minTransferTime = gt.getMinTransferTime();
	}

	/**
	 * Needed because no-arg constructor required by Hibernate
	 */
	@SuppressWarnings("unused")
	private Transfer() {
		configRev = -1;
		fromStopId = null;
		toStopId = null;
		transferType = null;
		minTransferTime = null;
	}
	
	/**
	 * Deletes rev 0 from the Transfers table
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE Transfer WHERE configRev=0";
		int numUpdates = session.createQuery(hql).executeUpdate();
		return numUpdates;
	}

	/**
	 * Returns List of Transfer objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Transfer> getTransfers(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Transfer " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Transfer ["
				+ "configRev=" + configRev 
				+ ", fromStopId=" + fromStopId
				+ ", toStopId=" + toStopId 
				+ ", transferType="	+ transferType 
				+ ", minTransferTime=" + minTransferTime
				+ "]";
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result
				+ ((fromStopId == null) ? 0 : fromStopId.hashCode());
		result = prime * result
				+ ((minTransferTime == null) ? 0 : minTransferTime.hashCode());
		result = prime * result
				+ ((toStopId == null) ? 0 : toStopId.hashCode());
		result = prime * result
				+ ((transferType == null) ? 0 : transferType.hashCode());
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
		Transfer other = (Transfer) obj;
		if (configRev != other.configRev)
			return false;
		if (fromStopId == null) {
			if (other.fromStopId != null)
				return false;
		} else if (!fromStopId.equals(other.fromStopId))
			return false;
		if (minTransferTime == null) {
			if (other.minTransferTime != null)
				return false;
		} else if (!minTransferTime.equals(other.minTransferTime))
			return false;
		if (toStopId == null) {
			if (other.toStopId != null)
				return false;
		} else if (!toStopId.equals(other.toStopId))
			return false;
		if (transferType == null) {
			if (other.transferType != null)
				return false;
		} else if (!transferType.equals(other.transferType))
			return false;
		return true;
	}

	/************************** Getter Methods ***************************/
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the fromStopId
	 */
	public String getFromStopId() {
		return fromStopId;
	}

	/**
	 * @return the toStopId
	 */
	public String getToStopId() {
		return toStopId;
	}

	/**
	 * @return the transferType
	 */
	public String getTransferType() {
		return transferType;
	}

	/**
	 * @return the minTransferTime
	 */
	public Integer getMinTransferTime() {
		return minTransferTime;
	}
	
}
