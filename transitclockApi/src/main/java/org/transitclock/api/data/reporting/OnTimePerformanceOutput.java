package org.transitclock.api.data.reporting;

import org.transitclock.api.data.reporting.chartjs.pie.PieChart;
import org.transitclock.api.data.reporting.chartjs.pie.PieChartData;
import org.transitclock.api.data.reporting.chartjs.pie.PieChartDataset;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDepartureScheduleAdherence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves on-time performance data for reporting
 *
 * @author carabalb
 *
 */
public class OnTimePerformanceOutput {

    private static final String EARLY = "Early";
    private static final String LATE = "Late";
    private static final String ON_TIME = "On-Time";

    public static PieChart getOnTimePerformanceForRoutesPieChart(List<IpcArrivalDepartureScheduleAdherence> arrivalDepartures,
                                                                 int minEarlyMSec, int minLateMSec){

        Map<String, Integer> labelsAndCountsMap = getOTPLabelsAndCounts(arrivalDepartures, minEarlyMSec/1000, minLateMSec/1000);

        PieChartDataset dataset = new PieChartDataset();
        dataset.addData(labelsAndCountsMap.get(EARLY), labelsAndCountsMap.get(LATE), labelsAndCountsMap.get(ON_TIME));

        PieChartData data = new PieChartData();
        data.addDataset(dataset);
        data.addLabels(EARLY, LATE, ON_TIME);

        org.transitclock.api.data.reporting.chartjs.pie.PieChart chart = new org.transitclock.api.data.reporting.chartjs.pie.PieChart();
        chart.setData(data);

        return chart;
    }

    private static Map<String, Integer> getOTPLabelsAndCounts(List<IpcArrivalDepartureScheduleAdherence> arrivalDepartures,
                                                       int minEarlySec, int minLateSec){

        Map<String, Integer> otpLabelsAndCountsMap = new LinkedHashMap<>();
        otpLabelsAndCountsMap.put(EARLY, 0);
        otpLabelsAndCountsMap.put(LATE, 0);
        otpLabelsAndCountsMap.put(ON_TIME, 0);

        int earlyCount;
        int lateCount;
        int onTimeCount;

        for(IpcArrivalDeparture ad : arrivalDepartures) {
            if(ad.isArrival()){
                if (ad.getScheduledAdherence().isLaterThan(minLateSec)) {
                    lateCount = otpLabelsAndCountsMap.get(LATE);
                    otpLabelsAndCountsMap.put(LATE, lateCount + 1);
                } else {
                    onTimeCount = otpLabelsAndCountsMap.get(ON_TIME);
                    otpLabelsAndCountsMap.put(ON_TIME, onTimeCount + 1);
                }
            } else {
                if (ad.getScheduledAdherence().isEarlierThan(minEarlySec)) {
                    earlyCount = otpLabelsAndCountsMap.get(EARLY);
                    otpLabelsAndCountsMap.put(EARLY, earlyCount + 1);
                } else {
                    onTimeCount = otpLabelsAndCountsMap.get(ON_TIME);
                    otpLabelsAndCountsMap.put(ON_TIME, onTimeCount + 1);
                }
            }

        }

        return otpLabelsAndCountsMap;
    }
}
