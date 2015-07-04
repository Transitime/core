<b>generateDatabaseSchema.jar</b>
The jar generateDatabaseSchema.jar can be used to re-generate the SQL required to create the database structures required to run transiTime. It generates three files in the specified directory. One for each supported database type. (Postgres, Oracle, Mysql). The script generated will drop tables that already exist.

usage: java -jar generateDatabaseSchema.jar
 -o,--outputDirectory <arg>        This is the directory to output the sql
 -p,--hibernatePackagePath <arg>   This is the path to the package
                                   containing the hibernate annotated java
                                   classes
                                   
Example:
	java -jar generateDatabaseSchema.jar -o c:\temp\ -p org.transitime.db.structs	
	
To create all tables require you to support the core and the webapp you should run
	java -jar generateDatabaseSchema.jar -o c:\temp\core\ -p org.transitime.db.structs
	java -jar generateDatabaseSchema.jar -o c:\temp\web\ -p org.transitime.db.webstructs

Once this is done you should run the sql in the files in the core and web directory in the database.
	
                                 