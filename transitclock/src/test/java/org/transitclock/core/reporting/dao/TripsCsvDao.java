package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.core.reporting.RunTimeLoaderTest;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.transitclock.utils.CsvDataConveterUtil.getInteger;

public class TripsCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public TripsCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<Trip> getAll() throws IOException, ParseException {

        List<Trip> trips = new ArrayList<>();

        TitleFormatter titleFormatter = new TitleFormatter("", false);

        InputStream inputStream = TripsCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);


        for (CSVRecord record : records) {
            int configRev = Integer.parseInt(record.get("configRev"));
            String tripId = record.get("tripId");
            String serviceId = record.get("serviceId");
            String directionId = record.get("directionId");
            String routeId = record.get("routeId");
            String routeShortName = record.get("routeShortName");
            String tripPatternId = record.get("tripPattern_id");
            String headsign = record.get("headsign");
            String tripShortName = record.get("tripShortName");
            String blockId = record.get("blockId");
            String shapeId = record.get("shapeId");
            Integer wheelchairAccessible = null;
            Integer bikesAllowed = null;

            GtfsTrip gtfsTrip = new GtfsTrip(routeId, serviceId, tripId, headsign, tripShortName, directionId,
                    blockId, shapeId, wheelchairAccessible, bikesAllowed);

            trips.add(new Trip(configRev, gtfsTrip, routeId, routeShortName, headsign, titleFormatter, tripPatternId));

        }

        return trips;
    }
}
