package org.transitclock.api.data.reporting.prescriptive;

import org.transitclock.ipc.data.IpcPrescriptiveRunTimeBand;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.stream.Collectors;


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

    public PrescriptiveRunTimeAdjustment(IpcPrescriptiveRunTimeBand timeBand){

        this.adjustedTimes = timeBand.getRunTimeByStopPathId().stream()
                .map(rt -> (long) (rt.getScheduled() + rt.getAdjustment()))
                .collect(Collectors.toList());

        this.originalTimes = timeBand.getRunTimeByStopPathId().stream()
                .map(rt -> (long) rt.getScheduled())
                .collect(Collectors.toList());


        this.fromTime = timeBand.getStartTime();
        this.toTime = timeBand.getEndTime();

        this.totalOriginal = originalTimes.stream().mapToLong(Long::longValue).sum();
        this.totalAdjusted = adjustedTimes.stream().mapToLong(Long::longValue).sum();

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
