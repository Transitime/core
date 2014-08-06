/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.applications;

import java.io.File;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * For generating SQL schema files based on classes to be stored in database
 * that were annotated for Hibernate. This is much nicer than trying to
 * figure out what the schema really should be by hand. This code was copied from
 * http://jandrewthompson.blogspot.nl/2009/10/how-to-generate-ddl-scripts-from.html
 * 
 * @author john.thompson and Skibu Smith
 *
 */
public class SchemaGenerator {
	private final Configuration cfg;
	private final String packageName;
	private final String outputDirectory;
	
	/**
	 * MySQL handles fractional seconds differently from PostGRES and other
	 * DBs. Need to use "datetime(3)" for fractional seconds whereas with 
	 * PostGRES can use the default "timestamp" type. In order to handle
	 * this properly in the generated ddl schema files need to not use
	 * @Column(columnDefinition="datetime(3)") in the Java class that defines
	 * the db object. Instead need to use this special ImprovedMySQLDialect
	 * as the Dialect.
	 */
	public static class ImprovedMySQLDialect extends MySQLDialect {
		public ImprovedMySQLDialect() {
			super();
			// Specify special SQL type for MySQL for timestamps so that get
			// fractions seconds.
			registerColumnType(Types.TIMESTAMP, "datetime(3)");
		}
	}


	@SuppressWarnings("unchecked")
	public SchemaGenerator(String packageName, String outputDirectory) throws Exception {
		this.cfg = new Configuration();
		this.cfg.setProperty("hibernate.hbm2ddl.auto", "create");

		for (Class<Object> clazz : getClasses(packageName)) {
			this.cfg.addAnnotatedClass(clazz);
		}
		
		this.packageName = packageName;
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Method that actually creates the file.
	 * 
	 * @param dbDialect to use
	 */
	private void generate(Dialect dialect) {
		cfg.setProperty("hibernate.dialect", dialect.getDialectClass());

		SchemaExport export = new SchemaExport(cfg);
		export.setDelimiter(";");
		
		// Determine file name. Use "ddl_" plus dialect name such as mysql or
		// oracle plus the package name with "_" replacing "." such as
		// org_transitime_db_structs .
		String packeNameSuffix = 
				packageName.replace(".", "_");
		String outputFilename = (outputDirectory!=null?outputDirectory+"/" : "") + 
				"ddl_" + dialect.name().toLowerCase() + 
				"_" + packeNameSuffix + ".sql";
		
		export.setOutputFile(outputFilename);
		
		// Export, but only to an SQL file. Don't actually modify the database
		System.out.println("Writing file " + outputFilename);
		export.execute(true, false, false, false);
	}

	/**
	 * Utility method used to fetch Class list based on a package name.
	 * 
	 * @param packageName
	 *            (should be the package containing your annotated beans.
	 */
	@SuppressWarnings("rawtypes")
	private List<Class> getClasses(String packageName) throws Exception {
		List<Class> classes = new ArrayList<Class>();
		File directory = null;
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			String path = packageName.replace('.', '/');
			URL resource = cld.getResource(path);
			if (resource == null) {
				throw new ClassNotFoundException("No resource for " + path);
			}
			directory = new File(resource.getFile());
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(packageName + " (" + directory
					+ ") does not appear to be a valid package");
		}
		if (directory.exists()) {
			for (String fileName : directory.list()) {
				if (fileName.endsWith(".class")) {
					// removes the .class extension
					String className = packageName + '.'
							+ fileName.substring(0, fileName.indexOf(".class"));
					classes.add(Class.forName(className));
				}
			}
		} else {
			throw new ClassNotFoundException(packageName
					+ " is not a valid package");
		}

		return classes;
	}

	/**
	 * Holds the class names of hibernate dialects for easy reference.
	 */
	private static enum Dialect {
		ORACLE("org.hibernate.dialect.Oracle10gDialect"), 
		// Note that using special ImprovedMySqlDialect
		MYSQL("org.transitime.applications.SchemaGenerator$ImprovedMySQLDialect"),
		POSTGRES("org.hibernate.dialect.PostgreSQLDialect"),
		HSQL("org.hibernate.dialect.HSQLDialect");

		private String dialectClass;

		private Dialect(String dialectClass) {
			this.dialectClass = dialectClass;
		}

		public String getDialectClass() {
			return dialectClass;
		}
	}

	/**
	 * @param args args[0] is the package name for the Hibernate annotated 
	 * classes whose schema is to be exported. args[1] is optional output
	 * directory where the resulting files are to go. If the optional output
	 * directory is not specified then schema files written to local directory.
	 * <p>
	 * The resulting files have the name "ddl_" plus dialect name such as mysql 
	 * or oracle plus the first two components of the package name such as
     * org_transitime.
	 */
	public static void main(String[] args) throws Exception {
		final String packageName = args[0];
		final String outputDirectory = args.length > 1 ? args[1] : null;
		
		SchemaGenerator gen = new SchemaGenerator(packageName, outputDirectory);
		// Note: need to generate MYSQL last because using special 
		// ImprovedMySQLDialect Dialect for MySQL but for some reason when
		// it calls registerColumnType() in the constructor it affects the 
		// other dialects as well. So need to do MySQL last.
		gen.generate(Dialect.POSTGRES);
		gen.generate(Dialect.ORACLE);
		gen.generate(Dialect.MYSQL);
	}

}

