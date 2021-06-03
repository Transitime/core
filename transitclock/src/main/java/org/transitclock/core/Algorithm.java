package org.transitclock.core;

public enum Algorithm {
  // lower order bits are travel time algorithm
  // higher order bits are dwell time algorithm
  UNKNOWN(-1),
  SPEED(0),
  SCHED(1),
  SERVC(2),
  TRIP(3),
  AVL(4),
  KALMAN(5),
  HISTORICAL_AVERAGE(6),
  LAST_VEHICLE(7),
  NEXTBUS(8),
  EXTERNAL(9),
  APC_DWELL(17),
  LEGACY_DWELL(18),
  HISTORICAL_DWELL(19);
  private int value;
  public int getValue() {
    return value;
  }
  private Algorithm(int value) { this.value = value; }

  public static Algorithm fromValue(int algorithm) {
    switch (algorithm) {
      case 0:
        return SPEED;
      case 1:
        return SCHED;
      case 2:
        return SERVC;
      case 3:
        return TRIP;
      case 4:
        return AVL;
      case 5:
        return KALMAN;
      case 6:
        return HISTORICAL_AVERAGE;
      case 7:
        return LAST_VEHICLE;
      case 8:
        return NEXTBUS;
      case 9:
        return EXTERNAL;
      case 17:
        return APC_DWELL;
      case 18:
        return LEGACY_DWELL;
      case 19:
        return HISTORICAL_DWELL;
      default:
        return UNKNOWN;
    }
  }

}
