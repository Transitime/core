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
package org.transitclock.db.structs;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.gtfs.gtfsStructs.GtfsFeedInfo;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Contains data from the transfers.txt GTFS file. This class is
 * for reading/writing that data to the db.
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="FeedInfo")
public class FeedInfo implements Serializable {

	// Because Hibernate requires objects with composite Ids to be Serializable
	private static final long serialVersionUID = 7068954782064701311L;

	@Column
	@Id
	private final int configRev;

	@Column(length=120)
	private final String feedVersion;

	@Column
	@Id
	private final String feedPublisherName;

	@Column(length=512)
	private final String feedPublisherUrl;

	@Column(length=15)
	private final String feedLanguage;

	@Column @Temporal(TemporalType.DATE)
	private final Date feedStartDate;

	@Column @Temporal(TemporalType.DATE)
	private final Date feedEndDate;

	// Logging
	public static final Logger logger =
			LoggerFactory.getLogger(FeedInfo.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 *
	 * @param configRev
	 * @param gtfsFeedInfo
	 */
	public FeedInfo(int configRev, GtfsFeedInfo gtfsFeedInfo, DateFormat dateFormat) {
		this.configRev = configRev;
		this.feedVersion = gtfsFeedInfo.getFeedVersion();
		this.feedPublisherName = gtfsFeedInfo.getFeedPublisherName();
		this.feedPublisherUrl = gtfsFeedInfo.getFeedPublisherUrl();
		this.feedLanguage = gtfsFeedInfo.getFeedLang();


		Date tempStartDate = null;
		if (gtfsFeedInfo.getFeedStartDate() != null){
			try {
				tempStartDate = dateFormat.parse(gtfsFeedInfo.getFeedStartDate());
			} catch (ParseException e) {
				logger.error("Could not parse Feed Start Date \"{}\" from " +
								"line #{} from file {}",
						gtfsFeedInfo.getFeedStartDate(),
						gtfsFeedInfo.getLineNumber(),
						gtfsFeedInfo.getFileName());
			}
		}
		this.feedStartDate = tempStartDate;

		Date tempEndDate= null;
		if(gtfsFeedInfo.getFeedEndDate() != null) {
			try {
				tempEndDate = dateFormat.parse(gtfsFeedInfo.getFeedEndDate());
			} catch (ParseException e) {
				logger.error("Could not parse Feed End Date \"{}\" from " +
								"line #{} from file {}",
						gtfsFeedInfo.getFeedEndDate(),
						gtfsFeedInfo.getLineNumber(),
						gtfsFeedInfo.getFileName());
			}
		}
		this.feedEndDate = tempEndDate;
	}

	/**
	 * Needed because no-arg constructor required by Hibernate
	 */
	@SuppressWarnings("unused")
	private FeedInfo() {
		this.configRev = -1;
		this.feedVersion = null;
		this.feedPublisherName = null;
		this.feedPublisherUrl = null;
		this.feedLanguage = null;
		this.feedStartDate = null;
		this.feedEndDate = null;
	}
	
	/**
	 * Deletes rev 0 from the Transfers table
	 * 
	 * @param session
	 * @param configRev
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromRev(Session session, int configRev) 
			throws HibernateException {
		// Note that hql uses class name, not the table name
		String hql = "DELETE FeedInfo WHERE configRev=" + configRev;
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
	public static List<FeedInfo> getFeedInfo(Session session, int configRev)
			throws HibernateException {
		String hql = "FROM FeedInfo " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	public int getConfigRev() {
		return configRev;
	}

	public String getFeedVersion() {
		return feedVersion;
	}

	public String getFeedPublisherName() {
		return feedPublisherName;
	}

	public String getFeedPublisherUrl() {
		return feedPublisherUrl;
	}

	public String getFeedLanguage() {
		return feedLanguage;
	}

	public Date getFeedStartDate() {
		return feedStartDate;
	}

	public Date getFeedEndDate() {
		return feedEndDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FeedInfo feedInfo = (FeedInfo) o;
		return configRev == feedInfo.configRev &&
				Objects.equals(feedVersion, feedInfo.feedVersion) &&
				feedPublisherName.equals(feedInfo.feedPublisherName) &&
				feedPublisherUrl.equals(feedInfo.feedPublisherUrl) &&
				feedLanguage.equals(feedInfo.feedLanguage) &&
				Objects.equals(feedStartDate, feedInfo.feedStartDate) &&
				Objects.equals(feedEndDate, feedInfo.feedEndDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(configRev, feedVersion, feedPublisherName, feedPublisherUrl, feedLanguage, feedStartDate, feedEndDate);
	}
}
