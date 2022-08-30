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
package org.transitclock.config;

import org.transitclock.config.ConfigValue.ConfigParamException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The ConfigFile class provides a way of handling parameters that can be
 * updated without having to restart the application. The parameters can be
 * access efficiently so that they can be accessed in loops that are frequently
 * executed. This class is useful for parameters that are system wide and often
 * have a default. Such params are not best stored in a database because with a
 * db it can be harder to see what all the values are or to copy the values from
 * one project to another. It can also be harder to keep track of changes when
 * such params are in a database.
 * <p>
 * An additional goal of Config is for all params to be processed when the
 * config file is read, both at startup and when file is reread. This is
 * important because then if there are any processing problems those problems
 * are noticed and must be dealt with immediately (instead of them occurring in
 * an lazy fashion, perhaps when no one is available to deal with them). But
 * this goal would mean that the params would need to be defined in config files
 * so they would all be dealt with at startup. For many situations want the
 * params to be defined where they are used since that would make them easier to
 * support. But then they wouldn't get initialized until the Java class is
 * actually accessed.
 * <p>
 * For each parameter configured in the file the property is written as a system
 * property if that system property is not yet defined. This way all parameters
 * are available as system properties, even non-transitclock ones such as for
 * hibernate or logback. Java properties set on the command line take precedence
 * since the properties are not overwritten if already set.
 * <p>
 * The params are configured in an XML or a properties file. The member
 * processConfig() first process a file named transitclock.properties if one
 * exists in the classpath. Then files specified by the Java system property
 * transitclock.configFiles . If the file has a .xml suffix then it will be
 * processed as XML. Otherwise it will be processed as a regular properties
 * file. Multiple config files can be used. Each XML file should have a
 * corresponding configuration class such as CoreConfig.java. In the
 * XXXConfig.java class the params are specified along with default values.
 * <p>
 * Multiple threads can read parameters simultaneously. The threads should call
 * readLock() if it is important to update the parameters in a consistent way,
 * such as when more than a single parameter are interrelated.
 * <p>
 * Logback error logging is not used in this class so that logback can be not
 * accessed until after the configuration is processed. In this way logback can
 * be configured using a configuration file to set any logback Java system
 * properties such as logback.configurationFile .
 * 
 * @author SkiBu Smith
 *
 */
public class ConfigFileReader {
	/**
	 * Need to be able to synchronize reads and writes. Since
	 * this class is static need static synchronization objects. 
	 * Using a ReentrantReadWriteLock so that multiple threads
	 * with reads can happen simultaneously. This works well
	 * because writing/updating data will be very infrequent.
	 */
	private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock readLock = rwl.readLock();
    private static final Lock writeLock = rwl.writeLock();
    
	/********************** Member Functions **************************/

	/**
	 * An exception for when a parameter is being read in
	 * @author SkiBu Smith
	 *
	 */
	public static class ConfigException extends Exception {
		// Needed since subclass Exception is serializable. Otherwise get warning. 
		private static final long serialVersionUID = 1L;

		private ConfigException(Exception e) {
			super(e);
		}
		
		private ConfigException(String msg) {
			super(msg);
		}
	}

	/**
	 * processChildren() is called recursively to process data from an xml file.
	 * 
	 * @param nodeList
	 *            The data from the XML file that was processed by the DOM XML
	 *            processor
	 * @param names
	 *            For keeping track of the full hierarchical parameter name such
	 *            as "transitest.predictor.maxDistance".
	 */
	private static void processChildren(NodeList nodeList, 
										List<String> names) {
		for (int i=0; i<nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			
			// Only need to deal with ELEMENT_NODES. Otherwise get 
			// extraneous info.
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			String nodeName = node.getNodeName();
			names.add(nodeName);
			
			// If there are child nodes handle them recursively. 
			if (hasChildNodesThatAreElements(node)) {
				NodeList childNodeList = node.getChildNodes();
				processChildren(childNodeList, names);				
			} else {				
				// Determine full name of parameter by appending node
				// names together, separated by a period. So get something
				// like "transitclock.predictor.radius".
				StringBuilder propertyNameBuilder = new StringBuilder("");
				for (int j=0; j<names.size(); ++j) {
					String name = names.get(j);
					propertyNameBuilder.append(name);
					if (j != names.size()-1)
						propertyNameBuilder.append(".");
				}
				String propertyName = propertyNameBuilder.toString();
				
				// Determine value specified in xml file
				String value = node.getTextContent().trim();
				
				// If the property hasn't yet been set then set the configured 
				// property as a system property so it can be read in using 
				// ConfigValue class. The reason only do this if the system 
				// property hasn't been set yet is so that parameters set on 
				// the system command line will take precedence.
				if (System.getProperty(propertyName) == null) {
					System.setProperty(propertyName, value);
				} else {
					if (!value.equals(System.getProperty(propertyName))) {
						// log a possible configuration issue
						// by documentation this is allowed as system properties
						// take precedence
						System.out.println("NOT OVERWRITING Property: " + propertyName + "=" + value
								+ " with existing val=" + System.getProperty(propertyName));
					}
				}
			}

			// Done with this part of the tree so take the current node name from the list
			names.remove(names.size()-1);
		}
	}

	/**
	 * Determines whether a node has valid child elements. Filters out ones
	 * that are not a real ELEMENT_NODE.
	 * @param node
	 * @return
	 */
	private static boolean hasChildNodesThatAreElements(Node node) {
		NodeList childNodeList = node.getChildNodes();
		for (int i=0; i<childNodeList.getLength(); ++i) {
			Node childNode = childNodeList.item(i);
			
			// Only need to deal with ELEMENT_NODES. Otherwise get extraneous info.
			if (childNode.getNodeType() == Node.ELEMENT_NODE)
				return true;
		}
		
		return false;
	}	
	
	/**
	 * Reads the specified XML file and stores the results into a HashMap
	 * configFileData. The file is expected to be very simple. Attributes are
	 * ignored. Only values are used. The keys for the resulting HashMap are
	 * based on the XML tags and their nesting. So if one is setting a value of
	 * 75 to a param with a key transitclock.predictor.allowableDistanceFromPath
	 * the XML would look like: {@code
	 * <transitclock>
	 *   <predictor>
	 *     <allowableDistanceFromPath>
	 *       75.0
	 *     </allowableDistanceFromPath>
	 *   </predictor>
	 * </transitclock>
	 * }
	 * @param fileName
	 */
	public static void readXmlConfigFile(String fileName) 
			throws ConfigException {
		Document doc = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try {
			File xmlFile;
			if (fileName.startsWith("classpath:")) {
				URL resource = ConfigFileReader.class.getClassLoader().getResource(fileName.substring("classpath:".length()));
				if (resource == null)
					throw new ConfigException("file not found in classpath; fileName=" + fileName);
				xmlFile = new File(resource.getFile());
			} else {
				xmlFile = new File(fileName);
			}

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
		} catch (Exception e) {
			System.err.println("Error parsing XML file \"" + fileName + "\" : " +
					e.getMessage());

			throw new ConfigException(e);
		}

		if (doc == null) {
			throw new ConfigException("init failed for file " + fileName);
		}

		// So text strings separated by newlines are not 
		// considered separate text nodes
		doc.getDocumentElement().normalize();
	 
		// For keeping track of names
		List<String> names = new ArrayList<String>();
		// Where to start in hierarchy when recursively calling processChild()
		NodeList nodeList = doc.getChildNodes();
		processChildren(nodeList, names);
	}

	/**
	 * Reads a Java properties file and stores all the properties as system
	 * properties if they haven't yet been set.
	 * 
	 * @param inputStream The properties file
	 */
	public static void readPropertiesConfigFile(InputStream inputStream) {
		try {
			// Read the properties config file
			Properties properties = new Properties();
			properties.load(inputStream);
			
			// For each property in the config file...
			for (String propertyName : properties.stringPropertyNames()) {				
				// If property not set yet then set it now
				if (System.getProperty(propertyName) == null) {
					String value = properties.getProperty(propertyName);

//					// Handle any property with a name ending with "File" 
//					// specially by seeing if it if a file in the class path.
//					// If it is then use the file name of the file that is 
//					// found in the classpath.
//					// NOTE: this might be confusing and therefore not a good idea.
//					if (propertyName.endsWith("File")) {
//						File f = new File(value);
//						if (!f.exists()) {
//							// Couldn't find file directly so look in classpath for it
//							ClassLoader classLoader = HibernateUtils.class.getClassLoader();
//							URL url = classLoader.getResource(value);
//							if (url != null) {
//								value = url.getFile();
//								System.out.println("url.getFile()=" + url.getFile());
//								System.out.println("url.getPath()=" + url.getPath());
//								System.out.println("url.getQuery()=" + url.getQuery());
//								System.out.println("url.toString()=" + url.toString());
//
//								System.out.println("url.getRef()=" + url.getRef());
//								System.out.println("url.getProtocol()=" + url.getProtocol());
//								System.out.println("url.toString()=" + url.toString());
//
//							}
//						}
//					}
					
					// Set the system property with the value
					System.setProperty(propertyName, value);
					
					System.out.println("Setting property name " + propertyName 
							+ " to value " + value);
				}
			}
		} catch (IOException e) {
			System.err.println("Exception occurred reading in properties file. "
					+ e.getMessage());
		}
	}
	
	/**
	 * Reads a Java properties file and stores all the properties as system
	 * properties if they haven't yet been set.
	 * 
	 * @param fileName Name of properties file
	 */
	private static void readPropertiesConfigFile(String fileName) {
		try (FileInputStream file = new FileInputStream(fileName)) {
			readPropertiesConfigFile(file);
		} catch (IOException e) {
			System.err.println("Exception occurred reading in fileName " 
					+ fileName + " . " + e.getMessage());
		}
	}
	
	/**
	 * When reading config params should lock up the system so that data is
	 * consistent. This is a good idea because even though each individual
	 * param is atomic there could be situations where multiple interdependent
	 * params are being used and don't want a write to change them between
	 * access.
	 * 
	 * The expectation is that the program is event driven. When an event
	 * is processed then a single call to readLock() and readUnlock() can
	 * protect the entire code where parameters might be read. The intent
	 * is not to read lock each single access of data.
	 * 
	 * Every time readLock() is called there needs to be a corresponding call
	 * to readUnlock(). Since this important the readUnlock() should be in
	 * a finally block. For example:
	 * {@code
	 *   try {
	 *     readLock();
	 *     ...; // Read access the data
	 *   } finally {
	 *     readUnlock();
	 *   }
	 */
	public static void readLock() {
		readLock.lock();
	}
	
	/**
	 * Every time readLock() is called there needs to be a call to readUnlock().
	 * Since this is important the readUnlock() should be called in a finally 
	 * block. See readLock() for details.
	 */
	public static void readUnlock() {
		readLock.unlock();
	}
	
	/**
	 * Processes specified config file and overrides the config parameters
	 * accordingly. This way can use an XML config file instead of -D Java
	 * properties to set configuration parameters.
	 * <p>
	 * Reads in data and stores it in the config objects so that it is
	 * programmatically accessible. Does a write lock so that readers will only
	 * execute when data is not being modified.
	 * 
	 * @param fileName
	 *            Name of the config file. If null then default values will be
	 *            used for each parameter.
	 * @throws ConfigException
	 * @throws ConfigParamException
	 */
	public static void processConfig(String fileName) 
			throws ConfigException, ConfigParamException {
		// Don't want reading while writing so lock access
		writeLock.lock();
		
		System.out.println("Processing configuration file " + fileName);
		
		try {
			// Read in the data from config file
			if (fileName != null)
				if (fileName.endsWith(".xml"))
					readXmlConfigFile(fileName);
				else
					readPropertiesConfigFile(fileName);
		} finally {
			// Make sure the write lock gets unlocked no matter what happens
			writeLock.unlock();
		}
	}
	
	/**
	 * Process the configuration file specified by a file named
	 * transitclock.properties that is in the classpath. The processes
	 * configuration files specified by the Java system property
	 * transitclock.configFiles. The transitclock.configFiles property is a ";"
	 * separated list so can specify multiple files. The files can be either in
	 * either XML or in Java properties format.
	 * <p>
	 * All parameters found will be set as system properties unless the system
	 * property was already set. This way Java system properties set on the
	 * command line have precedence.
	 * <p>
	 * By making all read parameters Java system properties they can be used for
	 * external libraries such as for specifying hibernate or logback config
	 * files.
	 */
	public static void processConfig() {
		// Determine from the Java system property transitime.configFiles 
		// the configuration files to be read
		String configFilesStr = System.getProperty("transitclock.configFiles");
		if (configFilesStr == null)
			return;		
		String configFiles[] = configFilesStr.split(";");
		for (String fileName : configFiles) {
			try {
				// Read in and process the config file
				System.out.println("loading config " + fileName);
				processConfig(fileName);
			} catch (ConfigException | ConfigParamException e) {
				System.err.println("error loading config " + fileName);
				e.printStackTrace();
			}
		}
		
		// If the file transitclock.properties exists in the classpath then
		// process it. This is done after the transitclock.configFiles files
		// are read in so that they have precedence over the file found
		// in the classpath.
		String defaultPropertiesFileName = "transitclock.properties";
		InputStream propertiesInput = ConfigFileReader.class.getClassLoader()
				.getResourceAsStream(defaultPropertiesFileName);
		if (propertiesInput != null) {
			readPropertiesConfigFile(propertiesInput);
		}		
	}
}
