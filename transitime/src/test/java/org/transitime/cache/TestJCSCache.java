package org.transitime.cache;

import org.transitime.core.dataCache.ErrorCache;
import org.transitime.core.dataCache.ErrorCacheFactory;
import org.transitime.core.dataCache.KalmanErrorCacheKey;

import junit.framework.TestCase;

public class TestJCSCache extends TestCase{
	public void testPersist()
	{		
		for(int i=0;i<10;i++)
		{
			ErrorCache cache=ErrorCacheFactory.getInstance();
			
			Double value=new Double(i);
			
			KalmanErrorCacheKey key=new KalmanErrorCacheKey(""+i,i);
					
			cache.putErrorValue(key, value);
							
			value=cache.getErrorValue(key);
			
			if(value.intValue()!=i)
				assertTrue(false);		
		}
		assertTrue(true);		
	
	}
}
