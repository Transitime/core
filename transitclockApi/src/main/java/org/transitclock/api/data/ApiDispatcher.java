package org.transitclock.api.data;


import org.transitclock.api.utils.MathUtils;
import org.transitclock.api.utils.NumberFormatter;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.utils.Time;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApiDispatcher {

    @XmlElement(name="data")
    private List<ApiDispatcherData> dispatcherData = new ArrayList<>();

    public ApiDispatcher() {}

    public ApiDispatcher(Collection<IpcVehicle> vehicles, SpeedFormat speedFormat){
        for(IpcVehicle vehicle : vehicles){
            String vehicleId = vehicle.getId();
            String lastReportTime = Time.timeStr(vehicle.getGpsTime());
            String blockId = vehicle.getBlockId();
            //String heading = NumberFormatter.getRoundedValueAsString(vehicle.getHeading(), 1);
            String speed = getFormattedSpeed(vehicle.getSpeed(), speedFormat);
            String route = vehicle.getRouteShortName();
            String scheduleAdherence = getFormattedScheduleAdherence(vehicle);
            Integer scheduleAdherenceTimeDiff = getScheduleAdherenceTimeDiff(vehicle);
            String operatorId = null;
            dispatcherData.add(new ApiDispatcherData(vehicleId, lastReportTime, blockId, speed, route,
                    scheduleAdherence, scheduleAdherenceTimeDiff, operatorId));
        }
    }

    private String getFormattedSpeed(float value, SpeedFormat speedFormat){
        if(Float.isNaN(value)){
            return null;
        }
        return NumberFormatter.getRoundedValueAsString(MathUtils.convertSpeed(value, speedFormat), 1);
    }

    private String getFormattedScheduleAdherence(IpcVehicle vehicle){
        try {
            if (vehicle.getRealTimeSchedAdh() != null) {
                return vehicle.getRealTimeSchedAdh().toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Integer getScheduleAdherenceTimeDiff(IpcVehicle vehicle){
        try {
            if (vehicle.getRealTimeSchedAdh() != null) {
                return vehicle.getRealTimeSchedAdh().getTemporalDifference();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<ApiDispatcherData> getDispatcherData() {
        return dispatcherData;
    }
}
