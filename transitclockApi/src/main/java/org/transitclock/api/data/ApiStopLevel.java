package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcPrediction;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing StopLevel Prediction data per the
 * /coammand/StopLevels API
 */
@XmlRootElement
public class ApiStopLevel {
    @XmlElement(name = "stopId")
    private String stopId;
    @XmlElement(name = "route")
    private String route; //routeShortName
    @XmlElement(name = "number_of_services")
    private String numberOfServices = "3";
    @XmlElement(name = "is_valid")
    private Integer isValid = null;
    @XmlElement(name = "error_desc")
    private String errorDescription;
    @XmlElement(name = "predictions")
    private ArrayList<ApiStopLevelPrediction> predictions = new ArrayList<ApiStopLevelPrediction>();

    private int services = 3;

    // needed for serialization
    protected ApiStopLevel() {

    }

    /**
     * Error constructor -- only use this if no predictions will be present.
     * @param rsn containing the errorDescription
     */
    public ApiStopLevel(RouteStopNumberParameter rsn) {
        isValid = 0;
        errorDescription = rsn.getErrorDescription();
    }

    /**
     * Typical contructor used to supply the input query and the resulting
     * predictions
     * @param rsn
     * @param routeStopPredictions
     */
    public ApiStopLevel(RouteStopNumberParameter rsn, List<IpcPredictionsForRouteStopDest> routeStopPredictions) {
        stopId = rsn.getStopId();
        route = rsn.getRoute();

        try {
            numberOfServices = rsn.getNumberOfServices();
            services = Integer.parseInt(numberOfServices);
        } catch (NumberFormatException nfe) {
            services = 3;
            // we just ignore an invalide service count
        }
        isValid = rsn.getValidCode();
        errorDescription = rsn.getErrorDescription();
        int servicesCount = 0;
        for (IpcPredictionsForRouteStopDest routeStopPrediction : routeStopPredictions) {
            if (routeStopPrediction.getPredictionsForRouteStop() != null) {
                for (IpcPrediction ipc : routeStopPrediction.getPredictionsForRouteStop()) {
                    servicesCount++;
                    predictions.add(new ApiStopLevelPrediction(ipc));
                    if (servicesCount >= rsn.getNumberOfServicesAsInt()) {
                        return;
                    }
                }
            }
        }
    }

}
