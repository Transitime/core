package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcDirection;
import org.transitclock.ipc.data.IpcDirectionsForRoute;
import org.transitclock.ipc.data.IpcStop;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@XmlRootElement
public class ApiStopsForRoute {
    @XmlElement
    List<ApiStopSimple> stops;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiStopsForRoute(){}

    public ApiStopsForRoute(List<IpcDirectionsForRoute> stopsForRoutes) {
        Set<ApiStopSimple> stopsSet = new LinkedHashSet<>();
        for(IpcDirectionsForRoute directionsForRoute : stopsForRoutes){
            for(IpcDirection  direction : directionsForRoute.getDirections()){
                for(IpcStop stop: direction.getStops()){
                    stopsSet.add(new ApiStopSimple(stop));
                }
            }
        }
        stops = new ArrayList<>(stopsSet);
    }
}
