package org.transitclock.integration_tests.playback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Prediction;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;
import java.text.DateFormat;

import java.util.Date;

/**
 * Write out combined prediction records.
 */
public class CombinedPredictionCsvWriter extends CsvWriterBase {

    private final Time timeUsingTimeZone;

    private static final Logger logger =
            LoggerFactory.getLogger(CombinedPredictionCsvWriter.class);
    public CombinedPredictionCsvWriter(String fileName, String timezoneStr) {
        super(fileName, false);
        timeUsingTimeZone = new Time(timezoneStr);
    }

    @Override
    protected void writeHeader() throws IOException {
        appendLine("tripId,gtfsStopSeq,affectedByWaitStop,avlTime,configRev,old_creationTime,isArrival," +
                "old_predictionTime,routeId,schedBasedPred,stopId,vehicleId,new_predictionTime,horizonSeconds,actual,old_diff,old_error,new_diff,new_error");

    }

    public void write(CombinedPredictionAccuracy combined) {
        try {
            writeUnsafe(combined);
        } catch (IOException e) {
            logger.error("Error writing {}.", combined, e);
        }
    }

    private void writeUnsafe(CombinedPredictionAccuracy combined) throws IOException {
        if (combined.oldPrediction == null && combined.newPrediction == null) {
            // nothing to do
            logger.error("nothing to do for record avlTime={} and stopSeq={}",
                    combined.actualADTime, combined.stopSeq);
            return;
        }
        Prediction prediction = combined.oldPrediction;
        if (prediction != null) {
            appendCol(prediction.getTripId());
            // gtfsStopSeq
            appendCol(combined.stopSeq);
            // affectedByWaitStop
            appendCol(prediction.isAffectedByWaitStop());
            // avlTime
            appendCol(formatTime(new Date(combined.avlTime)));
            //configRev
            appendCol(prediction.getConfigRev());
            // creationTime
            appendCol(formatTime(prediction.getCreationTime()));
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
            // vehicleId
            appendCol(prediction.getVehicleId());
        } else if (combined.newPrediction != null) {
            prediction = combined.newPrediction;
            appendCol(prediction.getTripId());
            // gtfsStopSeq
            appendCol(combined.stopSeq);
            // affectedByWaitStop
            appendCol(prediction.isAffectedByWaitStop());
            // avlTime
            appendCol(formatTime(new Date(combined.avlTime)));
            //configRev
            appendCol(prediction.getConfigRev());
            // creationTime
            appendCol(formatTime(prediction.getCreationTime()));
            // isArrival
            appendCol(prediction.isArrival());
            // predictionTIme
            appendCol(-1);
            // routeId
            appendCol(prediction.getRouteId());
            // schedBasedPred
            appendCol(prediction.isSchedBasedPred());
            // stopId
            appendCol(prediction.getStopId());
            // vehicleId
            appendCol(prediction.getVehicleId());

        }
        // new prediction
        if (combined.newPrediction != null) {
            appendCol(formatTime(combined.newPrediction.getPredictionTime()));
        } else {
            appendCol("-1");
        }
        if (combined.actualADTime > -1) {
            // horizon
            appendCol(toSeconds(combined.predLength));
            // actual Arrival/Departure
            appendCol(formatTime(new Date(combined.actualADTime)));
            double oldDiff = toSeconds(combined.oldPredTime) - toSeconds(combined.actualADTime);
            // original error
            appendCol(oldDiff);
            if (oldDiff != 0.0) {
                appendCol(oldDiff / toSeconds(combined.predLength));
            } else {
                appendCol(0.0);
            }
            double newDiff = toSeconds(combined.newPredTime) - toSeconds(combined.actualADTime);
            // new error
            appendCol(newDiff);
            if (newDiff != 0.0) {
                appendLine(newDiff / toSeconds(combined.predLength));
            } else {
                appendLine(0.0);
            }
        } else {
            appendLine(null);
        }
    }

    private double toSeconds(long time) {
        return time / 1000.0;
    }

    private String formatTime(Date value) {
        DateFormat sdf = Time.getReadableDateFormat24NoTimeZoneNoMsec();
        return sdf.format(value);
    }
}
