package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimeBand;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimeBands;
import org.transitclock.ipc.data.IpcStopPath;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptiveRunTimeDataForPattern {
    @XmlElement(name = "adjustments")
    private List<PrescriptiveRunTimeAdjustment> adjustments = new ArrayList<>();

    @XmlElement(name = "stop_names")
    private List<String> stopNames;

    public PrescriptiveRunTimeDataForPattern() {}

    public PrescriptiveRunTimeDataForPattern(IpcPrescriptiveRunTimeBands prescriptiveRunTimeBands) {
        this. stopNames = prescriptiveRunTimeBands.getTimePoints().stream()
                                                            .map(IpcStopPath::getStopName)
                                                            .collect(Collectors.toList());

        for(IpcPrescriptiveRunTimeBand tb : prescriptiveRunTimeBands.getTimeBands()){
            adjustments.add(new PrescriptiveRunTimeAdjustment(tb));
        }
    }

    public List<PrescriptiveRunTimeAdjustment> getAdjustments() {
        return adjustments;
    }

    public List<String> getStopNames() {
        return stopNames;
    }
}
