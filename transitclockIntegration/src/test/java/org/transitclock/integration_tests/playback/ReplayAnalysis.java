package org.transitclock.integration_tests.playback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Provide some insights into old predictions vs new predictions.
 */
public class ReplayAnalysis {

    private static final Logger logger = LoggerFactory.getLogger(ReplayAnalysis.class);

    public ReplayAnalysis() {

    }

    public ReplayResults compare(Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy) {
        int oldTotalPreds = 0, newTotalPreds = 0, bothTotalPreds = 0;

        double oldTotalError = 0, newTotalError = 0;

        int oldBetter = 0, newBetter = 0;

        int oldPredsForUnobservedStop = 0, newPredsForUnobservedStop = 0;

        // For each avltime/stopid/type, check if better or worse, etc
        for (CombinedPredictionAccuracy pred : combinedPredictionAccuracy) {

            double oldError = 0, newError = 0;

            if (pred.oldPredTime > 0) {
                if (pred.actualADTime > 0) {
                    oldTotalPreds++;
                    oldError = (double) (pred.oldPredTime - pred.actualADTime) / pred.predLength;
                    oldTotalError += oldError;
                }
                else
                    oldPredsForUnobservedStop++;
            }

            if (pred.newPredTime > 0) {
                if (pred.actualADTime > 0) {
                    newTotalPreds++;
                    newError = (double) (pred.newPredTime - pred.actualADTime) / pred.predLength;
                    newTotalError += newError;
                }
                else
                    newPredsForUnobservedStop++;
            }

            if (pred.oldPredTime > 0 && pred.newPredTime > 0 && pred.actualADTime > 0) {
                bothTotalPreds++;
                logger.debug("matched prediction {}, {}, {}, {}",
                        pred.stopSeq, pred.actualADTime, pred.oldPredTime, pred.newPredTime);

                if (oldError < newError)
                    oldBetter++;
                else if (newError < oldError)
                    newBetter++;
            }
        }

        oldTotalError /= oldTotalPreds;
        newTotalError /= newTotalPreds;

        logger.info("Old total predictions: {}, old total error: {}, old predictions for unobserved stops: {}",
                oldTotalPreds, oldTotalError, oldPredsForUnobservedStop);
        logger.info("New total predictions: {}, new total error: {}, new predictions for unobserved stops: {}",
                newTotalPreds, newTotalError, newPredsForUnobservedStop);
        logger.info("Predictions for both: {}, old better: {}, new better: {}",
                bothTotalPreds, oldBetter, newBetter);
        ReplayResults results = new ReplayResults();
        results.setOldTotalPreds(oldTotalPreds);
        results.setNewTotalPreds(newTotalPreds);
        results.setOldTotalError(oldTotalError);
        results.setNewTotalError(newTotalError);
        results.setOldBetter(oldBetter);
        results.setBothTotalPreds(bothTotalPreds);
        return results;

    }
}
