
    alter table Block_to_Trip_joinTable 
        drop constraint FK_abaj8ke6oh4imbbgnaercsowo;

    alter table Block_to_Trip_joinTable 
        drop constraint FK_1c1e1twdap19vq0xkav0amvm;

    alter table TravelTimesForTrip_to_TravelTimesForPath_joinTable 
        drop constraint FK_hh5uepurijcqj0pyc6e3h5mqw;

    alter table TravelTimesForTrip_to_TravelTimesForPath_joinTable 
        drop constraint FK_9j1s8ewsmokqg4m35wrr29na7;

    alter table TripPattern_to_Path_joinTable 
        drop constraint FK_s0gaw8iv60vc17a5ltryqwg27;

    alter table TripPattern_to_Path_joinTable 
        drop constraint FK_qsr8l6u1nelb5pt8rlnei08sy;

    alter table Trips 
        drop constraint FK_p1er53449kkfsca6mbnxkdyst;

    alter table Trips 
        drop constraint FK_676npp7h4bxh8sjcnugnxt5wb;

    drop table if exists Agencies cascade;

    drop table if exists ArrivalsDepartures cascade;

    drop table if exists AvlReports cascade;

    drop table if exists Block_to_Trip_joinTable cascade;

    drop table if exists Blocks cascade;

    drop table if exists CalendarDates cascade;

    drop table if exists Calendars cascade;

    drop table if exists FareAttributes cascade;

    drop table if exists FareRules cascade;

    drop table if exists Frequencies cascade;

    drop table if exists Matches cascade;

    drop table if exists Predictions cascade;

    drop table if exists Routes cascade;

    drop table if exists StopPaths cascade;

    drop table if exists Stops cascade;

    drop table if exists Transfers cascade;

    drop table if exists TravelTimesForStopPath cascade;

    drop table if exists TravelTimesForTrip_to_TravelTimesForPath_joinTable cascade;

    drop table if exists TravelTimesForTrips cascade;

    drop table if exists TripPattern_to_Path_joinTable cascade;

    drop table if exists TripPatterns cascade;

    drop table if exists Trips cascade;

    drop sequence hibernate_sequence;

    create table Agencies (
        configRev integer not null,
        agencyId varchar(60) not null,
        agencyFareUrl varchar(255),
        agencyLang varchar(15),
        agencyName varchar(60),
        agencyPhone varchar(15),
        agencyTimezone varchar(40),
        agencyUrl varchar(255),
        maxLat double precision,
        maxLon double precision,
        minLat double precision,
        minLon double precision,
        primary key (configRev, agencyId)
    );

    create table ArrivalsDepartures (
        DTYPE varchar(31) not null,
        vehicleId varchar(60) not null,
        tripStartTime integer not null,
        tripId varchar(60) not null,
        time datetime not null,
        stopSequence integer not null,
        stopId varchar(60) not null,
        isArrival boolean not null,
        avlTime datetime,
        blockId varchar(60),
        configRev integer,
        pathIndex integer,
        routeId varchar(60),
        routeShortName varchar(60),
        scheduledTime datetime,
        serviceId varchar(60),
        tripIndex integer,
        primary key (vehicleId, tripStartTime, tripId, time, stopSequence, stopId, isArrival)
    );

    create table AvlReports (
        vehicleId varchar(60) not null,
        time datetime(3) not null,
        assignmentId varchar(60),
        assignmentType varchar(40),
        driverId varchar(60),
        heading float,
        licensePlate varchar(10),
        lat double precision,
        lon double precision,
        passengerCount integer,
        passengerFullness float,
        speed float,
        timeProcessed datetime(3),
        primary key (vehicleId, time)
    );

    create table Block_to_Trip_joinTable (
        Blocks_serviceId varchar(60) not null,
        Blocks_configRev integer not null,
        Blocks_blockId varchar(60) not null,
        trips_tripId varchar(60) not null,
        trips_startTime integer not null,
        trips_configRev integer not null,
        listIndex integer not null,
        primary key (Blocks_serviceId, Blocks_configRev, Blocks_blockId, listIndex)
    );

    create table Blocks (
        serviceId varchar(60) not null,
        configRev integer not null,
        blockId varchar(60) not null,
        endTime integer,
        headwaySecs integer,
        startTime integer,
        primary key (serviceId, configRev, blockId)
    );

    create table CalendarDates (
        serviceId varchar(60) not null,
        date date not null,
        configRev integer not null,
        exceptionType varchar(2),
        primary key (serviceId, date, configRev)
    );

    create table Calendars (
        wednesday boolean not null,
        tuesday boolean not null,
        thursday boolean not null,
        sunday boolean not null,
        startDate date not null,
        serviceId varchar(60) not null,
        saturday boolean not null,
        monday boolean not null,
        friday boolean not null,
        endDate date not null,
        configRev integer not null,
        primary key (wednesday, tuesday, thursday, sunday, startDate, serviceId, saturday, monday, friday, endDate, configRev)
    );

    create table FareAttributes (
        fareId varchar(60) not null,
        configRev integer not null,
        currencyType varchar(3),
        paymentMethod varchar(255),
        price float,
        transferDuration integer,
        transfers varchar(255),
        primary key (fareId, configRev)
    );

    create table FareRules (
        routeId varchar(60) not null,
        originId varchar(60) not null,
        fareId varchar(60) not null,
        destinationId varchar(60) not null,
        configRev integer not null,
        containsId varchar(60),
        primary key (routeId, originId, fareId, destinationId, configRev)
    );

    create table Frequencies (
        tripId varchar(60) not null,
        startTime integer not null,
        configRev integer not null,
        endTime integer,
        exactTimes boolean,
        headwaySecs integer,
        primary key (tripId, startTime, configRev)
    );

    create table Matches (
        vehicleId varchar(60) not null,
        avlTime datetime(3) not null,
        blockId varchar(60),
        configRev integer,
        distanceAlongSegment float,
        distanceAlongStopPath float,
        segmentIndex integer,
        serviceId varchar(255),
        stopPathIndex integer,
        tripId varchar(60),
        primary key (vehicleId, avlTime)
    );

    create table Predictions (
        id bigint not null,
        affectedByWaitStop boolean,
        configRev integer,
        creationTime datetime,
        isArrival boolean,
        predictionTime datetime,
        routeId varchar(60),
        stopId varchar(60),
        tripId varchar(60),
        vehicleId varchar(60),
        primary key (id)
    );

    create table Routes (
        id varchar(60) not null,
        configRev integer not null,
        color varchar(10),
        description varchar(255),
        maxLat double precision,
        maxLon double precision,
        minLat double precision,
        minLon double precision,
        hidden boolean,
        maxDistance double precision,
        name varchar(255),
        routeOrder integer,
        shortName varchar(80),
        textColor varchar(10),
        type varchar(2),
        primary key (id, configRev)
    );

    create table StopPaths (
        tripPatternId varchar(60) not null,
        stopPathId varchar(60) not null,
        configRev integer not null,
        breakTime integer,
        lastStopInTrip boolean,
        layoverStop boolean,
        locations blob,
        pathLength double precision,
        routeId varchar(60),
        scheduleAdherenceStop boolean,
        stopId varchar(60),
        stopSequence integer,
        waitStop boolean,
        primary key (tripPatternId, stopPathId, configRev)
    );

    create table Stops (
        id varchar(60) not null,
        configRev integer not null,
        adherenceStop boolean,
        code integer,
        hidden boolean,
        layoverStop boolean,
        lat double precision,
        lon double precision,
        name varchar(255),
        waitStop boolean,
        primary key (id, configRev)
    );

    create table Transfers (
        toStopId varchar(60) not null,
        fromStopId varchar(60) not null,
        configRev integer not null,
        minTransferTime integer,
        transferType varchar(1),
        primary key (toStopId, fromStopId, configRev)
    );

    create table TravelTimesForStopPath (
        id integer not null,
        daysOfWeekOverride integer,
        stopPathId varchar(60),
        stopTimeMsec integer,
        travelTimeSegmentLength double precision,
        travelTimesMsec blob,
        primary key (id)
    );

    create table TravelTimesForTrip_to_TravelTimesForPath_joinTable (
        TravelTimesForTrips_id integer not null,
        travelTimesForStopPaths_id integer not null,
        listIndex integer not null,
        primary key (TravelTimesForTrips_id, listIndex)
    );

    create table TravelTimesForTrips (
        id integer not null,
        configRev integer,
        howSet varchar(40),
        tripCreatedForId varchar(60),
        tripPatternId varchar(60),
        primary key (id)
    );

    create table TripPattern_to_Path_joinTable (
        TripPatterns_id varchar(60) not null,
        TripPatterns_configRev integer not null,
        stopPaths_tripPatternId varchar(60) not null,
        stopPaths_stopPathId varchar(60) not null,
        stopPaths_configRev integer not null,
        listIndex integer not null,
        primary key (TripPatterns_id, TripPatterns_configRev, listIndex)
    );

    create table TripPatterns (
        id varchar(60) not null,
        configRev integer not null,
        shapeId varchar(60),
        directionId varchar(60),
        maxLat double precision,
        maxLon double precision,
        minLat double precision,
        minLon double precision,
        name varchar(255),
        routeId varchar(60),
        primary key (id, configRev)
    );

    create table Trips (
        tripId varchar(60) not null,
        startTime integer not null,
        configRev integer not null,
        blockId varchar(60),
        directionId varchar(60),
        endTime integer,
        name varchar(255),
        routeId varchar(60),
        routeShortName varchar(60),
        scheduledTimesMap blob,
        serviceId varchar(60),
        shapeId varchar(60),
        travelTimes_id integer,
        tripPattern_id varchar(60),
        tripPattern_configRev integer,
        primary key (tripId, startTime, configRev)
    );

    create index indexTest on ArrivalsDepartures (time);

    create index timeIndex on AvlReports (time);

    alter table Block_to_Trip_joinTable 
        add constraint FK_abaj8ke6oh4imbbgnaercsowo 
        foreign key (trips_tripId, trips_startTime, trips_configRev) 
        references Trips;

    alter table Block_to_Trip_joinTable 
        add constraint FK_1c1e1twdap19vq0xkav0amvm 
        foreign key (Blocks_serviceId, Blocks_configRev, Blocks_blockId) 
        references Blocks;

    create index avlTimeIndex on Matches (avlTime);

    alter table TravelTimesForTrip_to_TravelTimesForPath_joinTable 
        add constraint UK_hh5uepurijcqj0pyc6e3h5mqw unique (travelTimesForStopPaths_id);

    alter table TravelTimesForTrip_to_TravelTimesForPath_joinTable 
        add constraint FK_hh5uepurijcqj0pyc6e3h5mqw 
        foreign key (travelTimesForStopPaths_id) 
        references TravelTimesForStopPath;

    alter table TravelTimesForTrip_to_TravelTimesForPath_joinTable 
        add constraint FK_9j1s8ewsmokqg4m35wrr29na7 
        foreign key (TravelTimesForTrips_id) 
        references TravelTimesForTrips;

    alter table TripPattern_to_Path_joinTable 
        add constraint UK_s0gaw8iv60vc17a5ltryqwg27 unique (stopPaths_tripPatternId, stopPaths_stopPathId, stopPaths_configRev);

    alter table TripPattern_to_Path_joinTable 
        add constraint FK_s0gaw8iv60vc17a5ltryqwg27 
        foreign key (stopPaths_tripPatternId, stopPaths_stopPathId, stopPaths_configRev) 
        references StopPaths;

    alter table TripPattern_to_Path_joinTable 
        add constraint FK_qsr8l6u1nelb5pt8rlnei08sy 
        foreign key (TripPatterns_id, TripPatterns_configRev) 
        references TripPatterns;

    alter table Trips 
        add constraint FK_p1er53449kkfsca6mbnxkdyst 
        foreign key (travelTimes_id) 
        references TravelTimesForTrips;

    alter table Trips 
        add constraint FK_676npp7h4bxh8sjcnugnxt5wb 
        foreign key (tripPattern_id, tripPattern_configRev) 
        references TripPatterns;

    create sequence hibernate_sequence;
