package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.transitclock.db.structs.CalendarDate;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendarDate;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.transitclock.utils.CsvDataConveterUtil.getInteger;

public class CalendarDatesCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CalendarDatesCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<CalendarDate> getAll() throws IOException, ParseException {
        List<CalendarDate> calendarDates = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        InputStream inputStream = CalendarDatesCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);


        for (CSVRecord record : records) {
            GtfsCalendarDate gcd = new GtfsCalendarDate(record, false, null);
            int configRev = getInteger(record.get("configRev"));

            calendarDates.add(new CalendarDate(configRev, gcd, sdf));
        }

        return calendarDates;
    }
}
