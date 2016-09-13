package org.transitime.api.rootResources;

import java.util.Date;

import javax.ws.rs.WebApplicationException;

import org.transitime.utils.Time;

public class DateParam {
		  
	  private Date date;

	  public DateParam(String in) throws WebApplicationException {
	    try {
	    	date= Time.parseDate(in);
	  
	    }
	    catch (Exception exception) {
	      throw new WebApplicationException(400);
	    }
	  }
	  public Date getDate() {
	    return date;
	  }
	  public String format() {
	    return date.toString();
	  }
	
	}