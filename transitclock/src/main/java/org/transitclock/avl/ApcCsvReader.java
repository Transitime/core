package org.transitclock.avl;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.ApcReport;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;

/**
 * Support for reading APC data as CSV
 */
public class ApcCsvReader extends CsvBaseReader<ApcReport> {

    private int configRev;

    public ApcCsvReader(String fileName, int configRev) {
        super(fileName);
        this.configRev = configRev;
    }

    @Override
    protected ApcReport handleRecord(CSVRecord r, boolean supplemental) throws ParseException, NumberFormatException {
        try {
            String messageId = r.get("messageId");
            int alightings = Integer.parseInt(r.get("alightings"));
            int arrival = Integer.parseInt(r.get("arrival"));
            int boardings = Integer.parseInt(r.get("boardings"));
            int departure = Integer.parseInt(r.get("departure"));
            int doorClose = Integer.parseInt(r.get("doorClose"));
            int doorOpen = Integer.parseInt(r.get("doorOpen"));
            String driverId = r.get("driverId");
            double lat = Double.parseDouble(r.get("lat"));
            double lon = Double.parseDouble(r.get("lon"));
            int odo = Integer.parseInt(r.get("odo"));
            String serviceDateStr = r.get("serviceDate");
            long serviceDate = Long.parseLong(serviceDateStr);
            String timeStr = r.get("time");
            long time = Time.parse(timeStr).getTime();
            String vehicleId = r.get("vehicleId");
            ApcReport apc = new ApcReport(messageId,
                    time,
                    serviceDate,
                    driverId,
                    odo,
                    vehicleId,
                    boardings,
                    alightings,
                    doorOpen,
                    doorClose,
                    arrival,
                    departure,
                    lat,
                    lon,
                    null);
            return apc;
        } catch (ParseException ex) {
            logger.error(ex.getMessage());
            return null;
        } catch (Throwable t) {
            logger.error("unexpected data caused {}", t, t);
            return null;
        }
    }
}
