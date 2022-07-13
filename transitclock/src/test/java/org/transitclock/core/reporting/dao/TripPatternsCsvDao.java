package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.Extent;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TripPattern;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static org.transitclock.utils.CsvDataConveterUtil.getDouble;

public class TripPatternsCsvDao {
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public TripPatternsCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<TripPattern> getAllTripPatterns(List<StopPath> stopPaths) throws IOException {

        List<TripPattern> tripPatterns = new ArrayList<>();

        Map<String, List<StopPath>> stopPathsGroupedByTripPattern = getStopPathsGroupedByTripPattern(stopPaths);

        InputStream inputStream = TripPatternsCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            int configRev = Integer.parseInt(record.get("configRev"));
            String tripPatternId = record.get("id");
            String directionId = record.get("directionId");
            String headsign = record.get("headsign");
            String routeId = record.get("routeId");
            String routeShortName = record.get("routeShortName");
            String shapeId = record.get("shapeId");
            double minLat =  getDouble(record.get("minLat"));
            double maxLat =  getDouble(record.get("maxLat"));
            double minLon =  getDouble(record.get("minLon"));
            double maxLon =  getDouble(record.get("maxLon"));
            Extent extent = new Extent(minLat, maxLat, minLon, maxLon);
            List<StopPath> stopPathsForTripPattern = stopPathsGroupedByTripPattern.get(tripPatternId);

            TripPattern tripPattern = new TripPattern(configRev, tripPatternId, directionId, headsign, routeId,
                    routeShortName, shapeId, extent, stopPathsForTripPattern);

            tripPatterns.add(tripPattern);

        }

        return tripPatterns;
    }

    private Map<String, List<StopPath>> getStopPathsGroupedByTripPattern(List<StopPath> stopPaths){
        Map<String, List<StopPath>> stopPathsByTripPattern = new HashMap<>();
        for(StopPath stopPath : stopPaths){
            String tripPatternId = stopPath.getTripPatternId();
            List<StopPath> stopPathsForTripPattern = stopPathsByTripPattern.get(tripPatternId);
            if(stopPathsForTripPattern == null){
                stopPathsForTripPattern = new ArrayList<>();
                stopPathsByTripPattern.put(tripPatternId, stopPathsForTripPattern);
            }
            stopPathsForTripPattern.add(stopPath);
        }
        return stopPathsByTripPattern;
    }
}
