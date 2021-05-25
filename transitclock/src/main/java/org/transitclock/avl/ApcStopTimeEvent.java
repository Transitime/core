package org.transitclock.avl;

import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.DateUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import static org.transitclock.avl.ApcMatcher.APC_MATCH_WINDOW_MINUTES;

/**
 * An APC dwell time calculation result.
 */
public class ApcStopTimeEvent implements Serializable {

  private final String hash;

  public ApcStopTimeEvent(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
    hash = indices.getTrip().getId() + ":"
            + indices.getStopPathIndex() + ":"
            + DateUtils.dateBinning(new Date(avlReport.getTime()), Calendar.MINUTE, APC_MATCH_WINDOW_MINUTES.getValue());
  }

  @Override
  public String toString() {
    return hash;
  }

  @Override
  public int hashCode() {
    return hash.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    String s = (String) obj;
    return s.equals(hash);
  }
}
