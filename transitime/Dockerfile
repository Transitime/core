FROM java
MAINTAINER Nathan Walker <nathan@rylath.net>

ADD target/transitime.jar transitime.jar
ADD target/classes/postgres_hibernate.cfg.xml hibernate.cfg.xml
ADD docker/load_db.sh load_db.sh
ADD docker/run_server.sh run_server.sh

#ENTRYPOINT exec java -Dtransitime.hibernate.configFile=postgres_hibernate.cfg.xml \
#    -Dtransitime.avl.gtfsRealtimeFeedURI="https://data.texas.gov/download/i5qp-g5fd/application/octet-stream" \
#    -Dtransitime.modules.optionalModulesList=org.transitime.avl.GtfsRealtimeModule \
#    -Dtransitime.core.agencyId=transitime
#    -cp transitimeCore-0.0.1-SNAPSHOT.jar org.transitime.applications.GtfsFileProcessor
