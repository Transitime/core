package org.transitclock.integration_tests.playback;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Prediction;
import org.transitclock.utils.DateRange;

import java.util.*;

import static org.transitclock.utils.Time.sleep;

/**
 * Delegate data loading operations to its own class.
 */
public class ReplayLoader {

    private static final Logger logger = LoggerFactory.getLogger(ReplayLoader.class);
    private Session session;


    private ReplayCsv csv;

    private Collection<CombinedPredictionAccuracy> combinedPredictionAccuracies;
    public Collection<CombinedPredictionAccuracy> getCombinedPredictionAccuracies() {
        return combinedPredictionAccuracies;
    }

    private Map<PredictionKey, CombinedPredictionAccuracy> predsByStopAndAvlTime
            = new HashMap<PredictionKey, CombinedPredictionAccuracy>();


    public ReplayLoader(String outputDirectory) {
        this.csv = new ReplayCsv(outputDirectory);
    }


    public List<ArrivalDeparture> queryArrivalDepartures(DateRange avlRange, String arrivalDepartureFileName) {
        // Fill CombinedPredictionAccuracy objects with stop information
        waitForQueuesToDrain();
        logger.info("loading A/Ds for {}", avlRange);
        List<ArrivalDeparture> ads = getSession()
                .createCriteria(ArrivalDeparture.class)
                .add(Restrictions.between("time", avlRange.getStart(), avlRange.getEnd()))
                .addOrder(Order.asc("time"))
                .list();

        if (ads == null || ads.isEmpty())
            throw new RuntimeException("no ArrivalDepartures found, cannot prime data store");

        return ads;
    }

    private void waitForQueuesToDrain() {
        final int MAX_COUNT = 20;
        sleep(5000);
        int i = 0;
        while (Core.getInstance().getDbLogger().queueSize() > 0 && i < MAX_COUNT) {
            i++;
            logger.info("waiting on queues to drain with remaining size {}",
                    Core.getInstance().getDbLogger().queueSize());
            sleep(1000);
        }
        if (i >= MAX_COUNT) {
            logger.warn("DbLogger did not empty in allotted time.");
        }
    }

    public void loadPredictionsFromCSV(String predictionsCsvFileName) {

        List<Prediction> predictions = csv.loadPredictions(predictionsCsvFileName);
        for (Prediction p : predictions) {
            // Fill old predictions
            PredictionKey key = createKeyFromPrediction(p);
            CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndAvlTime, key);
            pred.setOldPrediction(p);
        }

    }

    private CombinedPredictionAccuracy getOrCreatePred(
                    Map<PredictionKey, CombinedPredictionAccuracy>
                    predsByStopAndCreationTime,
                    PredictionKey key) {
        CombinedPredictionAccuracy pred = predsByStopAndCreationTime.get(key);
        if (pred == null) {
            // This prediction does not have an associated arrival departure. Cannot gauge accuracy.
            pred = new CombinedPredictionAccuracy(key.getTripId(), key.getStopSequence(),
                    key.getArrivalOrDeparture(), key.getAvlTime());
            predsByStopAndCreationTime.put(key, pred);
        }
        return pred;
    }

    public void accumulate(String id, List<ArrivalDeparture> arrivalDepartures) {
        List<Prediction> newPreds = getSession().createCriteria(Prediction.class).list();
        csv.write(newPreds,"prediction", id);


        for (Prediction p : newPreds) {
            PredictionKey key = createKeyFromPrediction(p);
            CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndAvlTime, key);
            pred.setNewPrediction(p);
        }

        combinedPredictionAccuracies = predsByStopAndAvlTime.values();

        // match the A/Ds to the predictions for accuracy comparison
        for (CombinedPredictionAccuracy combined : combinedPredictionAccuracies) {
            for (ArrivalDeparture ad : arrivalDepartures) {
                // match on trip / stop / direction
                if (match(combined, ad)) {
                    combined.actualADTime = ad.getTime();
                    combined.predLength = combined.actualADTime - combined.avlTime;
                }
            }
        }


        ArrayList<CombinedPredictionAccuracy> sortedList = filter(combinedPredictionAccuracies);
        ComparatorChain chain = new ComparatorChain();

        chain.addComparator(new CombinedPredictionAccuracyTripIdComparator());
        chain.addComparator(new CombinedPredictionAccuracyAvlTimeComparator());
        chain.addComparator(new CombinedPredictionAccuracyStopSequenceComparator());

        Collections.sort(sortedList, chain);

        csv.write(sortedList, "combined_prediction", id);

        getSession().close();

    }

    private boolean match(CombinedPredictionAccuracy combined, ArrivalDeparture ad) {
        // match on trip / stop / direction
        if (combined.tripId.equals(ad.getTripId())
        && combined.stopSeq == ad.getGtfsStopSequence()
        && combined.which.equals(CombinedPredictionAccuracy.ArrivalOrDeparture.ARRIVAL) == ad.isArrival()) {
            return true;
        }
        return false;
    }

    // remove nonsensical accuracy objects
    private ArrayList<CombinedPredictionAccuracy> filter(Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy) {
        ArrayList<CombinedPredictionAccuracy> filtered = new ArrayList<>();
        for (CombinedPredictionAccuracy c : combinedPredictionAccuracy) {
            if (c.oldPredTime > -1 || c.newPredTime > -1)
                filtered.add(c);
            if (c.oldPredTime == -1 && c.newPredTime == -1) {
                logger.error("unmatched A/D {}", c);
            }
        }
        return filtered;
    }


    private static PredictionKey createKeyFromPrediction(Prediction p) {
        return new PredictionKey(p.getTripId(), p.getGtfsStopSeq(),
                p.isArrival() ? CombinedPredictionAccuracy.ArrivalOrDeparture.ARRIVAL : CombinedPredictionAccuracy.ArrivalOrDeparture.DEPARTURE,
                p.getAvlTime().getTime());
    }

    // lazy load the session so config can happen first
    private Session getSession() {
        if (session == null) {
            session = HibernateUtils.getSession();
        }
        return session;
    }

}
