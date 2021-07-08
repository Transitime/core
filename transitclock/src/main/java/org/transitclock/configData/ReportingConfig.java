package org.transitclock.configData;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

public class ReportingConfig {

    public static int getMaxSchedAdh() {
        return maxSchedAdhSec.getValue();
    }
    private static IntegerConfigValue maxSchedAdhSec =
            new IntegerConfigValue(
                    "transitclock.speedMap.maxSchedAdhSec",
                    30* Time.SEC_PER_MIN,
                    "Maximum allowable schedule adherence in sec");

    public static double getMaxStopPathSpeedMps() {
        return maxStopPathSpeedMps.getValue();
    }
    private static DoubleConfigValue maxStopPathSpeedMps =
            new DoubleConfigValue("transitclock.speedMap.maxStopPathSpeed",
                    27.0, // 27.0m/s = 60mph
                    "If a stop path is determined to have a higher "
                            + "speed than this value in meters/second then the speed "
                            + "will be decreased to meet this limit. Purpose is "
                            + "to make sure that don't get invalid speed values due to "
                            + "bad data.");

    public static double getMinStopPathSpeedMps() {
        return minStopPathSpeedMps.getValue();
    }
    private static DoubleConfigValue minStopPathSpeedMps =
            new DoubleConfigValue("transitclock.speedMap.minStopPathSpeed",
                    0.0,
                    "If a stop path is determined to have a lower "
                            + "speed than this value in meters/second then the speed "
                            + "will be decreased to meet this limit. Purpose is "
                            + "to make sure that don't get invalid speed values due to "
                            + "bad data.");
}
