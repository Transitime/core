package org.transitclock.core.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.RunTimesForRoutes;

/**
 * Return code and db structs output from RunTimeProcessor.
 */
public class RunTimeProcessorResult {

  private static final Logger logger = LoggerFactory.getLogger(RunTimeProcessorResult.class);

  private boolean success;
  private RunTimesForRoutes routes = null;


  public RunTimeProcessorResult(RunTimesForRoutes routes) {
    if (isValid(routes)) {
      success = true;
    } else {
      success = false;
    }
    this.routes = routes;
  }
  public RunTimeProcessorResult() {
    success = false;
  }

  private boolean isValid(RunTimesForRoutes routes){
    if(routes != null && routes.getVehicleId() != null && routes.getTripId() != null && routes.getStartTime() != null){
      return true;
    }
    logger.warn("Invalid RunTimeForRoutes {}", routes);
    return false;
  }

  public boolean success() {
    return success
            && routes != null
            && routes.getRunTimesForStops() != null
            && !routes.getRunTimesForStops().isEmpty();
  }

  public RunTimesForRoutes getRunTimesForRoutes() {
    return routes;
  }
}
