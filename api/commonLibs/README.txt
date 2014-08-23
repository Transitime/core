Common JAR Files for Tomcat applicatin
================================

Jersey (the dependencies are complicated but are listed in the API Google docs age)
  https://jersey.java.net/download.html
  http://mvnrepository.com/artifact/org.glassfish.jersey.media/jersey-media-moxy
  http://mvnrepository.com/artifact/org.glassfish.jersey.ext/jersey-entity-filtering
    aopalliance-repackaged-2.3.0-b05.jar
    asm-debug-all-5.0.2.jar
    avro-1.6.3.jar
    hk2-api-2.3.0-b05.jar
    hk2-locator-2.3.0-b05.jar
    hk2-utils-2.3.0-b05.jar
    javax.annotation-api-1.2.jar
    javax.inject-2.3.0-b05.jar
    javax.ws.rs-api-2.0.jar
    jaxb-api-2.2.7.jar
    jersey-client.jar
    jersey-common.jar
    jersey-container-servlet-core.jar
    jersey-entity-filtering-2.9.1.jar
    jersey-guava-2.9.jar
    jersey-media-moxy-2.9.1.jar
    jersey-server.jar
    org.eclipse.persistence.antlr-2.5.1.jar
    org.eclipse.persistence.asm-2.5.1.jar
    org.eclipse.persistence.core-2.5.1.jar
    org.eclipse.persistence.moxy-2.5.1.jar
    org.osgi.core-4.2.0.jar
    osgi-resource-locator-1.0.1.jar
    persistence-api-1.0.jar
    validation-api-1.1.0.Final.jar

Logging using logback:
  http://logback.qos.ch/download.html
    logback-classic-1.0.13.jar  
    logback-core-1.0.13.jar
  http://www.slf4j.org/download.html
    slf4j-api-1.7.5.jar

Need java.mail.xxx for using SMTP to e-mail errors using logback.
  https://java.net/projects/javamail/pages/Home
    javax.mail.jar (version 1.5)  

For parsing csv files, which is the GTFS format
  http://commons.apache.org/proper/commons-csv/downloads.html
  https://repository.apache.org/content/groups/snapshots/org/apache/commons/commons-csv/1.0-SNAPSHOT/
    commons-csv-1.0-20130901.142050-232.jar

For HornetQ JMS need the following jars. Note that this is more
than what was found in hornetq documentation, probably because 
logging is used.
    jboss-jms-api.jar
    hornetq-jms-client.jar
    hornetq-core-client.jar  
    hornetq-commons.jar
    jboss-mc.jar
    netty.jar

For concurrency annotations like @Immutable and @ThreadSave from the 
"Concurrency in Practice" book need library:
  http://jcip.net/
    jcp-annotations.jar

The JDOM library makes it easier to process XML files.
  http://www.jdom.org/downloads/index.html
    jdom-2.0.5.jar

For hibernate the following are the "required" jars. Note that some
hibernate libraries need to be included in webapp/APP/lib directory
for them to work and are therefore in webappLibs directory. Hibernate
libs are in the downloaded hibernate dir 
hibernate-search-4.3.0.Final/dist/lib/required/:
  http://hibernate.org
    antlr-2.7.7.jar
    avro-1.6.3.jar
    dom4j-1.6.1
    hibernate-commons-annotations-4.0.2.Final.jar (actually in webappsLib/)
    hibernate-core-4.2.2.Final.jar (actually in webappsLib/)
    jackson-core-asl-1.9.2.jar
    jackson-mapper-asl-1.9.2.jar
    javassist-3.15.1-GA.jar
    jboss-logging-3.1.0.GA
    lucerne-core-3.6.2.jar
    paranamer-2.3.jar
    slf4j-api-1.6.1.jar (note that already using this jar for general logging)
    snappy-java-1.0.4.1.jar

For hibernate also need the JPA annotations, such as for @Temporal.
Also need some jta things like SystemException.
There are in the downloaded hibernate dir 
hibernate-search-4.3.0.Final/dist/lib/provided/ :
    hibernate-jpa-2.0-api-1.0.1.Final.jar (actually in webappsLib/)
    jta-1.1.jar

For hibernate also need appropriate jdbc driver, depending on what
database is being used. 
  MySQL driver obtained from http://dev.mysql.com/downloads/connector/j
  Make sure you select the platform independent driver so that you
  get an easy to handle zip file instead of a Windows installer that
  put the jar file into some place I could not find. Note: need at least
  version 5.1.23 for fractional seconds to work.
    mysql-connector-java-5.1.26-bin.jar

  Postgres driver obtained from http://jdbc.postgresql.org/download.html
    postgresql-9.3-1102.jdbc41.jar

For hibernate also need a production worthy connection pooler
instead of using the standard hibernate one. 
  http://sourceforge.net/projects/c3p0
    c3p0-0.9.2.1.jar (actually in webappsLib/)
    mchange-commons-java-0.2.3.4.jar
  http://mvnrepository.com/artifact/org.hibernate/hibernate-c3p0
    hibernate-c3p0-4.2.4.Final.jar (actually in webappsLib/)

For many Java utilities, such as StringEscapeUtils
  commons-lang3-3.3.2.jar

For command line args. Not used for Tomcat but used in main app so
included here so can be added to Eclipse project and won't have
compile errors.
    commons-cli-1.2.jar
