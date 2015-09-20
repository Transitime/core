java \
    -Dtransitime.hibernate.configFile=hibernate.cfg.xml \
    -Dhibernate.connection.url=jdbc:postgresql://$POSTGRES_PORT_5432_TCP_ADDR:$POSTGRES_PORT_5432_TCP_PORT/cap-metro \
    -Dhibernate.connection.username=postgres \
    -Dhibernate.connection.password=transitime \
    -Dtransitime.avl.gtfsRealtimeFeedURI="https://data.texas.gov/download/i5qp-g5fd/application/octet-stream" \
    -Dtransitime.modules.optionalModulesList=org.transitime.avl.GtfsRealtimeModule \
    -Dtransitime.core.agencyId=cap-metro \
    -cp transitime.jar org.transitime.applications.Core
