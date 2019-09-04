package org.transitclock.core.dataCache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
/**
 * @author Sean Óg Crudden
 */
public class ArrivalDeparturesToProcessHoldingTimesFor {	
	
	private static ArrivalDeparturesToProcessHoldingTimesFor singleton = new ArrivalDeparturesToProcessHoldingTimesFor();
	
	private static final Logger logger = LoggerFactory
			.getLogger(ArrivalDeparturesToProcessHoldingTimesFor.class);
			
	
	
	
	private final ArrayList<ArrivalDeparture> m = new  ArrayList<ArrivalDeparture>();;
		
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static ArrivalDeparturesToProcessHoldingTimesFor getInstance() {
		return singleton;
	}
	
	
	private ArrivalDeparturesToProcessHoldingTimesFor() {													
	}
	public void empty()
	{
		m.clear();
	}
	public void add(ArrivalDeparture ad)
	{
		m.add(ad);	
	}
	public ArrayList<ArrivalDeparture> getList()
	{		
		return m;
	}
	
}
