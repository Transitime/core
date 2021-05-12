package org.transitclock.core.reporting;

import org.transitclock.db.structs.RunTimesForRoutes;

/**
 * Return code and db structs output from RunTimeProcessor.
 */
public class RunTimeProcessorResult {

  private boolean success;
  private RunTimesForRoutes routes = null;

  public RunTimeProcessorResult(RunTimesForRoutes routes) {
    if (routes != null) {
      success = true;
    } else {
      success = false;
    }
    this.routes = routes;
  }
  public RunTimeProcessorResult() {
    success = false;
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
