package org.transitclock.core.headwaygenerator;

import org.transitclock.configData.HeadwayConfig;
import org.transitclock.core.HeadwayGenerator;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.db.structs.Headway;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcVehicleComplete;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractHeadwayGenerator implements HeadwayGenerator {
    @Override
    public abstract Headway generate(VehicleState vehicleState);

    public List<IpcArrivalDeparture> getRecentArrivalDeparturesForStop(String stopId, long vehicleMatchAvlTime){
        StopArrivalDepartureCacheKey key=new StopArrivalDepartureCacheKey(stopId, new Date(vehicleMatchAvlTime));

        return StopArrivalDepartureCacheFactory.getInstance().getStopHistory(key);
    }

    public int[] getLastStopAndPrevVehicleArrivalDepartureIndex(VehicleState vehicleState,
                                                        String vehicleId,
                                                        String stopId,
                                                        List<IpcArrivalDeparture> arrivalDeparturesForStop,
                                                        boolean useArrival){
        int lastStopArrivalIndex = -1;
        int previousVehicleArrivalIndex = -1;

        for(int i=0;i<arrivalDeparturesForStop.size() && previousVehicleArrivalIndex==-1 ;i++)
        {
            IpcArrivalDeparture arrivalDepature = arrivalDeparturesForStop.get(i);
            boolean correctArrivalOrDeparture = useArrival ? arrivalDepature.isArrival() : arrivalDepature.isDeparture();

            if(correctArrivalOrDeparture &&
                    arrivalDepature.getStopId().equals(stopId) &&
                    arrivalDepature.getVehicleId().equals(vehicleId) &&
                    (vehicleState.getTrip().getDirectionId()==null ||
                            vehicleState.getTrip().getDirectionId().equals(arrivalDepature.getDirectionId())))
            {
                // This the arrival of this vehicle now the next arrival in the list will be the previous vehicle (The arrival of the vehicle ahead).
                lastStopArrivalIndex=i;
            }

            if(lastStopArrivalIndex>-1 &&
                    correctArrivalOrDeparture &&
                    arrivalDepature.getStopId().equals(stopId) &&
                    !arrivalDepature.getVehicleId().equals(vehicleId) &&
                    (!HeadwayConfig.matchByTripPattern() ||
                            arrivalDepature.getTripPatternId().equals(vehicleState.getTrip().getTripPattern().getId())) &&
                    (vehicleState.getTrip().getDirectionId()==null ||
                            vehicleState.getTrip().getDirectionId().equals(arrivalDepature.getDirectionId())))
            {
                previousVehicleArrivalIndex = i;
            }
        }
        return new int[]{lastStopArrivalIndex, previousVehicleArrivalIndex};
    }

    public static long calculateHeadway(IpcArrivalDeparture prevStopArrival, IpcArrivalDeparture prevStopArrivalForPrevVehicle){
        return Math.abs(prevStopArrival.getTime().getTime() - prevStopArrivalForPrevVehicle.getTime().getTime());
    }

    public static Long calculateScheduledHeadway(IpcArrivalDeparture prevStopArrival,
                                                 IpcArrivalDeparture prevStopArrivalForPrevVehicle) {
        if(prevStopArrival !=null && prevStopArrival.getScheduledDate() != null &&
                prevStopArrivalForPrevVehicle != null && prevStopArrivalForPrevVehicle.getScheduledDate() != null){
            return Math.abs(prevStopArrival.getScheduledDate().getTime() - prevStopArrivalForPrevVehicle.getScheduledDate().getTime());
        }
        return null;
    }

    public void setSystemVariance(Headway headway)
    {
        ArrayList<Headway> headways=new ArrayList<Headway>();

        int total_with_headway=0;
        int total_vehicles=0;
        boolean error=false;
        for(IpcVehicleComplete currentVehicle: VehicleDataCache.getInstance().getVehicles())
        {
            VehicleState vehicleState = VehicleStateManager.getInstance().getVehicleState(currentVehicle.getId());
            if(vehicleState.getHeadway()!=null)
            {
                headways.add(vehicleState.getHeadway());
                total_with_headway++;
            }
            total_vehicles++;
        }
        // ONLY SET IF HAVE VALES FOR ALL VEHICLES ON ROUTE.
        if(VehicleDataCache.getInstance().getVehicles().size()==headways.size()&&total_vehicles==total_with_headway)
        {
            headway.setAverage(average(headways));
            headway.setVariance(variance(headways));
            headway.setCoefficientOfVariation(coefficientOfVariance(headways));
            headway.setNumVehicles(headways.size());
        }else
        {
            headway.setAverage(-1);
            headway.setVariance(-1);
            headway.setCoefficientOfVariation(-1);
            headway.setNumVehicles(total_with_headway);
        }
    }

    private double average(List<Headway> headways)
    {
        double total=0;
        for(Headway headway:headways)
        {
            total=total+headway.getHeadway();
        }
        return total/headways.size();
    }

    private double variance(List<Headway> headways)
    {
        double topline=0;
        double average = average(headways);
        for(Headway headway:headways)
        {
            topline=topline+((headway.getHeadway()-average)*(headway.getHeadway()-average));
        }
        return topline/headways.size();
    }

    private double coefficientOfVariance(List<Headway> headways)
    {
        double variance = variance(headways);;
        double average = average(headways);

        return variance/(average*average);
    }

}
