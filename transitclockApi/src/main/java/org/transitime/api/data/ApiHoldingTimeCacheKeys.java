package org.transitime.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcHistoricalAverageCacheKey;
import org.transitclock.ipc.data.IpcHoldingTimeCacheKey;
/**
 *
 * @author Sean Óg Crudden
 *
 */
@XmlRootElement(name = "HoldingTimeCacheKeys")
public class ApiHoldingTimeCacheKeys {
	
	@XmlElement(name = "HoldingTimeCacheKey")
	private List<ApiHoldingTimeCacheKey> apiHoldingTimeCacheKeys;
	
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */	
	protected ApiHoldingTimeCacheKeys() {
	
	}
	public ApiHoldingTimeCacheKeys(Collection<IpcHoldingTimeCacheKey> cacheKeys) {
		apiHoldingTimeCacheKeys=new ArrayList<ApiHoldingTimeCacheKey>();
		for( IpcHoldingTimeCacheKey key:cacheKeys)
		{
			apiHoldingTimeCacheKeys.add(new ApiHoldingTimeCacheKey(key) );
		}		
	}
}
