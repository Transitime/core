package org.transitclock.api.rootResources;

import javax.ws.rs.WebApplicationException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class TimeParam {

	private LocalTime time;

	public TimeParam(String in) throws WebApplicationException {
		try {
			time=LocalTime.parse(in, DateTimeFormatter.ISO_TIME);
		}
		catch (Exception exception) {
			throw new WebApplicationException(400);
		}
	}
	public LocalTime getTime() {
		return time;
	}
	public String format() {
		return time.toString();
	}

}