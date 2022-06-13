package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForTimeBand;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForTimeBands;
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

    public PrescriptiveRunTimeDataForPattern(IpcPrescriptiveRunTimesForTimeBands prescriptiveRunTimeBands) {
        this.stopNames = prescriptiveRunTimeBands.getTimePoints().stream()
                                                            .map(IpcStopPath::getStopName)
                                                            .collect(Collectors.toList());

        for(IpcPrescriptiveRunTimesForTimeBand tb : prescriptiveRunTimeBands.getRunTimesForTimeBands()){
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
