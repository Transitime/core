package org.transitime.core.predAccuracy.gtfsrt;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;

/**
 * King County Metro has a bug in there GTFS-RT feed that prepends
 * digits to stopIds.  Below is a quick-and-dirty method to strip them.
 *
 */
public class KCMTranslator implements GTFSRealtimeTranslator {

  private static final Logger logger = LoggerFactory
      .getLogger(KCMTranslator.class);
  
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
    Date feedDate = new Date(header.getTimestamp()*1000 + (3 * 60 * 60 * 1000));
    if (Math.abs(System.currentTimeMillis() - feedDate.getTime()) > 1 * 60 * 1000) {
      // if the feed is reporting a date of more than a minute ago, we have significant clock skew
      // this will ruin reports, so pretend the feed is up-to-date
      logger.error("Feed has clock skew of {} mins, time {}", (System.currentTimeMillis() - feedDate.getTime())/60000, feedDate);
      return new Date();
    }
    return feedDate;
  }

}
