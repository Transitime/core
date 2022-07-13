package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.core.ServiceType;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.RunTimesForStops;
import org.transitclock.utils.MapKey;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.transitclock.utils.CsvDataConveterUtil.*;

public class RunTimesForStopsCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public RunTimesForStopsCsvDao(String filePath){
        this.filePath = filePath;
    }

    public Map<MapKey, List<RunTimesForStops>> getAllByRunTimesForRoute() throws IOException, ParseException {

        Map<MapKey, List<RunTimesForStops>> runTimesForStopsByRunTimesForRoute = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        InputStream inputStream = RunTimesForStopsCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            int configRev = getInteger(record.get("configRev"));
            String stopPathId = record.get("stopPathId");
            Integer stopPathIndex = getInteger(record.get("stopPathIndex"));
            Date time = getDate(record.get("time"), sdf);
            Date prevStopDepartureTime = getDate(record.get("prevStopDepartureTime"), sdf);
            Integer scheduledTime = getInteger(record.get("scheduledTime"));
            Integer scheduledPrevStopArrivalTime = getInteger(record.get("scheduledPrevStopArrivalTime"));
            Long dwellTime = getLong(record.get("dwellTime"));
            Double speed = getDouble(record.get("speed"));
            Boolean lastStop = getBoolean(record.get("lastStop"));
            Boolean timePoint = getBoolean(record.get("timePoint"));
            String vehicleId = record.get("vehicleId");
            String tripId = record.get("tripId");
            Date startTime = getDate(record.get("startTime"), sdf);

            MapKey key = new MapKey(configRev, vehicleId, tripId, startTime);

            List<RunTimesForStops> runTimesForStops = runTimesForStopsByRunTimesForRoute.get(key);

            if(runTimesForStops == null){
                runTimesForStops = new ArrayList<>();
                runTimesForStopsByRunTimesForRoute.put(key, runTimesForStops);
            }

            runTimesForStops.add(new RunTimesForStops(stopPathId, stopPathIndex, time, prevStopDepartureTime, scheduledTime,
                    scheduledPrevStopArrivalTime, dwellTime, speed, lastStop, timePoint, vehicleId, tripId, startTime, configRev));

        }

        return runTimesForStopsByRunTimesForRoute;
    }
}
