package org.transitclock.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.api.data.SpeedFormat;
import org.transitclock.utils.MathUtils;
import static org.transitclock.api.utils.MathUtils.*;

public class NumberFormatter {
    private static final Logger logger = LoggerFactory.getLogger(NumberFormatter.class);

    public static Long getValueAsLong(Double value){
        if(value != null){
            try{
                return value.longValue();
            } catch (NumberFormatException nfe){
                logger.warn("Unable to convert {} to a Long do to a number format exception",value, nfe);
            } catch(Exception e){
                logger.warn("Hit unexpected issue converting value {} to Big Decimal", value, e);
            }
        }
        return 0L;
    }

    public static Double getRoundedValueAsDouble(float value, int precision){
        return Float.isNaN(value) ?
                null : MathUtils.round(value, precision);
    }

    public static String getRoundedValueAsString(float value, int precision){
        return Float.isNaN(value) ?
                null : String.valueOf(MathUtils.round(value, precision));
    }
}
