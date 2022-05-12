package org.transitclock.reporting.service.runTime.prescriptive.helper;

import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.FeedInfo;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.reporting.service.runTime.prescriptive.model.RunTimeDateRange;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class RunTimeDateRangeHelper {

    private static final StringConfigValue feedInfoVersionRegex = new StringConfigValue(
            "transitclock.runTime.feedInfoVersionRegex",
            "-",
            "The number of days in the past that prescriptive runtimes looks back.");

    private static final StringConfigValue feedInfoVersionIndices = new StringConfigValue(
            "transitclock.runTime.feedInfoVersionIndices",
            "1,2",
            "Comma separated list of indices that make up feedVersion");

    public static List<RunTimeDateRange> getDateRanges(){
        List<RunTimeDateRange> dateRanges = new ArrayList<>();
        List<FeedInfo> feedInfos = Core.getInstance().getDbConfig().getFeedInfos();
        Map<String, RunTimeDateRange> datesForFeedVersion = new HashMap<>();
        int feedInfosCount = feedInfos.size();

        RunTimeDateRange prevDateRangeForVersion = null;

        for(int i=0; i < feedInfosCount; i++){
            FeedInfo currentFeedInfo = feedInfos.get(i);
            String currentFeedVersion = getConvertedFeedVersion(currentFeedInfo);
            RunTimeDateRange currentDateRangeForVersion;
            RunTimeDateRange nextVersionDateRange = null;


            // Get Next Date Range
            if(i+1 < feedInfosCount){
                FeedInfo nextFeedInfo = feedInfos.get(i+1);
                String nextFeedVersion = getConvertedFeedVersion(nextFeedInfo);
                if(!nextFeedVersion.equalsIgnoreCase(currentFeedVersion)){
                    nextVersionDateRange =  getDateRangeForFeedInfo(nextFeedInfo);
                }
            }

            // Current Date Range
            String feedVersion = getConvertedFeedVersion(currentFeedInfo);
            currentDateRangeForVersion = datesForFeedVersion.get(feedVersion);
            if(currentDateRangeForVersion == null){
                currentDateRangeForVersion = getDateRangeForFeedInfo(currentFeedInfo);
            } else {
                currentDateRangeForVersion = getDateRangeForFeedInfo(currentDateRangeForVersion,
                                                        prevDateRangeForVersion, nextVersionDateRange);
            }
            if(currentDateRangeForVersion != null){
                datesForFeedVersion.put(feedVersion, currentDateRangeForVersion);
            }

            // Set Prev Date Range
            prevDateRangeForVersion = currentDateRangeForVersion;
        }


        return dateRanges;
    }


    private static RunTimeDateRange getDateRangeForFeedInfo(FeedInfo feedInfo){
        try {
            LocalDate feedStartDate =  Instant.ofEpochMilli(feedInfo.getFeedStartDate().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate feedEndDate =  Instant.ofEpochMilli(feedInfo.getFeedEndDate().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            return new RunTimeDateRange(feedStartDate, feedEndDate, feedInfo.getConfigRev());
        } catch (Exception e){
            return null;
        }
    }

    private static RunTimeDateRange getDateRangeForFeedInfo(RunTimeDateRange currentDateRangeForVersion,
                                                            RunTimeDateRange prevDateRangeForVersion,
                                                            RunTimeDateRange nextVersionDateRange){
        LocalDate prevStartDate = prevDateRangeForVersion.getStartDate();
        LocalDate prevEndDate = prevDateRangeForVersion.getEndDate();
        LocalDate currentStartDate = currentDateRangeForVersion.getStartDate();
        LocalDate currentEndDate = currentDateRangeForVersion.getStartDate();
        LocalDate nextVersionStartDate = nextVersionDateRange.getStartDate();
        int currentConfigRev = currentDateRangeForVersion.getConfigRev();

        if(nextVersionStartDate.isAfter(currentStartDate) && currentStartDate != null && currentEndDate != null){

            LocalDate minStartDate = currentStartDate.isBefore(prevStartDate) ? currentStartDate : prevStartDate;
            LocalDate maxEndDate = currentEndDate.isAfter(prevEndDate) ? currentStartDate : prevStartDate;
            maxEndDate = !maxEndDate.isAfter(nextVersionStartDate) ? maxEndDate : nextVersionStartDate;

            return new RunTimeDateRange(minStartDate, maxEndDate, currentConfigRev);
        }

        return prevDateRangeForVersion;
    }

    private static String getConvertedFeedVersion(FeedInfo feedInfo){
        String feedVersion = feedInfo.getFeedVersion();
        String regex = feedInfoVersionRegex.getValue();

        try{
            String[] feedVersionArray = feedVersion.split(regex);
            return buildRegexFeedVersion(feedVersionArray);
        } catch (Exception e){
            return feedVersion;
        }

    }

    private static String buildRegexFeedVersion(String[] feedVersionArray) throws Exception{
        String[] indicesConfig = feedInfoVersionIndices.toString().split(",");
        List<Integer> indices = Arrays.stream(indicesConfig)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for(Integer index : indices){
            sb.append("_");
            sb.append(feedVersionArray[index]);
        }

        return sb.toString();
    }
}
