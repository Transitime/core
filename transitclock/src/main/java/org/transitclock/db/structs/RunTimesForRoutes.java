package org.transitclock.db.structs;

import com.google.common.base.Objects;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.core.ServiceType;
import org.transitclock.core.dwell.DwellTimeUtil;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@DynamicUpdate
@Table(name="RunTimesForRoutes",
        indexes = { @Index(name="RunTimesForRoutesRouteNameIndex",
                        columnList="routeShortName" ),
                    @Index(name="RunTimesForRoutesServiceTypeIndex",
                        columnList="serviceType" ),
                    @Index(name="RunTimesForRoutesTripPatternIndex",
                        columnList="tripPatternId" ),
                    @Index(name="RunTimesForRoutesDirectionIdIndex",
                        columnList="directionId" )
                    } )

public class RunTimesForRoutes implements Serializable {

    @Id
    @Column
    private int configRev;

    @Column(length= HibernateUtils.DEFAULT_ID_SIZE)
    private String serviceId;

    @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
    private String directionId;

    @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
    private String routeShortName;

    @Column(length=TripPattern.TRIP_PATTERN_ID_LENGTH)
    private String tripPatternId;

    @Id
    @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
    private String tripId;

    @Column(length=TripPattern.HEADSIGN_LENGTH)
    private String headsign;

    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column
    private Integer scheduledStartTime;

    @Column
    private Integer scheduledEndTime;

    @Column
    private Integer nextTripStartTime;

    @Id
    @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
    private String vehicleId;

    @Column(length=8)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column
    private Long dwellTime;

    @Column
    private Integer startStopPathIndex;

    @Column
    private Integer actualLastStopPathIndex;

    @Column
    private Integer expectedLastStopPathIndex;

    @OneToMany(mappedBy = "runTimesForRoutes", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RunTimesForStops> runTimesForStops;

    public RunTimesForRoutes() {
    }

    public RunTimesForRoutes(int configRev,
                             String serviceId,
                             String directionId,
                             String routeShortName,
                             String tripPatternId,
                             String tripId,
                             String headsign,
                             Date startTime,
                             Date endTime,
                             Integer scheduledStartTime,
                             Integer scheduledEndTime,
                             Integer nextTripStartTime,
                             String vehicleId,
                             ServiceType serviceType,
                             Long dwellTime,
                             Integer startStopIndex) {
        this.configRev = configRev;
        this.serviceId = serviceId;
        this.directionId = directionId;
        this.routeShortName = routeShortName;
        this.tripPatternId = tripPatternId;
        this.tripId = tripId;
        this.headsign = headsign;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.nextTripStartTime = nextTripStartTime;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.dwellTime = dwellTime;
    }

    public int getConfigRev() {
        return configRev;
    }

    public void setConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDirectionId() {
        return directionId;
    }

    public void setDirectionId(String directionId) {
        this.directionId = directionId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public void setTripPatternId(String tripPatternId) {
        this.tripPatternId = tripPatternId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(Integer scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public Integer getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(Integer scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public Integer getNextTripStartTime() {
        return nextTripStartTime;
    }

    public void setNextTripStartTime(Integer nextTripStartTime) {
        this.nextTripStartTime = nextTripStartTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public Long getDwellTime() {
        return dwellTime;
    }

    public void setDwellTime(Long dwellTime) {
        this.dwellTime = dwellTime;
    }

    public List<RunTimesForStops> getRunTimesForStops() {
        return runTimesForStops;
    }

    public void setRunTimesForStops(List<RunTimesForStops> runTimesForStops) {
        this.runTimesForStops = runTimesForStops;
    }

    public Integer getStartStopPathIndex() {
        return startStopPathIndex;
    }

    public void setStartStopPathIndex(Integer startStopIndex) {
        this.startStopPathIndex = startStopIndex;
    }

    public boolean hasFirstStop(){
        return startStopPathIndex != null && startStopPathIndex == 0;
    }

    public boolean hasLastStop() {
        return actualLastStopPathIndex != null && expectedLastStopPathIndex != null &&
            actualLastStopPathIndex == expectedLastStopPathIndex;
    }


    public Long getRunTime(){
        Long runTime = null;
        if(endTime != null && startTime != null){
            runTime = endTime.getTime() - startTime.getTime();
            if(startStopPathIndex == 0){
                Long firstStopDwellTime = DwellTimeUtil.calculateFirstStopDwellTime(scheduledStartTime, startTime.getTime());
                if(firstStopDwellTime != null){
                    runTime += firstStopDwellTime;
                }
            }
        }
        return runTime;
    }

    public boolean hasCompleteRunTime(){
        return hasFirstStop() && hasLastStop();
    }

    public boolean hasAllScheduledAndActualTimes(){
        if(getRouteShortName() != null && getEndTime() != null && getStartTime() != null &&
                getScheduledStartTime() != null && getScheduledEndTime() != null){
            return true;
        }
        return false;
    }

    public Integer getActualLastStopPathIndex() {
        return actualLastStopPathIndex;
    }

    public void setActualLastStopPathIndex(Integer actualLastStopPathIndex) {
        this.actualLastStopPathIndex = actualLastStopPathIndex;
    }

    public Integer getExpectedLastStopPathIndex() {
        return expectedLastStopPathIndex;
    }

    public void setExpectedLastStopPathIndex(Integer expectedLastStopPathIndex) {
        this.expectedLastStopPathIndex = expectedLastStopPathIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTimesForRoutes that = (RunTimesForRoutes) o;
        return configRev == that.configRev &&
                Objects.equal(serviceId, that.serviceId) &&
                Objects.equal(directionId, that.directionId) &&
                Objects.equal(routeShortName, that.routeShortName) &&
                Objects.equal(tripPatternId, that.tripPatternId) &&
                Objects.equal(tripId, that.tripId) &&
                Objects.equal(headsign, that.headsign) &&
                Objects.equal(startTime, that.startTime) &&
                Objects.equal(endTime, that.endTime) &&
                Objects.equal(scheduledStartTime, that.scheduledStartTime) &&
                Objects.equal(scheduledEndTime, that.scheduledEndTime) &&
                Objects.equal(nextTripStartTime, that.nextTripStartTime) &&
                Objects.equal(vehicleId, that.vehicleId) &&
                serviceType == that.serviceType &&
                Objects.equal(dwellTime, that.dwellTime) &&
                Objects.equal(startStopPathIndex, that.startStopPathIndex) &&
                Objects.equal(actualLastStopPathIndex, that.actualLastStopPathIndex) &&
                Objects.equal(expectedLastStopPathIndex, that.expectedLastStopPathIndex) &&
                Objects.equal(runTimesForStops, that.runTimesForStops);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configRev, serviceId, directionId, routeShortName, tripPatternId, tripId, headsign, startTime, endTime, scheduledStartTime, scheduledEndTime, nextTripStartTime, vehicleId, serviceType, dwellTime, startStopPathIndex, actualLastStopPathIndex, expectedLastStopPathIndex, runTimesForStops);
    }

    @Override
    public String toString() {
        return "RunTimesForRoutes{" +
                "configRev=" + configRev +
                ", serviceId='" + serviceId + '\'' +
                ", directionId='" + directionId + '\'' +
                ", routeShortName='" + routeShortName + '\'' +
                ", tripPatternId='" + tripPatternId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", headsign='" + headsign + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", scheduledStartTime=" + scheduledStartTime +
                ", scheduledEndTime=" + scheduledEndTime +
                ", nextTripStartTime=" + nextTripStartTime +
                ", vehicleId='" + vehicleId + '\'' +
                ", serviceType=" + serviceType +
                ", dwellTime=" + dwellTime +
                ", startStopPathIndex=" + startStopPathIndex +
                ", actualLastStopPathIndex=" + actualLastStopPathIndex +
                ", expectedLastStopPathIndex=" + expectedLastStopPathIndex +
                '}';
    }


}
