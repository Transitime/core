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
            "1,2",
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

        for(int i=0; i < feedInfosCount; i++){
            FeedInfo currentFeedInfo = feedInfos.get(i);
            DatedGtfs currentDateRangeForVersion;

            // Current Date Range
            String feedVersion = getConvertedFeedVersion(currentFeedInfo);
            currentDateRangeForVersion = datesForFeedVersion.get(feedVersion);

            // First Date Range for this version
            if(currentDateRangeForVersion == null){
                currentDateRangeForVersion = getDateRangeForFeedInfo(currentFeedInfo, feedVersion);
            }
            // Found existing Date Range for this version
            else {
                // Reconfigure start and end date by comparing to next and prev date range
                currentDateRangeForVersion = getDateRangeForFeedInfo(currentDateRangeForVersion,
                                                                     prevDateRangeForVersion);
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
                                                     DatedGtfs prevDateRangeForVersion){

        LocalDate prevStartDate = prevDateRangeForVersion.getStartDate();
        LocalDate prevEndDate = prevDateRangeForVersion.getEndDate();

        LocalDate currentStartDate = currentDateRangeForVersion.getStartDate();
        LocalDate currentEndDate = currentDateRangeForVersion.getStartDate();

        LocalDate minStartDate = prevStartDate;
        LocalDate maxEndDate = prevEndDate;

        String feedVersion = currentDateRangeForVersion.getVersion();
        int configRev = prevDateRangeForVersion.getConfigRev();

        if(!currentStartDate.isAfter(minStartDate)){
            minStartDate = currentStartDate;
        }

        if(!currentEndDate.isBefore(maxEndDate)){
            maxEndDate = currentStartDate;
            configRev = currentDateRangeForVersion.getConfigRev();
        }

        return new DatedGtfs(minStartDate, maxEndDate, feedVersion, configRev);

    }

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

    private static String buildRegexFeedName(String[] feedNameArray) throws Exception{
        String[] nameIndices = feedInfoNameIndices.toString().split(",");
        List<Integer> indices = Arrays.stream(nameIndices)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for(int i=0; i < indices.size(); i++){
            if(i>0){
                sb.append("-");
            }
            sb.append(feedNameArray[indices.get(i)]);

        }

        return sb.toString();
    }
}
