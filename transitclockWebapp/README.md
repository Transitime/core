This is the web application for TheTransitClock and can be built by 

```
cd transitclockWebapp
maven install -DskipTests
```

This produces a war file for deployment web.war in the target directory.

You will need to configure the location of the transitclockConfig.xml file as a command line argument:

`-Dtransitclock.configFiles=/path/to/your/transitclockConfig.xml`

The exact place to do this depends on how you're running TheTransitClock. In Eclipse, add this as a VM argument in the run configuration for Tomcat. In a bash script, add it to `CATALINA_OPTS` before Tomcat starts up.

The transitclockConfig.xml file in turn is used to specify the location of the database and the hibernate file.

You will also need to configure the key for accessing the transitclockApi in the template/includes.jsp file. You can use the CreateAPIKey application in TheTransitClock to create a test/demo key. This you may already have done as part of the setup of transitclockApi.
