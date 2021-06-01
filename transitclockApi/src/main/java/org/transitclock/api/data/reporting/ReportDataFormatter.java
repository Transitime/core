package org.transitclock.api.data.reporting;

import java.text.NumberFormat;
import java.util.Locale;

public class ReportDataFormatter {
    public static String formatStopPath(String stopPath){
        return stopPath.replaceAll("_", " ");
    }

    public static String formatValueAsPercent(Double value){
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        return numberFormat.format(value);
    }
}
