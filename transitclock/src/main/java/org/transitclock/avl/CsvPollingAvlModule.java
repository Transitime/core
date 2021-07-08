package org.transitclock.avl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Map arbitrary CSV columns to an AVLRecord.  Currently makes
 * large assumptions about the columns but these can be improved as needed.
 * Current columns required: vehicleId,Date,lat,lon,speed,heading
 */
public class CsvPollingAvlModule extends PollUrlAvlModule {

    private static final float KILOMETERS_PER_MILE = 1.60934f;

    private static StringConfigValue feedUrl =
            new StringConfigValue("transitclock.avl.csv.url",
                    "http://localhost:8080/csv",
                    "The URL of the CSV feed.");

    private static StringConfigValue vehicleParam =
            new StringConfigValue("transitclock.avl.csv.vehicle_param",
                    "DeviceID",
                    "CSV header for vehicleId column");

    private static StringConfigValue dateParam =
            new StringConfigValue("transitclock.avl.csv.date_param",
                    "Date",
                    "CSV header for date column");

    private static BooleanConfigValue isSeparateTimeParam =
            new BooleanConfigValue("transitclock.avl.csv.need_time_param",
                    true,
                    "If time is separate from date");

    private static StringConfigValue timeParam =
            new StringConfigValue("transitclock.avl.csv.time_param",
                    "Time",
                    "CSV header for time column");

    private static StringConfigValue dateFormatParam =
            new StringConfigValue("transitclock.avl.csv.date_format_param",
                    "yyyy/MM/ddHH:mm:ss",
                    "SimpleDateFormat constructor for date parsing");

    private static StringConfigValue latParam =
            new StringConfigValue("transitclock.avl.csv.lat_param",
                    "Latitude",
                    "CSV header for latitude column");

    private static StringConfigValue lonParam =
            new StringConfigValue("transitclock.avl.csv.lon_param",
                    "Longitude",
                    "CSV header for longitude column");

    private static StringConfigValue speedParam =
            new StringConfigValue("transitclock.avl.csv.speed_param",
                    "Speed",
                    "CSV header for speed column");

    private static BooleanConfigValue speedInMphParam =
            new BooleanConfigValue("transitclock.avl.csv.speed_is_mph_param",
                    true,
                    "True if speed is in miles per hour");

    private static StringConfigValue headingParam =
            new StringConfigValue("transitclock.avl.csv.heading_param",
                    "Heading",
                    "CSV header for heading/bearing column");

    private static final Logger logger = LoggerFactory.getLogger(CsvPollingAvlModule.class);


    /**
     * Constructor
     *
     * @param agencyId
     */
    public CsvPollingAvlModule(String agencyId) {
        super(agencyId);
    }

    @Override
    protected String getUrl() {
        return feedUrl.getValue();
    }

    @Override
    protected Collection<AvlReport> processData(InputStream in) throws Exception {
        // adapt an arbitrary csv file feed to an AVLRecord
        Collection<AvlReport> avlReportsReadIn = new ArrayList<>();
        CSVRecord record = null;
        CSVFormat formatter =
                CSVFormat.DEFAULT.withHeader().withCommentMarker('-');

        long start = System.currentTimeMillis();
        // Parse the file
        Iterable<CSVRecord> records = formatter.parse(new BufferedReader(new InputStreamReader(in)));
        Iterator<CSVRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            record = iterator.next();
            if (record.size() == 0)
                continue;
            try {
                AvlReport report = handleRecord(record);
                if (report != null) {
                    avlReportsReadIn.add(report);
                }
            } catch (Exception any) {
                // bad record -- don't let it abort the entire file
                logger.warn("bad record {} at row {}", record.toString(), record.getRecordNumber());
            }
        }
        long stop = System.currentTimeMillis();
        logger.warn("Parsed csv feed in {} ms", (stop-start)/1000);
        return avlReportsReadIn;
    }

    private AvlReport handleRecord(CSVRecord record) throws Exception {
        String vehicleId = record.get("DeviceID").trim();
        String dateStr = record.get(dateParam.getValue()).trim();
        if (isSeparateTimeParam.getValue()) {
            dateStr += record.get(timeParam.getValue()).trim();
        }
        String latStr = record.get(latParam.getValue()).trim();
        String lonStr = record.get(lonParam.getValue()).trim();
        String speedStr = record.get(speedParam.getValue()).trim();
        String headingStr = record.get(headingParam.getValue()).trim();

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatParam.toString());
        Date avlDate = sdf.parse(dateStr);
        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);
        float speed = Float.parseFloat(speedStr);
        if (speedInMphParam.getValue()) {
            speed = speed / (float)2.237;  // mph to m/s

        }
        float heading = Float.parseFloat(headingStr);

        AvlReport r = new AvlReport(vehicleId, avlDate.getTime(), lat, lon,
        speed, heading, "OpenGTS");

        return r;
    }
}
