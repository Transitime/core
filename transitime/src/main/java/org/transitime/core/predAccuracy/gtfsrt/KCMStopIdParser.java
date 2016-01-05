package org.transitime.core.predAccuracy.gtfsrt;

/**
 * King County Metro has a bug in there GTFS-RT feed that prepends
 * digits to stopIds.  Below is a quick-and-dirty method to strip them.
 *
 */
public class KCMStopIdParser implements StopIdParser {

  @Override
  public String parse(String stopId) {
    if (stopId.startsWith("100000"))
      return stopId.substring("100000".length(), stopId.length());
    if (stopId.startsWith("10000"))
      return stopId.substring("10000".length(), stopId.length());
    if (stopId.startsWith("1000"))
      return stopId.substring("1000".length(), stopId.length());
    if (stopId.startsWith("100"))
      return stopId.substring("100".length(), stopId.length());
     return stopId;
  }

}
