package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Location;
import org.transitclock.utils.Geo;
import org.transitclock.utils.Time;

import java.util.Date;
import java.util.List;

/**
 * Represents a match of an APC observation to an AVL ArrivalDeparture.
 * They likely come from disparate systems and therefore require some
 * effort to reconcile.
 */
public class ApcMatch {

  private static final Logger logger = LoggerFactory.getLogger(ApcMatch.class);
  private static final IntegerConfigValue MAX_MATCH_DISTANCE
          = new IntegerConfigValue(
          "transitclock.avl.stopMatchDistance",
          250,
          "max distance in meters an ArrivalDeparture can " +
                  "be from stop and still be considered a match");

  private ApcParsedRecord apc;
  private List<ArrivalDeparture> arrivalDepartures;
  public ApcMatch(ApcParsedRecord apc, List<ArrivalDeparture> arrivalDepartures) {
    this.apc = apc;
    this.arrivalDepartures = arrivalDepartures;
  }

  public ApcParsedRecord getApc() {
    if (apc.getArrivalDeparture() == null) {
      apc.setArrivalDeparture(getArrivalDeparture());
    }
    return apc;
  }

  /**
   * the full set of ArrivalDepartures within the configured window.
   * @return
   */
  public List<ArrivalDeparture> getArrivalDepartures() {
    return arrivalDepartures;
  }

  public ArrivalDeparture getArrivalDeparture() {
    return findBestArrival(arrivalDepartures);
  }

  /**
   * Multiple arrivals may occur within window, select the best among them.
   * @param arrivalDepartures
   * @return
   */
  private ArrivalDeparture findBestArrival(List<ArrivalDeparture> arrivalDepartures) {
    if (arrivalDepartures == null || arrivalDepartures.isEmpty()) return null;
    double closest = Double.MAX_VALUE;
    ArrivalDeparture best = null;
    Location apcLocation = toApcLocation(apc);
    for (ArrivalDeparture ad : arrivalDepartures) {
      Location adLocation = toAdLocation(ad);
      // if the stop location is found
      if (adLocation != null) {
        double distance = Geo.distance(apcLocation, adLocation);
        if (distance < closest) {
          closest = distance;
          best = ad;
        }
      }
    }
    if (closest > MAX_MATCH_DISTANCE.getValue()) {
      if (arrivalDepartures != null && !arrivalDepartures.isEmpty()) {
        logger.error("distance {} precluded apc {}/{} from matching to ad {}/{}",
                closest,
                apc.getVehicleId(),
                new Date(apc.getTime()),
                best.getVehicleId(),
                new Date(best.getTime()));
        return null;
      }
    }
    logger.debug("match distance {}m / temporal difference {}s for {}",
            Geo.oneDigitFormat(closest),
            Time.secondsStr(apc.getTime() - best.getTime()),
            best.getVehicleId());
    return best;
  }

  private Location toAdLocation(ArrivalDeparture ad) {
    if (ad == null) {
      return null;
    }
    if (ad.getStop() == null) {
      logger.error("no stop found for stop_id {}", ad.getStopId());
      return null;
    }
    return ad.getStop().getLoc();
  }

  private Location toApcLocation(ApcParsedRecord apc) {
    return new Location(apc.getLat(), apc.getLon());
  }
}
