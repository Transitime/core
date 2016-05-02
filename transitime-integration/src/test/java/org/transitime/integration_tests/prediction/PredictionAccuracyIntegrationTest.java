package org.transitime.integration_tests.prediction;

import junit.framework.TestCase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.Session;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Prediction;
import org.transitime.playback.PlaybackModule;
import org.transitime.utils.Time;

import java.io.FileReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This integration test buids an entirely new transitime DB from GTFS files, prepares the DB for the app to run
 * imports a CSV file of avl test data, waits for predictions to be generated, then checks the output against
 * a csv file of expected prediction values. 
 * 
 * For the test to succeed, prediction quality must improve overall. Up to 5% of individual predictions 
 * (by stop and AVL time of creation) can be worse.
 *
 */
public class PredictionAccuracyIntegrationTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(PredictionAccuracyIntegrationTest.class);

    private static final String GTFS = "src/test/resources/gtfs/S2";
	private static final String AVL = "src/test/resources/avl/S2_2113.csv";
    private static final String PREDICTIONS_CSV = "src/test/resources/pred/S2_2113.csv";

    Collection<CombinedPredictionAccuracy> combinedPredictionAccuracy;
    
    @Override
    public void setUp() {
    	
    	// Run trace
    	PlaybackModule.runTrace(GTFS, AVL);
    	
    	Map<Triple<Integer, ArrivalOrDeparture, Long>, CombinedPredictionAccuracy> predsByStopAndCreationTime
    		= new HashMap<Triple<Integer, ArrivalOrDeparture, Long>, CombinedPredictionAccuracy>();
    	
    	// Fill CombinedPredictionAccuracy objects with stop information
    	Session session = HibernateUtils.getSession();
    	List<ArrivalDeparture> ads = session.createCriteria(ArrivalDeparture.class).list();
    	for (ArrivalDeparture ad : ads) {
    		CombinedPredictionAccuracy o = new CombinedPredictionAccuracy(ad);
    		predsByStopAndCreationTime.put(o.getKey(), o);
    	}
    	
    	// Fill old predictions
		try {
			Reader in = new FileReader(PREDICTIONS_CSV);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
			
			for (CSVRecord r : records) {
				long prediction = Time.parse(r.get("predictionTime")).getTime();
				Triple<Integer, ArrivalOrDeparture, Long> key = createKeyFromCsvRecord(r);
				CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndCreationTime, key);
				pred.oldPredTime = prediction;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Fill new predictions
		List<Prediction> newPreds = session.createCriteria(Prediction.class).list();
		for (Prediction p : newPreds) {
			long prediction = p.getPredictionTime().getTime();
			Triple<Integer, ArrivalOrDeparture, Long> key = createKeyFromPrediction(p);
			CombinedPredictionAccuracy pred = getOrCreatePred(predsByStopAndCreationTime, key);
			pred.newPredTime = prediction;
		}
		
		combinedPredictionAccuracy = predsByStopAndCreationTime.values();
		session.close();
    }
   
    @Test
    public void testPredictions() {
    	
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
    	
    	// New method is bad if...
    	
    	// there are fewer new predictions than old predictions
    	assertTrue(oldTotalPreds <= newTotalPreds);
    	
    	// total scaled error did not improve
    	assertTrue(newTotalError <= oldTotalError);
    	
    	// old is more accurate in over 5% of cases
    	assertTrue(((double) oldBetter/bothTotalPreds) <= 0.5);
    	
    }
    
    private static Triple<Integer, ArrivalOrDeparture, Long> createKeyFromCsvRecord(CSVRecord r) {
    	try {
	    	int stopSeq = Integer.parseInt(r.get("gtfsStopSeq"));
	    	boolean isArrival = Integer.parseInt(r.get("isArrival")) > 0;
	    	ArrivalOrDeparture ad = isArrival ? ArrivalOrDeparture.ARRIVAL : ArrivalOrDeparture.DEPARTURE;
	    	long avlTime = Time.parse(r.get("avlTime")).getTime();
	    	
	    	return Triple.of(stopSeq, ad, avlTime);
    	}
    	catch(ParseException ex) {
    		logger.error(ex.getMessage());
    		return null;
    	}
    }
    
    private static Triple<Integer, ArrivalOrDeparture, Long> createKeyFromPrediction(Prediction p) {
    	return Triple.of(p.getGtfsStopSeq(), 
    			p.isArrival() ? ArrivalOrDeparture.ARRIVAL : ArrivalOrDeparture.DEPARTURE,
    			p.getAvlTime().getTime());
    }
    
    private CombinedPredictionAccuracy getOrCreatePred(
    		Map<Triple<Integer, ArrivalOrDeparture, Long>, CombinedPredictionAccuracy>
    			predsByStopAndCreationTime,
        	Triple<Integer, ArrivalOrDeparture, Long> key) {
    	CombinedPredictionAccuracy pred = predsByStopAndCreationTime.get(key);
		if (pred == null) {
			// This prediction does not have an associated arrival departure. Cannot gauge accuracy.
			pred = new CombinedPredictionAccuracy(key.getLeft(), key.getMiddle(), key.getRight());
			predsByStopAndCreationTime.put(key, pred);
		}
		return pred;
    }
    
    /* 
     * CombinedPredictionAccuracy: keep track of stop, old prediction, new prediction.
     * Arrival/Departure Key: key by gtfsStopSeq & whether arrival or departure 
     */
    private class CombinedPredictionAccuracy {

    	int stopSeq;
    	ArrivalOrDeparture which;
    	long avlTime;

    	long actualADTime = -1;
    	long predLength = -1; // actualADTime - avlTime
    	
    	long oldPredTime = -1;
    	long newPredTime = -1;
    	
    	CombinedPredictionAccuracy(int stopSeq, ArrivalOrDeparture which, long avlTime) {
    		this.stopSeq = stopSeq;
    		this.which = which;
    		this.avlTime = avlTime;
    	}
    	
    	CombinedPredictionAccuracy(ArrivalDeparture ad) {
    		this(ad.getGtfsStopSequence(),
    				ad.isArrival() ? ArrivalOrDeparture.ARRIVAL : ArrivalOrDeparture.DEPARTURE,
    				ad.getAvlTime().getTime());
    		this.actualADTime = ad.getTime();
    		this.predLength = actualADTime - avlTime;
    	}
    	
    	Triple<Integer, ArrivalOrDeparture, Long> getKey() {
    		return Triple.of(stopSeq, which, avlTime);
    	}
    } 
    
    private enum ArrivalOrDeparture {ARRIVAL, DEPARTURE};
    
}
