package org.transitclock.api.data.reporting;

import org.transitclock.api.data.reporting.chartjs.pie.PieChart;
import org.transitclock.api.data.reporting.chartjs.pie.PieChartData;
import org.transitclock.api.data.reporting.chartjs.pie.PieChartDataset;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves on-time performance data for reporting
 *
 * @author carabalb
 *
 */
public class OnTimePerformanceData {

    private static final String EARLY = "Early";
    private static final String LATE = "Late";
    private static final String ON_TIME = "On Time";

    public PieChart getOnTimePerformanceForRoutesPieChart(List<IpcArrivalDeparture> arrivalDepartures,
                                                  int minEarlySec, int minLateSec){

        Map<String, Integer> labelsAndCountsMap = getOTPLabelsAndCounts(arrivalDepartures, minEarlySec, minLateSec);

        PieChartDataset dataset = new PieChartDataset();
        dataset.addData(labelsAndCountsMap.get(EARLY), labelsAndCountsMap.get(LATE), labelsAndCountsMap.get(ON_TIME));

        PieChartData data = new PieChartData();
        data.addDataset(dataset);
        data.addLabels(EARLY, LATE, ON_TIME);

        PieChart chart = new PieChart();
        chart.setData(data);

        return chart;
    }

    private Map<String, Integer> getOTPLabelsAndCounts(List<IpcArrivalDeparture> arrivalDepartures,
                                                       int minEarlySec, int minLateSec){

        Map<String, Integer> otpLabelsAndCountsMap = new LinkedHashMap<>();
        otpLabelsAndCountsMap.put(EARLY, 0);
        otpLabelsAndCountsMap.put(LATE, 0);
        otpLabelsAndCountsMap.put(ON_TIME, 0);

        int count;

        for(IpcArrivalDeparture ad : arrivalDepartures) {
            if(ad.getScheduledAdherence().isEarlierThan(minEarlySec)){
                count = otpLabelsAndCountsMap.get(EARLY);
                otpLabelsAndCountsMap.put(EARLY, count + 1);
            } else if(ad.getScheduledAdherence().isLaterThan(minLateSec)){
                count = otpLabelsAndCountsMap.get(LATE);
                otpLabelsAndCountsMap.put(LATE, count + 1);
            } else {
                count = otpLabelsAndCountsMap.get(ON_TIME);
                otpLabelsAndCountsMap.put(ON_TIME, count + 1);
            }
        }

        return otpLabelsAndCountsMap;
    }
}
