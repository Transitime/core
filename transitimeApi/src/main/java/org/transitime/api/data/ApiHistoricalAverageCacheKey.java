package org.transitime.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcHistoricalAverageCacheKey;
/**
 * Describes an historical average key which is used to refer to data elements in the cache
 *
 * @author Sean Og Crudden
 *
 */
@XmlRootElement(name = "HistoricalAverageCacheKey")
public class ApiHistoricalAverageCacheKey {
	
	@XmlAttribute
	private String tripId;
	@XmlAttribute
	private Integer stopPathIndex;
	
	public ApiHistoricalAverageCacheKey() {
	
	}
	public ApiHistoricalAverageCacheKey(IpcHistoricalAverageCacheKey key) {
		
		this.tripId=key.getTripId();
		this.stopPathIndex=key.getStopPathIndex();
	}
}
