package org.transitclock.api.data.reporting;

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
}
