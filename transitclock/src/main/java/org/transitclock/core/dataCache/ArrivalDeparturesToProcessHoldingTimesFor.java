package org.transitclock.core.dataCache;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
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
