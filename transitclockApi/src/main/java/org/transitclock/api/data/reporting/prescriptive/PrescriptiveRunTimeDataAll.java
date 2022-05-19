package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.api.data.reporting.PrescriptiveRunTimeAdjustment;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class PrescriptiveRunTimeDataAll {

    @XmlElement(name = "data")
    private List<PrescriptiveRunTimeDataForPattern> dataForPatterns = new ArrayList<>();

    public PrescriptiveRunTimeDataAll() {
    }

    public PrescriptiveRunTimeDataAll(List<PrescriptiveRunTimeDataForPattern> dataForPatterns) {
        this.dataForPatterns = dataForPatterns;
    }

    public List<PrescriptiveRunTimeDataForPattern> getDataForPatterns() {
        return dataForPatterns;
    }
}
