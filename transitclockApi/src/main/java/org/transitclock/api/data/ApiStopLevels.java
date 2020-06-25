package org.transitclock.api.data;

import org.transitclock.api.utils.StandardParameters;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitclock.ipc.interfaces.PredictionsInterface;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing aggregation of StopLevel Prediction data per the
 * /coammand/StopLevels API
 */

@XmlRootElement
public class ApiStopLevels {

    @XmlElement(name = "stop_level")
    private ArrayList<ApiStopLevel> stopLevels = new ArrayList<>();

    // needed for serialzation
    protected ApiStopLevels() {

    }

    /**
     * Public constructor that passed through prediction interfaces and query parameters
     * via the StandardParmeters object.  Typical errors are caught and reported via
     * the errorDescription field in child objects.
     * @param stdParameters
     */
    public ApiStopLevels(StandardParameters stdParameters) {
        List<RouteStopNumberParameter> queries = parseQuery(stdParameters.getRequest().getParameter("rs"));

        for (RouteStopNumberParameter rsn : queries) {
            if (rsn.isInvalid()) {
                // invalid query -- no need to try and retrieve prediction
                ApiStopLevel asl = new ApiStopLevel(rsn);
            } else {
                try {
                    PredictionsInterface.RouteStop routeStop = new PredictionsInterface.RouteStop(rsn.getRoute(), rsn.getStopId());
                    List<PredictionsInterface.RouteStop> routeStopList = new ArrayList<>();
                    routeStopList.add(routeStop);
                    List<IpcPredictionsForRouteStopDest> ipcPredictionsForRouteStopDests = stdParameters.getPredictionsInterface().get(routeStopList, rsn.getNumberOfServicesAsInt());
                    stopLevels.add(new ApiStopLevel(rsn, ipcPredictionsForRouteStopDests));
                } catch (Exception any) {
                    rsn.setInvalid();
                    rsn.setErrorDescription(any.toString());
                    stopLevels.add(new ApiStopLevel(rsn));                }
            }
        }
    }

    /**
     * Break the user request in a list of RouteStopNumberParameters.
     * @param query
     * @return
     */
    private List<RouteStopNumberParameter> parseQuery(String query) {
        // query format:  stopId|route|number_of_services{:stopId|route|number_of_services}
        ArrayList<RouteStopNumberParameter> queries = new ArrayList<RouteStopNumberParameter>();


        int start = 0;
        int delim = query.indexOf(":", start);
        while (delim > 0) {
            String subQuery = query.substring(start, delim);
            RouteStopNumberParameter rsn = parseFragment(subQuery);
            queries.add(rsn);
            start = delim;
            start++; // advance start past delim
            delim = query.indexOf(":", start);
        }

        String lastQuery = query.substring(start);
        RouteStopNumberParameter rsn = parseFragment(lastQuery);
        queries.add(rsn);


        return queries;
    }

    /**
     * Break a fragment of the query into a single RouteStopNumberParemeter
     * or pupulate errorDescription if the syntax is invald.
     * @param query
     * @return
     */
    private RouteStopNumberParameter parseFragment(String query) {
        RouteStopNumberParameter rsn = new RouteStopNumberParameter();

        if (query == null || query.length() == 0) {
            rsn.setInvalid();
            rsn.setErrorDescription("Missing parameter");
            return rsn;
        }

        int firstDelim = query.indexOf("|");
        if (firstDelim < 0) {
            rsn.setStopId(query);
            return rsn;
        }
        int secondDelim = query.indexOf("|", firstDelim+1);
        if (secondDelim < 0) {
            rsn.setStopId(query.substring(0, firstDelim));
            rsn.setRoute(query.substring(firstDelim+1));
            return rsn;
        }
        rsn.setStopId(query.substring(0, firstDelim));
        rsn.setRoute(query.substring(firstDelim+1, secondDelim));
        rsn.setNumberOfServices(query.substring(secondDelim+1));
        return rsn;
    }

    public static void main(String[] argsv) {
        // unit test of parameter parsing
        RouteStopNumberParameter rsn;
        ApiStopLevels asl = new ApiStopLevels();
        List<RouteStopNumberParameter> rsns;

        String p = null;
        rsn = asl.parseFragment(p);
        testAssert(rsn != null);
        testAssert(rsn.isInvalid());
        testAssert(rsn.getNumberOfServices() == null);

        String p1 = "";
        rsn = asl.parseFragment(p1);
        testAssert(rsn != null);
        testAssert(rsn.isInvalid() == true);
        testAssert(rsn.getRoute() == null);
        testAssert(rsn.getNumberOfServices() == null);

        String p2 = "stop1";
        rsn = asl.parseFragment(p2);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals(p2));
        testAssert(rsn.getRoute() ==  null);
        testAssert(rsn.getNumberOfServices().equals("3"));

        String p3 = "stop1|routeShortName2";
        rsn = asl.parseFragment(p3);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop1"));
        testAssert(rsn.getRoute().equals("routeShortName2"));
        testAssert(rsn.getNumberOfServices().equals("3"));

        String p4 = "stop1|routeShortName2|4";
        rsn = asl.parseFragment(p4);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop1"));
        testAssert(rsn.getRoute().equals("routeShortName2"));
        testAssert(rsn.getNumberOfServices().equals("4"));

        String p5 = "stop1|routeShortName2|4";
        rsns = asl.parseQuery(p5);
        testAssert(rsns.size() == 1);
        rsn = asl.parseQuery(p5).get(0);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop1"));
        testAssert(rsn.getRoute().equals("routeShortName2"));
        testAssert(rsn.getNumberOfServices().equals("4"));

        String p6 = "stop1|routeShortName2|4:";
        rsns = asl.parseQuery(p6);
        testAssert(rsns.size() == 2);
        rsn = rsns.get(0);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop1"));
        testAssert(rsn.getRoute().equals("routeShortName2"));
        testAssert(rsn.getNumberOfServices().equals("4"));
        rsn = rsns.get(1);
        testAssert(rsn != null);
        testAssert(rsn.isInvalid() == true);
        testAssert(rsn.getRoute() == null);
        testAssert(rsn.getNumberOfServices() == null);

        String p7 = "stop1|routeShortName2|4:stop2|routeShortName3|5:";
        rsns = asl.parseQuery(p7);
        testAssert(rsns.size() == 3);
        rsn = rsns.get(0);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop1"));
        testAssert(rsn.getRoute().equals("routeShortName2"));
        testAssert(rsn.getNumberOfServices().equals("4"));
        rsn = rsns.get(1);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop2"));
        testAssert(rsn.getRoute().equals("routeShortName3"));
        testAssert(rsn.getNumberOfServices().equals("5"));
        rsn = rsns.get(2);
        testAssert(rsn != null);
        testAssert(rsn.isInvalid() == true);
        testAssert(rsn.getRoute() == null);
        testAssert(rsn.getNumberOfServices() == null);

        String p8 = "stop1|routeShortName2|4:stop2|routeShortName3|5:stop3:";
        rsns = asl.parseQuery(p8);
        testAssert(rsns.size() == 4);
        rsn = rsns.get(0);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop1"));
        testAssert(rsn.getRoute().equals("routeShortName2"));
        testAssert(rsn.getNumberOfServices().equals("4"));
        rsn = rsns.get(1);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop2"));
        testAssert(rsn.getRoute().equals("routeShortName3"));
        testAssert(rsn.getNumberOfServices().equals("5"));

        rsn = rsns.get(2);
        testAssert(rsn.isInvalid() == false);
        testAssert(rsn.getStopId().equals("stop3"));
        testAssert(rsn.getRoute() == null);
        testAssert(rsn.getNumberOfServices().equals("3"));

        rsn = rsns.get(3);
        testAssert(rsn != null);
        testAssert(rsn.isInvalid() == true);
        testAssert(rsn.getRoute() == null);
        testAssert(rsn.getNumberOfServices() == null);

    }

    private static void testAssert(boolean flag) {
        if (!flag) {
            throw new AssertionError("fail");
        }
    }


}
