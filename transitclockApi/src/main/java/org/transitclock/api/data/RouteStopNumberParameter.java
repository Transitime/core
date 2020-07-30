package org.transitclock.api.data;

/**
 * Represents a fragment of a parameter for Stop level API
 * in /command/stopLevel
 */
public class RouteStopNumberParameter {
    private String stopId;
    private String route;
    private String numberOfServices = "3";
    private Integer isValid = null;
    private String errorDescription = null;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String id) {
        stopId = id;
    }

    public String getRoute() {
        return route;
    }
    public void setRoute(String r) {
        route = r;
    }

    public String getNumberOfServices() {
        return numberOfServices;
    }

    public int getNumberOfServicesAsInt() {
        try {
            return Integer.parseInt(numberOfServices);
        } catch (NumberFormatException nfe) {
            return 3;
        }
    }
    public void setNumberOfServices(String number) {
        numberOfServices = number;
    }

    public Integer getValidCode() {
        return isValid;
    }

    public void setInvalid() {
        isValid = 0;
        numberOfServices = null;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String msg) {
        errorDescription = msg;
    }

    public boolean isInvalid() {
        return isValid != null && isValid == 0;
    }

}
