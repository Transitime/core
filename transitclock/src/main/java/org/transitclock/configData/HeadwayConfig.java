package org.transitclock.configData;

import org.transitclock.config.BooleanConfigValue;

public class HeadwayConfig {

    public static Boolean matchByTripPattern() {
        return matchByTripPattern.getValue();
    }
    private static BooleanConfigValue matchByTripPattern =
            new BooleanConfigValue("transitclock.headway.matchByTripPattern",
                    true,
                    "Specifies whether to look for vehicles with matching tripPatterns when generating headways.");


    public static Boolean calculateSystemVariance() {
        return calculateSystemVariance.getValue();
    }
    private static BooleanConfigValue calculateSystemVariance =
            new BooleanConfigValue("transitclock.headway.calculateSystemVariance",
                    false,
                    "Specifies whether to calculate and set system variance values for headways.");
}
