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

package org.transitime.db.webstructs;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;
import org.transitime.db.hibernate.HibernateUtils;

/**
 * Database class for storing keys and related info for the API.
 *
 * @author SkiBu Smith
 *
 */
@Entity
@DynamicUpdate
@Table(name = "ApiKeys")
public class ApiKey implements Serializable {

	@Column
	@Id
	private final String applicationName;

	@Column
	private final String key;

	@Column
	private final String applicationUrl;

	@Column
	private final String email;

	@Column
	private final String phone;

	@Column
	private final String description;

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = 903194461306815545L;

	public static final Logger logger = LoggerFactory.getLogger(ApiKey.class);

	/********************** Member Functions **************************/

	/**
	 * For creating object to be written to db.
	 * 
	 * @param key
	 */
	public ApiKey(String applicationName, String key, String applicationUrl,
			String email, String phone, String description) {
		this.applicationName = applicationName;
		this.key = key;
		this.applicationUrl = applicationUrl;
		this.email = email;
		this.phone = phone;
		this.description = description;
	}

	/**
	 * Needed because Hibernate requires no-arg constructor for reading in data
	 */
	@SuppressWarnings("unused")
	private ApiKey() {
		this.applicationName = null;
		this.key = null;
		this.applicationUrl = null;
		this.email = null;
		this.phone = null;
		this.description = null;
	}

	/**
	 * Reads in all keys from database.
	 * 
	 * @param session
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<ApiKey> getApiKeys(Session session)
			throws HibernateException {
		String hql = "FROM ApiKey";
		Query query = session.createQuery(hql);
		return query.list();
	}

	// FIXME taken out because I believe this is only needed if table has composite ID
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((applicationName == null) ? 0 : applicationName.hashCode());
//		result = prime * result
//				+ ((applicationUrl == null) ? 0 : applicationUrl.hashCode());
//		result = prime * result
//				+ ((description == null) ? 0 : description.hashCode());
//		result = prime * result + ((email == null) ? 0 : email.hashCode());
//		result = prime * result + ((key == null) ? 0 : key.hashCode());
//		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ApiKey other = (ApiKey) obj;
//		if (applicationName == null) {
//			if (other.applicationName != null)
//				return false;
//		} else if (!applicationName.equals(other.applicationName))
//			return false;
//		if (applicationUrl == null) {
//			if (other.applicationUrl != null)
//				return false;
//		} else if (!applicationUrl.equals(other.applicationUrl))
//			return false;
//		if (description == null) {
//			if (other.description != null)
//				return false;
//		} else if (!description.equals(other.description))
//			return false;
//		if (email == null) {
//			if (other.email != null)
//				return false;
//		} else if (!email.equals(other.email))
//			return false;
//		if (key == null) {
//			if (other.key != null)
//				return false;
//		} else if (!key.equals(other.key))
//			return false;
//		if (phone == null) {
//			if (other.phone != null)
//				return false;
//		} else if (!phone.equals(other.phone))
//			return false;
//		return true;
//	}

	@Override
	public String toString() {
		return "ApiKey [" 
				+ "applicationName=" + applicationName 
				+ ", key=" + key
				+ ", applicationUrl=" + applicationUrl 
				+ ", email=" + email
				+ ", phone=" + phone 
				+ ", description=" + description 
				+ "]";
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getKey() {
		return key;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getDescription() {
		return description;
	}
}
