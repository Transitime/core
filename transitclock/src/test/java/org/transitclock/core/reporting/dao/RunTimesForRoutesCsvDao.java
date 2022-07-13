package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.core.ServiceType;
import org.transitclock.db.structs.RunTimesForRoutes;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.transitclock.utils.CsvDataConveterUtil.getInteger;
import static org.transitclock.utils.CsvDataConveterUtil.getLong;

public class RunTimesForRoutesCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public RunTimesForRoutesCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<RunTimesForRoutes> getAll(ServiceType serviceType) throws IOException, ParseException {

        List<RunTimesForRoutes> runTimesForRoutes = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        InputStream inputStream = RunTimesForRoutesCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            int configRev = Integer.parseInt(record.get("configRev"));
            String vehicleId = record.get("vehicleId");
            String tripId = record.get("tripId");
            String serviceId = record.get("serviceId");
            String directionId = record.get("directionId");
            String routeShortName = record.get("routeShortName");
            String tripPatternId = record.get("tripPatternId");
            String headsign = record.get("headsign");
            Date startTime = sdf.parse(record.get("startTime"));
            Date endTime = sdf.parse(record.get("endTime"));
            Integer scheduledStartTime = getInteger(record.get("scheduledStartTime"));
            Integer scheduledEndTime = getInteger(record.get("scheduledEndTime"));
            Integer nextTripStartTime = getInteger(record.get("nextTripStartTime"));
            serviceType = serviceType == null ? null : ServiceType.valueOf(record.get("serviceType"));
            Long dwellTime = getLong(record.get("dwellTime"));
            Integer startStopIndex = getInteger(record.get("startStopPathIndex"));
            Integer expectedLastStopPathIndex = getInteger(record.get("expectedLastStopPathIndex"));
            Integer actualLastStopPathIndex = getInteger(record.get("actualLastStopPathIndex"));

            runTimesForRoutes.add(new RunTimesForRoutes(configRev, serviceId, directionId,
                    routeShortName, tripPatternId, tripId, headsign, startTime, endTime, scheduledStartTime,
                    scheduledEndTime, nextTripStartTime, vehicleId, serviceType, dwellTime, startStopIndex,
                    expectedLastStopPathIndex, actualLastStopPathIndex));
        }

        return runTimesForRoutes;
    }
}
