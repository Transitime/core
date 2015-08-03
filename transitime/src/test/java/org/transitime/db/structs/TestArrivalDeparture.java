/**
 * 
 */
package org.transitime.db.structs;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.ConfigFileReader;;


/**
 * @author SeanOg
 *
 */
public class TestArrivalDeparture {
static String fileName = "testConfig.xml";
	
	private static final Logger logger = LoggerFactory
			.getLogger(TestArrivalDeparture.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {		
		
		ConfigFileReader.processConfig(this.getClass().getClassLoader()
				.getResource(fileName).getPath());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testfindVehicleArrivalDeparture() 
	{	
		Date beginTime=new Date(new Long("1357648269000"));
		
		Date endTime=new Date(new Long("1357649329000"));
		
		String vehicleId="33409";
		
		List<ArrivalDeparture> results = ArrivalDeparture.getArrivalsDeparturesFromDb(beginTime, endTime, vehicleId);
		
		logger.debug("Found : " + results.size() + " results");
		
		for(ArrivalDeparture result:results)
			System.out.println(result);
		
		assert(results.size()>0);	
	}
	@Test
	public void testFindLastStop()
	{
		Long timeToFind=new Long("1357660522000");
				
		Long fuzzySize=new Long("180000");
		
		Long stopDwellTime=new Long("30000");
						
		String vehicleId="33409";
		
		List<ArrivalDeparture> results = ArrivalDeparture.getArrivalsDeparturesFromDb(new Date(timeToFind-fuzzySize), new Date(timeToFind+fuzzySize), vehicleId);
						
		ArrivalDeparture closest=null;
		long closestInMilliseconds=fuzzySize;
		
		for(ArrivalDeparture result:results)
		{
			if(result.isDeparture())
			{
				if(result.getDate().getTime()<timeToFind+stopDwellTime)
				{
					if(closest==null)
					{
						closest=result;
					}
					else if(result.getDate().getTime()-timeToFind<closestInMilliseconds) {
						closestInMilliseconds=result.getDate().getTime()-timeToFind;
						closest=result;
					}
				}
			}
				
		}
		System.out.println("Looking for departure closet to : "+new Date(timeToFind));
		System.out.println(closest);
	}
}
