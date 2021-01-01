package org.transitclock.api.data;


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

    public ApiDispatcher(Collection<IpcVehicle> vehicles){
        for(IpcVehicle vehicle : vehicles){
            String vehicleId = vehicle.getId();
            String lastReportTime = Time.timeStr(vehicle.getGpsTime());
            String heading = getFormattedFloat(vehicle.getHeading());
            String speed = getFormattedFloat(vehicle.getSpeed());
            String route = vehicle.getRouteShortName();
            String scheduleAdherence = getFormattedScheduleAdherence(vehicle);
            String operatorId = null;
            dispatcherData.add(new ApiDispatcherData(vehicleId, lastReportTime, heading, speed, route, scheduleAdherence, operatorId));
        }
    }

    private String getFormattedFloat(float value){
        if(Float.isNaN(value)){
            return null;
        }
        return String.format("%.10f", value);
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

    public List<ApiDispatcherData> getDispatcherData() {
        return dispatcherData;
    }
}
