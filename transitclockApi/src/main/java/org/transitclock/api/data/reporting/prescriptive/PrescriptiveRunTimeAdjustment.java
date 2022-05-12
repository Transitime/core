package org.transitclock.api.data.reporting.prescriptive;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;


public class PrescriptiveRunTimeAdjustment {
    @XmlElement(name = "fromTime")
    private String fromTime;

    @XmlElement(name = "toTime")
    private String toTime;

    @XmlElement(name = "current_otp")
    private String currentOtp;

    @XmlElement(name = "expected_otp")
    private String expectedOtp;

    @XmlElement(name = "adjusted_times")
    private List<Long> adjustedTimes;

    @XmlElement(name = "original_times")
    private List<Long> originalTimes;

    @XmlElement(name = "total_adjusted")
    private Long totalAdjusted;

    @XmlElement(name = "total_original")
    private Long totalOriginal;

    public PrescriptiveRunTimeAdjustment() { }

    public PrescriptiveRunTimeAdjustment(String fromTime,
                                         String toTime,
                                         String currentOtp,
                                         String expectedOtp,
                                         List<Long> adjustedTimes,
                                         List<Long> originalTimes) {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.currentOtp = currentOtp;
        this.expectedOtp = expectedOtp;
        this.adjustedTimes = adjustedTimes;
        this.originalTimes = originalTimes;

        this.totalAdjusted = adjustedTimes.stream().mapToLong(Long::longValue).sum();
        this.totalOriginal = adjustedTimes.stream().mapToLong(Long::longValue).sum();

    }


    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
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

    public List<Long> getAdjustedTimes() {
        return adjustedTimes;
    }

    public void setAdjustedTimes(List<Long> adjustedTimes) {
        this.adjustedTimes = adjustedTimes;
    }

    public List<Long> getOriginalTimes() {
        return originalTimes;
    }

    public void setOriginalTimes(List<Long> originalTimes) {
        this.originalTimes = originalTimes;
    }

    public Long getTotalAdjusted() {
        return totalAdjusted;
    }

    public Long getTotalOriginal() {
        return totalOriginal;
    }
}
