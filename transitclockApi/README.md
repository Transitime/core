This is the a REST service which provides the information required to run a web application or mobile application based on TheTransitClock.

This can be built on its own by 
```
cd transitclockApi
mvn install
```

This will produce a api.war file which can be deployed on Tomcat. 

You will need to configure the location of the transitclockConfig.xml file as a command line argument:

`-Dtransitclock.configFiles=/path/to/your/transitclockConfig.xml`

The exact place to do this depends on how you're running TheTransitClock. In Eclipse, add this as a VM argument in the run configuration for Tomcat. In a bash script, add it to `CATALINA_OPTS` before Tomcat starts up.

This server talks to core using RMI calls to get the information to support the REST service calls.

To access the service a key is required to be provided in the URL. This key is compared against a key in the database. You can use the CreateAPIKey application in TheTransitClock to create a test/demo key.

The tables that store this information are create by running the ddl_xxxx_org_transitime_db_webstructs.sql in the database. (Where xxxx is the type of database you are using)
```
Example URLs

http://[server]:[port]/v1/transitime/key/[Key from CreateAPIKey]/agency/[agency id]/command/gtfs-rt/tripUpdates?format=human

http://127.0.0.1:8093/v1/transitime/key/8a3273b0/agency/02/command/gtfs-rt/tripUpdates?format=human
```
The comments in the supporting classes are the best source of information for RESTFul calls.
