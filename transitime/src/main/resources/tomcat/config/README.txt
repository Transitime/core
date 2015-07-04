The files in the tomcat/confg directory are for customizing the
Tomcat configuration on AWS so will work with the Transitime
application. They need to be copied to the AWS web server when
it is created plus sometimes after a "yum update" is done since
that can load in new version of Tomcat along with config files.

The files, and where they go, are:
/usr/share/tomcat7/conf/server.xml - for enabling compression
/usr/share/tomcat7/conf/catalina.properties - for speeding startup and for setting the shared lib directory

