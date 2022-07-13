package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.core.reporting.RunTimeLoaderTest;
import org.transitclock.db.structs.ArrivalDeparture;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Long.getLong;
import static org.transitclock.utils.CsvDataConveterUtil.*;

public class ArrivalsDeparturesCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ArrivalsDeparturesCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<ArrivalDeparture> getAll() throws IOException, ParseException {
        List<ArrivalDeparture> arrivalDepartures = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        InputStream inputStream = ArrivalsDeparturesCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            int configRev = getInteger(record.get("configRev"));
            String vehicleId = record.get("vehicleId");
            Date time = sdf.parse(record.get("time"));
            Date avlTime = sdf.parse(record.get("avlTime"));
            int tripIndex = getInteger(record.get("tripIndex"));
            int stopPathIndex = getInteger(record.get("stopPathIndex"));
            boolean isArrival = Boolean.parseBoolean(record.get("isArrival"));
            Date freqStartTime = null;
            Long dwellTime = getLong(record.get("dwellTime"));
            String stopPathId = record.get("stopPathId");
            boolean scheduleAdherenceStop = Boolean.parseBoolean(record.get("scheduleAdherenceStop"));
            Date scheduledTime = getDate(record.get("scheduledTime"), sdf);

            if(isWeekDayRecord(time)){
                arrivalDepartures.add(new ArrivalDeparture(configRev, vehicleId, time, avlTime, null, tripIndex,
                        stopPathIndex, isArrival, freqStartTime, dwellTime, stopPathId, scheduleAdherenceStop, scheduledTime));
            }
        }

        return arrivalDepartures;
    }
}
