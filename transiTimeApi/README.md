This is the a REST service which provides the information required to run a web applicaiton or mobile application based on transitTime.

This can be built on its own by 

cd transitTimeApi
mvn install

A grizzly server can be started with the API running by running

mvn exec:java

This server talks to core using RMI calls to get the information to support the REST service calls.

TODO: Need go figure out how to configure the location of the RMI host and port.

To access the service a key is required to be provided in the URL. This key is compared against a key in the database. You can use the CreateAPIKey applcation in transiTime to create a test/demo key.

The tables that store this information are create by running the ddl_xxxx_org_transitime_db_webstructs.sql in the database.(Where xxxx is the type of datbase you are using)

org.transitime.api.utils.ApiKeyManger can be run to create a key to be used. The paramaters required are 
applicationUrl, email, phone, and description in that order. (TODO This needs to be changed to use commons-cli)
