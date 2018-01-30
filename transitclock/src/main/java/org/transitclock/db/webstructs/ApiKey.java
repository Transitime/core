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

package org.transitclock.db.webstructs;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

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

	@Column(length=80)
	@Id
	private final String applicationName;

	@Column(length=20)
	private final String applicationKey;

	@Column(length=80)
	private final String applicationUrl;

	@Column(length=80)
	private final String email;

	@Column(length=80)
	private final String phone;

	@Column(length=1000)
	private final String description;

	// Because Hibernate requires objects with composite IDs to be Serializable
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
		this.applicationKey = key;
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
		this.applicationKey = null;
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

	/**
	 * Stores the ApiKey in the database
	 * 
	 * @param dbName name of database
	 */
	public void storeApiKey(String dbName) {
		Session session = HibernateUtils.getSession(dbName);
		try {
		    Transaction transaction = session.beginTransaction();
		    session.save(this);
		    transaction.commit();
		} catch (Exception e) {
		    throw e;
		} finally {
			// Make sure that the session always gets closed, even if 
			// exception occurs
		    session.close();
		}
	}

	/**
	 * Deletes this ApiKey from specified database
	 * 
	 * @param dbName name of database
	 */
	public void deleteApiKey(String dbName) {
		Session session = HibernateUtils.getSession(dbName);
		try {
		    Transaction transaction = session.beginTransaction();
		    session.delete(this);
		    transaction.commit();
		} catch (Exception e) {
		    throw e;
		} finally {
			// Make sure that the session always gets closed, even if 
			// exception occurs
		    session.close();
		}
	}
	
	@Override
	public String toString() {
		return "ApiKey [" 
				+ "applicationName=" + applicationName 
				+ ", key=" + applicationKey
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
		return applicationKey;
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
