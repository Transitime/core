package org.transitime.core.predAccuracy.gtfsrt;

import java.util.Date;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;

/**
 * King County Metro has a bug in there GTFS-RT feed that prepends
 * digits to stopIds.  Below is a quick-and-dirty method to strip them.
 *
 */
public class KCMTranslator implements GTFSRealtimeTranslator {

  @Override
  public String parseStopId(String stopId) {
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

  @Override
  public Date parseFeedHeaderTimestamp(FeedHeader header) {
    // KCM feed is three hours behind!
    return new Date(header.getTimestamp()*1000 + (3 * 60 * 60 * 1000));
  }

}
