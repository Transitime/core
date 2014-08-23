Want all jars to be in the Tomcat shared/lib directory so that don't have
to include them in WAR file each time. This reduces duplication of 
libraries, makes them easier to support, and makes creating and
copying the war files easier. But this didn't work with some of
the hibernate jar files. Was getting ClassNotFoundException when
the hibernate jars were in shared/lib. Therefore the jar files
in this directory are the ones that need to be under webapps/APP/libs.

The libs that need to be in webaps/APP/libs are:
  c3p0-0.9.2.1.jar
  hibernate-c3p0-4.2.4.Final.jar
  hibernate-commons-annotations-4.0.2.Final.jar
  hibernate-core-4.2.2.Final.jar
  hibernate-jpa-2.0-api-1.0.1.Final.jar

