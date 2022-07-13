package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.StopPath;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.transitclock.utils.CsvDataConveterUtil.*;

public class StopPathsCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public StopPathsCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<StopPath> getAll() throws IOException, ParseException {

        List<StopPath> stopPaths = new ArrayList<>();

        InputStream inputStream = StopPathsCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            int configRev = Integer.parseInt(record.get("configRev"));
            String stopPathId = record.get("stopPathId");
            String stopId = record.get("stopId");
            String tripPatternId = record.get("tripPatternId");
            int gtfsStopSeq = getInteger(record.get("gtfsStopSeq"));
            boolean lastStopInTrip = getBoolean(record.get("lastStopInTrip"));
            String routeId = record.get("routeId");
            boolean layoverStop = getBoolean(record.get("layoverStop"));
            boolean waitStop = getBoolean(record.get("waitStop"));
            boolean scheduleAdherenceStop = getBoolean(record.get("scheduleAdherenceStop"));
            Integer breakTime = getInteger(record.get("breakTime"));
            Double maxDistance = getDouble(record.get("maxDistance"));
            Double maxSpeed = getDouble(record.get("maxSpeed"));

            StopPath stopPath = new StopPath(configRev, stopPathId, stopId, gtfsStopSeq, lastStopInTrip, routeId,
                    layoverStop, waitStop, scheduleAdherenceStop, breakTime, maxDistance, maxSpeed);
            stopPath.setTripPatternId(tripPatternId);
            stopPaths.add(stopPath);
        }

        return stopPaths;
    }
}
