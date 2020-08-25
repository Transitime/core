package org.transitclock.db.reporting;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class TripStatistics {
    private final String tripId;

    private Map<Integer, TripStatisticsForConfigRev> tripStatisticsForConfigRevMap = new TreeMap<>(Collections.reverseOrder());

    public TripStatistics(String tripId) {
        this.tripId = tripId;
    }

    public Collection<TripStatisticsForConfigRev> getAllTripStatisticsGroupedByConfigRev() {
        return tripStatisticsForConfigRevMap.values();
    }

    public TripStatisticsForConfigRev getTripStatistics(int configRev) {
        return tripStatisticsForConfigRevMap.get(configRev);
    }

    public String getTripId() {
        return tripId;
    }

    public void addStopPathRunTime(ArrivalDeparture ad, Double runTime){
        TripStatisticsForConfigRev tripStatsConf = getTripStatsConf(ad);
        if(tripStatsConf != null){
            StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(ad.getStopId(), ad.getStopPathId(), ad.getStopPathIndex());
            tripStatsConf.addStopPathRunTime(stopPathkey, ad, runTime);
        }
    }

    public void addStopPathDwellTime(ArrivalDeparture ad, Double dwellTime){
        TripStatisticsForConfigRev tripStatsConf = getTripStatsConf(ad);
        if(tripStatsConf != null){
            StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(ad.getStopId(), ad.getStopPathId(), ad.getStopPathIndex());
            tripStatsConf.addStopPathDwellTime(stopPathkey, ad, dwellTime);
        }
    }

    public TripStatisticsForConfigRev getTripStatsConf(ArrivalDeparture ad){
        if(ad.getTripId().equals(tripId)){
            TripStatisticsForConfigRev tripStatsConf = tripStatisticsForConfigRevMap.get(ad.getConfigRev());
            if(tripStatsConf == null){
                Trip trip = ad.getTripFromDb();
                int numberOfStopPaths = trip.getNumberStopPaths();
                tripStatsConf = new TripStatisticsForConfigRev(ad.getConfigRev(),ad.getTripId(),
                        numberOfStopPaths -1, ad.getTripIndex(), ad.getBlockId());
                tripStatisticsForConfigRevMap.put(ad.getConfigRev(), tripStatsConf);
            }
            return tripStatsConf;
        }
        return null;
    }
}
