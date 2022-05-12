package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.api.data.reporting.prescriptive.PrescriptiveRunTimeAdjustment;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimeBand;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimeBands;
import org.transitclock.ipc.data.IpcStopPath;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptiveRunTimeData {
    @XmlElement(name = "adjustments")
    private List<PrescriptiveRunTimeAdjustment> adjustments = new ArrayList<>();

    @XmlElement(name = "stop_names")
    private List<String> stopNames;

    public PrescriptiveRunTimeData(IpcPrescriptiveRunTimeBands prescriptiveRunTimeBands) {
        this. stopNames = prescriptiveRunTimeBands.getTimePoints().stream()
                                                            .map(IpcStopPath::getStopName)
                                                            .collect(Collectors.toList());

        for(IpcPrescriptiveRunTimeBand tb : prescriptiveRunTimeBands.getTimeBands()){
            PrescriptiveRunTimeAdjustment adjustment = new PrescriptiveRunTimeAdjustment();

            adjustment.setAdjustedTimes(tb.getRunTimeByStopPathId().stream()
                                                .map(IpcPrescriptiveRunTime::getAdjustment)
                                                .mapToLong(Double::doubleToRawLongBits)
                                                .boxed()
                                                .collect(Collectors.toList()));
            adjustment.setOriginalTimes(tb.getRunTimeByStopPathId().stream()
                                                .map(IpcPrescriptiveRunTime::getAdjustment)
                                                .mapToLong(Double::doubleToRawLongBits)
                                                .boxed()
                                                .collect(Collectors.toList()));

            adjustment.setFromTime(tb.getStartTime());
            adjustment.setToTime(tb.getEndTime());
        }
    }

    public List<PrescriptiveRunTimeAdjustment> getAdjustments() {
        return adjustments;
    }

    public List<String> getStopNames() {
        return stopNames;
    }
}
