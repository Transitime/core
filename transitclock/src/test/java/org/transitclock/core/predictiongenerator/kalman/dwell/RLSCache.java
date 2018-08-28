package org.transitclock.core.predictiongenerator.kalman.dwell;

import static org.junit.Assert.*;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.junit.Test;

import junit.framework.TestCase;
import smile.regression.RLS;

public class RLSCache extends TestCase{
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		cache = JCS.getInstance(cacheName);
	}
	final private static String cacheName = "TestDwellTimeModelCache";
	 
	private CacheAccess<String, RLS>  cache = null;
	@Test
	public void test() {
		
		try {
			RLS rls = null;
			String key="1";
			if(cache.get(key)!=null)
			{			
				rls=cache.get(key);
			}else
			{
				double samplex[][]=new double[4][1];
				double sampley[]=new double[4];
				
				samplex[0][0]=0;
				samplex[1][0]=2;
				samplex[2][0]=3;
				samplex[3][0]=4;
				
				sampley[0]=0;
				sampley[1]=2;
				sampley[2]=3;
				sampley[3]=4;
				
				rls=new RLS(samplex, sampley);
			}
			double predict1[]={5};
			double result1 = rls.predict(predict1);
			cache.put(key,rls);
			
			rls=cache.get(key);
			
			double predict2[]={5};
			double result2 = rls.predict(predict2);
			
			// check if RLS works same when read back from cache.
			if(result1!=result2)
			{
				fail();
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
