<b>generateDatabaseSchema.jar</b>
=================================
<br/>
The jar generateDatabaseSchema.jar can be used to re-generate the SQL required to create the database structures required to run transiTime. It generates three files in the specified directory. One for each supported database type. (Postgres, Oracle, Mysql). The script generated will drop tables that already exist.
<br/>
<i>
usage: 
<br/>
	java -jar generateDatabaseSchema.jar<br/>
 		-o,--outputDirectory <arg>        This is the directory to output the sql<br/>
 		-p,--hibernatePackagePath <arg>   This is the path to the package
                		                  containing the hibernate annotated java<br/>
                                		  classes<br/>
                                   
</i>                                   
example:
	java -jar generateDatabaseSchema.jar -o c:\temp\ -p org.transitime.db.structs	
	
To create all tables require you to support the core and the webapp you should run
<br/>
	java -jar generateDatabaseSchema.jar -o c:\temp\core\ -p org.transitime.db.structs
<br/>	java -jar generateDatabaseSchema.jar -o c:\temp\web\ -p org.transitime.db.webstructs
<br/>
Once this is done you should run the sql in the files in the core and web directory in the database.
	
                                 
