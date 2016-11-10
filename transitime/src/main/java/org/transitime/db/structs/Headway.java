package org.transitime.db.structs;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;

public class Headway implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4561111910398287801L;

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	// The revision of the configuration data that was being used
	@Column 
	private final int configRev;
	
	@Column		
	private final long headway;	

	// The time the AVL data was processed and the headway was created.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date creationTime;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String vehicleId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;
	
	public Headway(long headway, Date creationTime, String vehicleId, String stopId, String tripId,
			String routeId) {
		
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		this.headway = headway;
		this.creationTime = creationTime;
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		this.tripId = tripId;
		this.routeId = routeId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getId() {
		return id;
	}

	public int getConfigRev() {
		return configRev;
	}

	public long getHeadway() {
		return headway;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public String getStopId() {
		return stopId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getRouteId() {
		return routeId;
	}

	

}
