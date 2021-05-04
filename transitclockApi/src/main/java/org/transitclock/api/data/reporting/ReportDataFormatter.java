package org.transitclock.api.data.reporting;

public class ReportDataFormatter {
    public static String formatStopPath(String stopPath){
        return stopPath.replaceAll("_", " ");
    }
}
