package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.core.reporting.RunTimeLoaderTest;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.transitclock.utils.CsvDataConveterUtil.getInteger;

public class TripScheduleTimesCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public TripScheduleTimesCsvDao(String filePath){
        this.filePath = filePath;
    }

    public Map<String, List<ScheduleTime>> getAllTimesByTrip() throws IOException, ParseException {

        Map<String, List<ScheduleTime>> scheduledTimesByTrip = new LinkedHashMap<>();

        InputStream inputStream = TripScheduleTimesCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            String tripId = record.get("Trip_tripId");
            Integer arrivalTime = getInteger(record.get("arrivalTime"));
            Integer departureTime = getInteger(record.get("departureTime"));

            List<ScheduleTime> scheduleTimesForTrip = scheduledTimesByTrip.get(tripId);
            if(scheduleTimesForTrip == null){
                scheduleTimesForTrip = new ArrayList<>();
                scheduledTimesByTrip.put(tripId, scheduleTimesForTrip);
            }
            scheduleTimesForTrip.add(new ScheduleTime(arrivalTime, departureTime));

        }

        return scheduledTimesByTrip;
    }
}
