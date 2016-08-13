package org.transitime.api.rootResources;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.WebApplicationException;

public class DateParam {
	  private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	  
	  private LocalDate date;

	  public DateParam(String in) throws WebApplicationException {
	    try {
	    	date=LocalDate.parse(in, DateTimeFormatter.ISO_DATE);
	  
	    }
	    catch (Exception exception) {
	      throw new WebApplicationException(400);
	    }
	  }
	  public LocalDate getDate() {
	    return date;
	  }
	  public String format() {
	    return date.toString();
	  }
	}