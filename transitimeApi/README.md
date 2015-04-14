This is the a REST service which provides the information required to run a web applicaiton or mobile application based on transitTime.

This can be built on its own by 

cd transitTimeApi<br/>
mvn install

This will produce a war file which can be deployed on Tomcat. 

This server talks to core using RMI calls to get the information to support the REST service calls.

To access the service a key is required to be provided in the URL. This key is compared against a key in the database. You can use the CreateAPIKey applcation in transiTime to create a test/demo key.

The tables that store this information are create by running the ddl_xxxx_org_transitime_db_webstructs.sql in the database.(Where xxxx is the type of datbase you are using)

Example URLs

http://[server]:[port]/v1/transitime/key/[Key from CreateAPIKey]/agency/[agency id]/command/gtfs-rt/tripUpdates?format=human

http://127.0.0.1:8093/v1/transitime/key/8a3273b0/agency/02/command/gtfs-rt/tripUpdates?format=human

The comments in the supporting classes are the best source of information for RESTFul calls.
