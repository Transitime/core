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

cd transitTime mvn install
