package org.transitclock.api.data.reporting;

import org.transitclock.api.data.ApiRunTimeSummary;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class PrescriptiveRunTimeData {
    @XmlElement(name = "adjustments")
    private List<PrescriptiveRunTimeAdjustment> adjustments = new ArrayList<>();

    @XmlElement(name = "current_otp")
    private String currentOtp;

    @XmlElement(name = "expected_otp")
    private String expectedOtp;

    @XmlElement(name = "summary")
    private ApiRunTimeSummary summary;

    public List<PrescriptiveRunTimeAdjustment> getAdjustments() {
        return adjustments;
    }

    public String getCurrentOtp() {
        return currentOtp;
    }

    public void setCurrentOtp(String currentOtp) {
        this.currentOtp = currentOtp;
    }

    public String getExpectedOtp() {
        return expectedOtp;
    }

    public void setExpectedOtp(String expectedOtp) {
        this.expectedOtp = expectedOtp;
    }

    public ApiRunTimeSummary getSummary() {
        return summary;
    }

    public void setSummary(ApiRunTimeSummary summary) {
        this.summary = summary;
    }
}
