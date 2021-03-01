package org.transitclock.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
