This is the web application for transitTime and can be built by 

```
cd transitimeWebapp
maven install -DskipTests
```

This produces a war file for deployment web.war in the target directory.

You will need to configure the location of the transitTimeConfig.xml file in the web.xml file. The transitTimeConfig.xml file in turn is used to specify the location of the database and the hibernate file.

You will also need to configure the key for accessing the transitimeApi in the template/includes.jsp file. You can use the CreateAPIKey application in transiTime to create a test/demo key. This you may already have done as part of the setup of transitimeApi.
