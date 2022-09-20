package org.transitclock.reporting.service.runTime.prescriptive.helper;

import org.transitclock.applications.Core;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.FeedInfo;
import org.transitclock.reporting.service.runTime.prescriptive.model.DatedGtfs;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class DatedGtfsService {

    private static final StringConfigValue feedInfoVersionRegex = new StringConfigValue(
            "transitclock.runTime.feedInfoVersionRegex",
            "-",
            "The number of days in the past that prescriptive runtimes looks back.");

    private static final StringConfigValue feedInfoVersionIndices = new StringConfigValue(
            "transitclock.runTime.feedInfoVersionIndices",
            "1",
            "Comma separated list of indices that make up feedVersion");

    private static final StringConfigValue feedInfoNameIndices = new StringConfigValue(
            "transitclock.runTime.feedInfoNameIndices",
            "0",
            "Comma separated list of indices that make up feedName");

    public static List<DatedGtfs> getDatedGtfs(){
        List<FeedInfo> feedInfos = Core.getInstance().getDbConfig().getFeedInfos();
        Map<String, DatedGtfs> datesForFeedVersion = new HashMap<>();
        int feedInfosCount = feedInfos.size();

        DatedGtfs prevDateRangeForVersion = null;

        Map<Integer, Long> dateRangeByConfigRev = feedInfos.stream().collect(Collectors.toMap(d -> d.getConfigRev(),
                                                    d-> d.getFeedEndDate().getTime() - d.getFeedStartDate().getTime()));

        // Loop through feed info
        for(int i=0; i < feedInfosCount; i++){
            FeedInfo currentFeedInfo = feedInfos.get(i);
            DatedGtfs currentDateRangeForVersion;

            // Current Date Range
            String feedVersion = getConvertedFeedVersion(currentFeedInfo);
            currentDateRangeForVersion = getDateRangeForFeedInfo(currentFeedInfo, feedVersion);

            // First Date Range for this version
            if(datesForFeedVersion.containsKey(feedVersion)){
                // Reconfigure start and end date by comparing to next and prev date range
                currentDateRangeForVersion = getDateRangeForFeedInfo(currentDateRangeForVersion,
                                                                     prevDateRangeForVersion,
                                                                     dateRangeByConfigRev);
            }

            // Add to map
            if(currentDateRangeForVersion != null){
                datesForFeedVersion.put(feedVersion, currentDateRangeForVersion);
            }

            // Set Prev Date Range
            prevDateRangeForVersion = currentDateRangeForVersion;
        }

        Map<String, DatedGtfs> sortedDatesForFeedVersion = new LinkedHashMap<>();
        datesForFeedVersion.entrySet()
                           .stream()
                           .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                           .forEachOrdered(x -> sortedDatesForFeedVersion.put(x.getKey(), x.getValue()));

        return sortedDatesForFeedVersion.values().stream().collect(Collectors.toList());
    }


    private static DatedGtfs getDateRangeForFeedInfo(FeedInfo feedInfo, String feedVersion){
        try {
            LocalDate feedStartDate =  Instant.ofEpochMilli(feedInfo.getFeedStartDate().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate feedEndDate =  Instant.ofEpochMilli(feedInfo.getFeedEndDate().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            return new DatedGtfs(feedStartDate, feedEndDate, feedVersion, feedInfo.getConfigRev());
        } catch (Exception e){
            return null;
        }
    }

    private static DatedGtfs getDateRangeForFeedInfo(DatedGtfs currentDateRangeForVersion,
                                                     DatedGtfs prevDateRangeForVersion,
                                                     Map<Integer, Long> dateRangeByConfigRev){
        // dates
        LocalDate prevStartDate = prevDateRangeForVersion.getStartDate();
        LocalDate prevEndDate = prevDateRangeForVersion.getEndDate();

        LocalDate currentStartDate = currentDateRangeForVersion.getStartDate();
        LocalDate currentEndDate = currentDateRangeForVersion.getEndDate();

        LocalDate minStartDate = prevStartDate;
        LocalDate maxEndDate = prevEndDate;

        // feed version
        String currentFeedVersion = currentDateRangeForVersion.getVersion();

        // configRev
        int configRev = prevDateRangeForVersion.getConfigRev();
        int currentConfigRev = currentDateRangeForVersion.getConfigRev();

        if(!currentStartDate.isAfter(minStartDate)){
            minStartDate = currentStartDate;
        }

        if(!currentEndDate.isBefore(maxEndDate)){
            maxEndDate = currentEndDate;
        }

        Long prevConfigRevDateRange = dateRangeByConfigRev.get(configRev);
        Long currentConfigRevDateRange = dateRangeByConfigRev.get(currentConfigRev);

        if(currentConfigRevDateRange > prevConfigRevDateRange){
            configRev = currentConfigRev;
        }

        return new DatedGtfs(minStartDate, maxEndDate, currentFeedVersion, configRev);

    }

    /**
     * Get feed version from feedInfo
     * Use configurable regex to parse feedVersion
     * @param feedInfo
     * @return
     */
    private static String getConvertedFeedVersion(FeedInfo feedInfo){
        String feedVersion = feedInfo.getFeedVersion();
        String regex = feedInfoVersionRegex.getValue();

        try{
            String[] feedVersionArray = feedVersion.split(regex);
            feedVersion = buildRegexFeedVersion(feedVersionArray);
            if(feedVersion == null){
                return feedVersion;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return feedVersion;
    }

    /**
     * Use configurable list of indices to determine which indices from the split FeedInfo FeedVersion
     * makes up the trimmed down feedVersion
     * @param feedVersionArray
     * @return
     * @throws Exception
     */
    private static String buildRegexFeedVersion(String[] feedVersionArray) throws Exception{
        String[] indicesConfig = feedInfoVersionIndices.toString().split(",");
        List<Integer> indices = Arrays.stream(indicesConfig)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for(int i=0; i < indices.size(); i++){
            if(i>0){
                sb.append("-");
            }
            sb.append(feedVersionArray[indices.get(i)]);

        }

        return sb.toString();
    }

}
