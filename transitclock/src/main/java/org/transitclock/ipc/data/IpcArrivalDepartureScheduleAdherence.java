package org.transitclock.ipc.data;

import org.transitclock.core.TemporalDifference;
import org.transitclock.db.structs.ArrivalDeparture;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

public class IpcArrivalDepartureScheduleAdherence extends IpcArrivalDeparture {

    @XmlAttribute
    private TemporalDifference scheduledAdherence;

    @XmlAttribute
    private boolean isTimePoint;

    private IpcArrivalDepartureScheduleAdherence(){
        super();
    }

    public IpcArrivalDepartureScheduleAdherence(ArrivalDeparture arrivalDepature) throws Exception {
        super(arrivalDepature);
        this.scheduledAdherence = arrivalDepature.getScheduleAdherence();
    }

    @Override
    public TemporalDifference getScheduledAdherence() {
        return scheduledAdherence;
    }

    @Override
    public void setScheduledAdherence(TemporalDifference scheduledAdherence) {
        this.scheduledAdherence = scheduledAdherence;
    }

    public boolean isTimePoint() {
        return isTimePoint;
    }

    public void setTimePoint(boolean timePoint) {
        isTimePoint = timePoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IpcArrivalDepartureScheduleAdherence that = (IpcArrivalDepartureScheduleAdherence) o;
        return isTimePoint == that.isTimePoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isTimePoint);
    }
}
