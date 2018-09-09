package org.transitclock.api.rootResources;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.WebApplicationException;



public class DateTimeParam {
	
	  private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	  
	  private LocalDateTime date;

	  public DateTimeParam(String in) throws WebApplicationException {
	    try {
	    	date=LocalDateTime.parse(in,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	  
	    }
	    catch (Exception exception) {
	      throw new WebApplicationException(400);
	    }
	  }
	  public LocalDateTime getDate() {
	    return date;
	  }
	  public long getTimeStamp() {
		    return date.toEpochSecond(ZoneOffset.UTC)*1000L;
		  }
	  public String format() {
	    return date.toString();
	  }
	  public static void main(String[] args) {
		  
		  			  
		  	DateTimeParam date=new DateTimeParam("2018-02-02T18:02:00");
			LocalDateTime now = date.getDate(); // 2015-11-19T19:42:19.224
						
			
			
			System.out.println(now+ " "+date.getTimeStamp()+ " "+format.format(now.toEpochSecond(ZoneOffset.UTC)*1000L));
	  }
	}