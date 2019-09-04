package org.transitclock.api.rootResources;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
	public static void main(String[] args) {

		LocalTime midnight = LocalTime.MIDNIGHT;

		DateParam date=new DateParam("2016-09-03");
		LocalDate now = date.getDate(); // 2015-11-19T19:42:19.224

		LocalDateTime todayMidnight = LocalDateTime.of(now, midnight);
		LocalDateTime yesterdatMidnight = todayMidnight.plusDays(-1);

		Date end_date = Date.from(todayMidnight.atZone(ZoneId.systemDefault()).toInstant());
		Date start_date = Date.from(yesterdatMidnight.atZone(ZoneId.systemDefault()).toInstant());

		System.out.println(start_date+ " " +end_date);
	}
}