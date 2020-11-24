package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcRunTime;
import org.transitclock.utils.MathUtils;
import javax.xml.bind.annotation.XmlAttribute;

public class ApiRunTimeSummary{
    @XmlAttribute(name = "avgRunTime")
    private Double avgRunTime;

    @XmlAttribute(name = "fixed")
    private Double fixed;

    @XmlAttribute(name = "variable")
    private Double variable;

    @XmlAttribute(name = "dwell")
    private Double dwell;

    public ApiRunTimeSummary(){}

    public ApiRunTimeSummary(IpcRunTime ipcRunTime){
        this.avgRunTime = ipcRunTime.getAvgRunTime() != null ? MathUtils.round(ipcRunTime.getAvgRunTime(), 1) : null;
        this.fixed = ipcRunTime.getFixed() != null ? MathUtils.round(ipcRunTime.getFixed(), 1) : null;
        this.variable = ipcRunTime.getVariable() != null ? MathUtils.round(ipcRunTime.getVariable(), 1) : null;
        this.dwell = ipcRunTime.getDwell() != null ? MathUtils.round(ipcRunTime.getDwell(), 1) : null;
    }
}
