package org.transitime.api.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcPredictionForStopPath;
@XmlRootElement(name = "traveltimepredictionforstoppath")
public class ApiPredictionForStopPath {
	
	@XmlAttribute
	private String tripId;				
	@XmlAttribute
	private Integer stopPathIndex;
	
	@XmlAttribute
	private Date creationTime;
	
	@XmlAttribute
	private Double predictionTime;
	
	@XmlAttribute
	private String algorithm;
	
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiPredictionForStopPath() {
		
		
	}

	public ApiPredictionForStopPath(IpcPredictionForStopPath prediction
			) {
		super();
		this.tripId = prediction.getTripId();
		this.stopPathIndex = prediction.getStopPathIndex();
		this.creationTime = prediction.getCreationTime();
		this.predictionTime = prediction.getPredictionTime();
		this.algorithm = prediction.getAlgorithm();
	}

}
