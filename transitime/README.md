There are several main classes which are used in the set up of the system. These can be run directly by specifying the class to run or by using the executable jar in the target directory.

generateDatabaseSchema.jar -- Main class: org.transitime.applications.SchemaGenerator
=================================
<br/>
The jar generateDatabaseSchema.jar can be used to re-generate the SQL required to create the database structures required to run transiTime. It generates three files in the specified directory. One for each supported database type. (Postgres, Oracle, Mysql). The script generated will drop tables that already exist.
<br/>
<i>


```
usage: 
	java -jar generateDatabaseSchema.jar<br/>
 		-o,--outputDirectory <arg>        This is the directory to output the sql<br/>
 		-p,--hibernatePackagePath <arg>   This is the path to the package
                		                  containing the hibernate annotated java<br/>
                                		  classes<br/>
```                                		  
                                   

```
example:
	java -jar generateDatabaseSchema.jar -o c:\temp\ -p org.transitime.db.structs	
```
To create all tables require you to support the core and the webapp you could run

```
	java -jar generateDatabaseSchema.jar -o c:\temp\core\ -p org.transitime.db.structs
	java -jar generateDatabaseSchema.jar -o c:\temp\web\ -p org.transitime.db.webstructs
```

Once these commands have been run you should run the sql created in the files in the core and web directory in your database.
	
TODO: This works in eclipse by executing the class but not on command line using the executable jar. Strange and need to investigate. Probably somthing to do with the fact it is an executable jar.

processGTFSFile.jar -- Main class: org.transitime.applications.GTFSFileProcessor
=================================                            
