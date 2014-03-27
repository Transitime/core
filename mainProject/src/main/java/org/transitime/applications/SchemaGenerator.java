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
import java.util.ArrayList;
import java.util.List;
import org.hibernate.cfg.Configuration;
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
		String outputFilename = (outputDirectory!=null?outputDirectory+"/" : "") + 
				"ddl_" + dialect.name().toLowerCase() + 
				"_" + packageName.replace(".", "-") + ".sql";
		export.setOutputFile(outputFilename);
		
		// Export, but only to an sql file. Don't actually modify the database
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
	 * Holds the classnames of hibernate dialects for easy reference.
	 */
	private static enum Dialect {
		ORACLE("org.hibernate.dialect.Oracle10gDialect"), 
		MYSQL("org.hibernate.dialect.MySQLDialect"), 
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
	 * directory where the resulting files are to go.
	 */
	public static void main(String[] args) throws Exception {
		final String packageName = args[0];
		final String outputDirectory = args.length > 1 ? args[1] : null;
		
		SchemaGenerator gen = new SchemaGenerator(packageName, outputDirectory);
		gen.generate(Dialect.MYSQL);
		gen.generate(Dialect.POSTGRES);
		gen.generate(Dialect.ORACLE);
	}

}

