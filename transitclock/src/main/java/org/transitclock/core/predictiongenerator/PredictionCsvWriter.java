package org.transitclock.core.predictiongenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Prediction;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * serialize Prediction object to CSV.
 */
public class PredictionCsvWriter extends CsvWriterBase {

    private final Time timeUsingTimeZone;

    private static final Logger logger =
            LoggerFactory.getLogger(PredictionCsvWriter.class);

    public PredictionCsvWriter(String fileName, String timezoneStr) {
        super(fileName, false);
        timeUsingTimeZone = new Time(timezoneStr);
    }
    @Override
    protected void writeHeader() throws IOException {
        appendLine("id,affectedByWaitStop,avlTime,configRev,creationTime,gtfsStopSeq,isArrival," +
                "predictionTime,routeId,schedBasedPred,stopId,tripId,vehicleId");
    }

    public void write(Prediction prediction) {
        try {
            writeUnsafe(prediction);
        } catch (IOException e) {
            logger.error("Error writing {}.", prediction, e);
        }
    }
    private void writeUnsafe(Prediction prediction) throws IOException {
        // id
        appendCol(prediction.getId());
        // affectedByWaitStop
        appendCol(prediction.isAffectedByWaitStop());
        // avlTime
        appendCol(formatTime(prediction.getAvlTime()));
        //configRev
        appendCol(prediction.getConfigRev());
        // creationTime
        appendCol(formatTime(prediction.getCreationTime()));
        // gtfsStopSeq
        appendCol(prediction.getGtfsStopSeq());
        // isArrival
        appendCol(prediction.isArrival());
        // predictionTIme
        appendCol(formatTime(prediction.getPredictionTime()));
        // routeId
        appendCol(prediction.getRouteId());
        // schedBasedPred
        appendCol(prediction.isSchedBasedPred());
        // stopId
        appendCol(prediction.getStopId());
        // tripId
        appendCol(prediction.getTripId());
        // vehicleId
        appendLine(prediction.getVehicleId());
    }

    private String formatTime(Date value) {
        DateFormat sdf = Time.getReadableDateFormat24NoTimeZoneNoMsec();
        return sdf.format(value);
    }
}
