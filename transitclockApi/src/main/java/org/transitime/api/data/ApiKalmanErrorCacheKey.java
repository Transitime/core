package org.transitime.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcHistoricalAverageCacheKey;
import org.transitime.ipc.data.IpcKalmanErrorCacheKey;
/**
 * Describes an kalman error key which is used to refer to data elements in the kalman error cache
 *
 * @author Sean Og Crudden
 *
 */
@XmlRootElement(name = "KalmanErrorCacheKey")
public class ApiKalmanErrorCacheKey {
	
	@XmlAttribute
	private String tripId;
	@XmlAttribute
	private Integer stopPathIndex;
	
	public ApiKalmanErrorCacheKey() {
	
	}
	public ApiKalmanErrorCacheKey(IpcKalmanErrorCacheKey key) {
		
		this.tripId=key.getTripId();
		this.stopPathIndex=key.getStopPathIndex();
	}
}
