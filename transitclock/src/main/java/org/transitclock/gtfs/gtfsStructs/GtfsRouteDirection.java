package org.transitclock.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.transitclock.utils.csv.CsvBase;
import java.text.ParseException;

public class GtfsRouteDirection extends CsvBase {

    private final String routeShortName;
    private final String directionId;
    private final String directionName;

    /********************** Member Functions **************************/

    /**
     * Creates a GtfsFeedInfo object by reading the data
     * from the CSVRecord.
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsRouteDirection(CSVRecord record, boolean supplemental, String fileName)
            throws ParseException {
        super(record, supplemental, fileName);

        routeShortName = getRequiredValue(record, "ARTICLE");
        directionId = getRequiredValue(record, "DIRNUM");
        directionName = getRequiredValue(record, "DIRECTIONNAME");
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getDirectionId() {
        return directionId;
    }

    public String getDirectionName() {
        return directionName;
    }

    public boolean containsEmptyColumn(){
        return StringUtils.isBlank(routeShortName) || StringUtils.isBlank(directionId) || StringUtils.isBlank(directionName);
    }

    @Override
    public String toString() {
        return "GtfsRouteDirection{" +
                "routeShortName='" + routeShortName + '\'' +
                ", directionId='" + directionId + '\'' +
                ", directionName='" + directionName + '\'' +
                "} " + super.toString();
    }
}