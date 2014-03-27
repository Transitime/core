JAR Files
=========

Logging using logback:
  http://logback.qos.ch/download.html
    logback-classic-1.0.13.jar  
    logback-core-1.0.13.jar
  http://www.slf4j.org/download.html
    slf4j-api-1.7.5.jar

Need java.mail.xxx for using SMTP to e-mail errors using logback.
  https://java.net/projects/javamail/pages/Home
    javax.mail.jar (version 1.5)  

For handling command line arguments can use the apache commons lib:
  http://commons.apache.org/proper/commons-cli/download_cli.cgi
    commons-cli-1.2.jar

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
    jcp-annoations.jar

The JDOM library makes it easier to process XML files.
  http://www.jdom.org/downloads/index.html
    jdom-2.0.5.jar

For hibernate the following are the "required" jars. They are in
the downloaded hibernate dir hibernate-search-4.3.0.Final/dist/lib/required/:
  http://hibernate.org
    antlr-2.7.7.jar
    avro-1.6.3.jar
    dom4j-1.6.1
    hibernate-commons-annotations-4.0.2.Final.jar
    hibernate-core-4.2.2.Final.jar
    jackson-core-asl-1.9.2.jar
    jackson-mapper-asl-1.9.2.jar
    javassist-3.14.0-GA.jar
    jboss-logging-3.1.0.GA
    lucerne-core-3.6.2.jar
    paranamer-2.3.jar
    slf4j-api-1.6.1.jar (note that already using this jar for general logging)
    snappy-java-1.0.4.1.jar

For hibernate also need the JPA annotations, such as for @Temporal.
Also need some jta things like SystemException.
There are in the downloaded hibernate dir 
hibernate-search-4.3.0.Final/dist/lib/provided/ :
    hibernate-jpa-2.0-1pi-1.0.1.Final.jar
    jta-1.1.jar

For hibernate also need appropriate jdbc driver, depending on what
database is being used. 
  MySQL driver obtained from http://dev.mysql.com/downloads/connector/j
  Make sure you select the platform independent driver so that you
  get an easy to handle zip file instead of a Windows installer that
  put the jar file into some place I could not find. Note: need at least
  version 5.1.23 for fractional seconds to work.
    mysql-connector-java-5.1.26-bin.jar

For hibernate also need a production worthy connection pooler
instead of using the standard hibernate one. 
  http://sourceforge.net/projects/c3p0
    c3p0-0.9.2.1.jar
    mchange-commons-java-0.2.3.4.jar
  http://mvnrepository.com/artifact/org.hibernate/hibernate-c3p0
    hibernate-c3p0-4.2.4.Final.jar

