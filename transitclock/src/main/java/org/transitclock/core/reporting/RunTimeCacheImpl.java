package org.transitclock.core.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.RunTimesForStops;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintain a cache of RunTimesForRoutes
 * and convenience methods against that cache.
 */
public class RunTimeCacheImpl implements RunTimeCache{

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeCacheImpl.class);

  Map<String, List<RunTimesForStops>> stopsMap = new HashMap<>();
  Map<String, List<RunTimesForRoutes>> routesMap = new HashMap<>();
  Map<String, RunTimesForRoutes> routeMap = new HashMap<>();

  @Override
  public List<RunTimesForRoutes> getAll() {
    ArrayList<RunTimesForRoutes> allRoutes = new ArrayList<>(routeMap.size());
    allRoutes.addAll(routeMap.values());
    return allRoutes;
  }

  @Override
  public void update(RunTimeProcessorResult runTimeProcessorResult, List<ArrivalDeparture> arrivalDepartures) {
    routeMap.put(hash(runTimeProcessorResult.getRunTimesForRoutes()), runTimeProcessorResult.getRunTimesForRoutes());
    updateRoutesMap(routesMap, runTimeProcessorResult);
    updateStopsMap(stopsMap, runTimeProcessorResult, arrivalDepartures);
  }

  private void updateRoutesMap(Map<String, List<RunTimesForRoutes>> routesMap,
                               RunTimeProcessorResult runTimeProcessorResult) {
    String hash = hash(runTimeProcessorResult.getRunTimesForRoutes());
    if (routesMap.containsKey(hash)) {
      List<RunTimesForRoutes> list = routesMap.get(hash);
      list.add(runTimeProcessorResult.getRunTimesForRoutes());
      routesMap.put(hash, list);
    } else {
      List<RunTimesForRoutes> list = new ArrayList<>();
      list.add(runTimeProcessorResult.getRunTimesForRoutes());
      routesMap.put(hash, list);
    }
  }

  private void updateStopsMap(Map<String, List<RunTimesForStops>> keyMap,
                              RunTimeProcessorResult runTimeProcessorResult,
                              List<ArrivalDeparture> arrivalDepartures) {

    RunTimesForRoutes runTimesForRoutes = runTimeProcessorResult.getRunTimesForRoutes();
    for (RunTimesForStops runTimesForStops : runTimesForRoutes.getRunTimesForStops()) {
      String keyHash = hash(runTimesForRoutes, runTimesForStops);
      if (keyMap.containsKey(keyHash)) {
        List<RunTimesForStops> list = keyMap.get(keyHash);
        list.add(runTimesForStops);
        keyMap.put(keyHash, list);
        logger.error("{} occurs {} times: {}, ::: {}, ::: {} \n\n", keyHash, list.size(), list, runTimesForRoutes, arrivalDepartures);
      } else {
        List<RunTimesForStops> list = new ArrayList<>();
        list.add(runTimesForStops);
        keyMap.put(keyHash, list);
      }
    }
  }

  /**
   * Search the Array of RunTimesForStops for duplicates globally in the cache.
   */
  @Override
  public boolean containsDuplicateStops(RunTimesForRoutes runTimesForRoutes) {
    for (RunTimesForStops runTimesForStops: runTimesForRoutes.getRunTimesForStops()) {
      String hash = hash(runTimesForRoutes, runTimesForStops);
      int count = stopsMap.get(hash).size();
      if (count > 1) return true;
    }

    return false;
  }

  /**
   * filter out any global duplicate RunTimesForStops.
   */
  @Override
  public RunTimesForRoutes deduplicate(RunTimesForRoutes runTimesForRoutes) {
    List<RunTimesForStops> filtered = new ArrayList<>();
    for (RunTimesForStops runTimesForStops: runTimesForRoutes.getRunTimesForStops()) {
      String hash = hash(runTimesForRoutes, runTimesForStops);
      int count = stopsMap.get(hash).size();
      if (count == 1) {
        filtered.add(runTimesForStops);
      }
    }
    runTimesForRoutes.setRunTimesForStops(filtered);

    return runTimesForRoutes;
  }

  /**
   * retrieve RunTimesForRoutes from the cache that maps to the given ids
   * or create one if one doesn't exist.
   */
  @Override
  public RunTimesForRoutes getOrCreate(int configRev, String id, Date startTime, String vehicleId) {
    String routesHash = hash(configRev, id, startTime, vehicleId);
    if (routesMap.containsKey(routesHash))
      return routesMap.get(routesHash).get(0);
    RunTimesForRoutes rt = new RunTimesForRoutes();
    rt.setConfigRev(configRev);
    rt.setTripId(id);
    rt.setStartTime(startTime);
    rt.setVehicleId(vehicleId);
    return rt;
  }

  /**
   * Ensure object is complete enough to persist.
   */
  @Override
  public boolean isValid(RunTimesForRoutes rt) {
    if (rt.getStartTime() == null) {
      return false;
    }

    List<RunTimesForRoutes> runTimesForRoutes = routesMap.get(hash(rt));
    if(runTimesForRoutes !=null && runTimesForRoutes.size() > 1){
      return false;
    }
    return true;
  }

  private String hash(RunTimesForRoutes rt) {
    return hash(rt.getConfigRev(), rt.getTripId(), rt.getStartTime(), rt.getVehicleId());
  }

  private String hash(int configRev, String id, Date startTime, String vehicleId) {
    return configRev
            + "-" + id
            + "-" + (startTime==null?"Null": Time.dateTimeStr(startTime))
            + "-" + vehicleId;
  }

  private String hash(RunTimesForRoutes runTimesForRoutes, RunTimesForStops runTimesForStops) {
    try {
      return runTimesForRoutes.getConfigRev()
              + "_" + (runTimesForRoutes.getStartTime() == null ? "null" : Time.dateTimeStr(runTimesForRoutes.getStartTime()))
              + "_" + runTimesForRoutes.getTripId()
              + "_" + runTimesForRoutes.getVehicleId()
              + "_" + Time.dateTimeStr(runTimesForStops.getTime())
              + "_" + runTimesForStops.getStopPathIndex();
    }catch(NullPointerException e){
      e.printStackTrace();
      return null;
    }
  }

}
