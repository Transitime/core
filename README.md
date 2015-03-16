core
====

The complete core Java software for the Transitime real-time transit information project.

The software is checked in and made public only so that it can be viewed. While it has been used in a test environment it still is lacking significant features and is not ready for deployment.

The software is made up of three modules which can each be built with maven.

The core fucntionality is in the transiTime project. The REST api is in transiTimeApi and the user Web applicaton is in transiTimeWebapp.

They should be built in this order.


transitTime

transitTimeApi

transitTimeWebApp


Each module can be built by change to the related directory and running "mvn install".

eg.

cd transitTime

mvn install

Setup

The main module is transitTime. This has the several tools in the org.transitime.applications package.

SchemaGenerator.java will generate the SQL to create the database structures you need to run on.
DBTest.java can be used to test that the database can be connected to.
GTFSFileProcessor.java will read a GTFS file into this database structure.
Core.java is as the name implies is the workhorse of the system. 
RmiQuery.java allows you make queries to the server run in core from the command line.

Details on how to run each of these and their respective parameters are in the README for the transiTime module.



