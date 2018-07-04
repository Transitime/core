package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcKalmanErrorCacheKey;;
/**
*
* @author Sean Og Crudden
*
*/
@XmlRootElement(name = "KalmanErrorCacheKeys")
public class ApiKalmanErrorCacheKeys {
	
	@XmlElement(name = "KalmanErrorCacheKey")
	private List<ApiKalmanErrorCacheKey> apiKalmanErrorCacheKeys;
	
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */	
	protected ApiKalmanErrorCacheKeys() {
	
	}
	public ApiKalmanErrorCacheKeys(Collection<IpcKalmanErrorCacheKey> cacheKeys) {
		apiKalmanErrorCacheKeys=new ArrayList<ApiKalmanErrorCacheKey>();
		for( IpcKalmanErrorCacheKey key:cacheKeys)
		{
			apiKalmanErrorCacheKeys.add(new ApiKalmanErrorCacheKey(key) );
		}		
	}
}
