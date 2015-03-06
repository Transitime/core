export CATALINA_OPTS="$CATALINA_OPTS -Dtransitime.db.dbName=web\
 -Dtransitime.db.dbHost=sfmta.c3zbap9ppyby.us-west-2.rds.amazonaws.com\
 -Dtransitime.db.dbUserName=transitime\
 -Dtransitime.db.dbPassword=transitime\
 -Dtransitime.db.dbType=postgresql\
 -Dtransitime.hibernate.configFile=$CATALINA_BASE/transitimeConfig/hibernate_postgres_cfg.xml\
 -Dlogback.configurationFile=$CATALINA_BASE/transitimeConfig/logbackTomcat.xml\
 -Dtransitime.logging.dir=/tomcatlogs"

