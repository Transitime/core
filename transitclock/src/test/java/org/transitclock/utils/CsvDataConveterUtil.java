package org.transitclock.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class CsvDataConveterUtil {
    private static Calendar cal = Calendar.getInstance();

    public static Long getLong(String longVal){
        try {
            return Long.parseLong(longVal);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public static Integer getInteger(String integerVal){
        try {
            return Integer.parseInt(integerVal);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public static Double getDouble(String doubleVal){
        try {
            return Double.parseDouble(doubleVal);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public static Boolean getBoolean(String booleanVal){
        try {
            return Boolean.parseBoolean(booleanVal);
        } catch (NullPointerException e){
            return null;
        }
    }

    public static Date getDate(String dateVal, DateFormat dateFormat){
        try {
            return dateFormat.parse(dateVal);
        } catch (NullPointerException | ParseException e){
            return null;
        }
    }

    public static int getDayOfWeek(Date date){
        synchronized (cal){
            cal.setTime(date);
            return cal.get(Calendar.DAY_OF_WEEK);
        }
    }

    public static boolean isWeekDayRecord(Date time){
        int dayOfWeek = getDayOfWeek(time);
        if(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY){
            return false;
        }
        return true;
    }
}
