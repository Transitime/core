package org.transitime.core.predAccuracy.gtfsrt;

import java.util.Date;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;

public interface GTFSRealtimeTranslator {
  String parseStopId(String inputStopId);

  Date parseFeedHeaderTimestamp(FeedHeader header);
}
