package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.core.reporting.RunTimeLoaderTest;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Calendar;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendar;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Long.getLong;
import static org.transitclock.utils.CsvDataConveterUtil.getInteger;
import static org.transitclock.utils.CsvDataConveterUtil.isWeekDayRecord;

public class CalendarsCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CalendarsCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<Calendar> getAll() throws IOException, ParseException {
        List<Calendar> calendars = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        InputStream inputStream = CalendarsCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);


        for (CSVRecord record : records) {
            GtfsCalendar gc = new GtfsCalendar(record, false, null);
            int configRev = getInteger(record.get("configRev"));

            calendars.add(new Calendar(configRev, gc, sdf));
        }

        return calendars;
    }
}
