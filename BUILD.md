The software is made up of three modules which can each be built with maven.

The core fucntionality is in the transiTime project. The REST api is in transiTimeApi and the user Web applicaton is in transiTimeWebapp.

They should be built in this order.

1. transitTime
2. transitTimeApi
3. transitTimeWebApp

Each module can be built by change to the related directory and running "mvn install".

eg. 

cd transitTime
mvn install