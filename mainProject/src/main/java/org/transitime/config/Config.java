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
package org.transitime.config;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.ConfigValue.ConfigParamException;
import org.transitime.configData.CoreConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Config class provides a way of handling parameters that
 * can be updated without having to restart the application. The
 * parameters can be access efficiently so that they can be accessed
 * in loops that are frequently executed. This class is useful for
 * parameters that are system wide and often have a default. Such
 * params are not best stored in a database because with a db it
 * can be harder to see what all the values are or to copy the values
 * from one project to another. It can also be harder to keep track
 * of changes when such params are in a database.
 * 
 * An additional goal of Config is for all params to be processed
 * when the XML file is read, both at startup and when file is reread.
 * This is important because then if there are any processing
 * problems those problems are noticed and must be dealt with
 * immediately (instead of them occurring in an lazy fashion,
 * perhaps when no one is available to deal with them). But this
 * goal would mean that the params would need to be defined in 
 * config files so they would all be dealt with at startup. For
 * many situations want the params to be defined where they are used
 * since that would make them easier to support. But then they wouldn't
 * get initialized until the Java class is actually accessed.
 * 
 * The params are configured in an XML file. Multiple XML files can be
 * used. Each XML file should have a corresponding configuration class
 * such as CoreConfig.java. In the XXXConfig.java class the params
 * are specified along with default values.
 * 
 * Multiple threads can read parameters simultaneously. The threads
 * should call readLock() if it is important to update the parameters
 * in a consistent way, such as when more than a single parameter
 * are interrelated.
 * 
 * In the future could use a different type of configuration file,
 * such as a standard Properties file.
 * 
 * @author SkiBu Smith
 *
 */
public class Config {
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
    
    // So can do error and debug logging
    private static final Logger logger= LoggerFactory.getLogger(Config.class);
    
	// Stores all of the values so that they can be read in with a
	// single function call. This way the values only need to be defined
	// in one place. Makes maintenance easier.
    private static ArrayList<ConfigValue<?>> configValuesList = 
			new ArrayList<ConfigValue<?>>();

	// Contains the data read from the config files
	private static HashMap<String, List<String>> configFileData = 
			new HashMap<String, List<String>>();
	
	/********************** Member Functions **************************/

	/**
	 * So that the parameter classes can add created parameters to the
	 * list.
	 * @return
	 */
	public static ArrayList<ConfigValue<?>> getConfigValuesList() {
		return configValuesList;
	}
	
	public static HashMap<String, List<String>> getConfigFileData() {
		return configFileData;
	}
	
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
	 * Reads the specified XML file and stores the results into a HashMap configFileData.
	 * The file is expected to be very simple. Attributes are ignored. Only
	 * values are used. The keys for the resulting HashMap are based on
	 * the XML tags and their nesting. So if one is setting a value of 75
	 * to a param with a key transitime.predictor.allowableDistanceFromPath 
	 * the XML would look like:
	 * {@code
	 * <transitime>
	 *   <predictor>
	 *     <allowableDistanceFromPath>
	 *       75.0
	 *     </allowableDistanceFromPath>
	 *   </predictor>
	 * </transitime>
	 * }
	 * @param fileName
	 */
	public static void readConfigFile(String fileName) 
			throws ConfigException {
		File xmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc;
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
		} catch (Exception e) {
			logger.error("Error parasing XML file \"" + fileName + "\" : " + 
					e.getMessage(), e);
			
			throw new ConfigException(e);
		}
		
		// So text strings separated by newlines are not 
		// considered separate text nodes
		doc.getDocumentElement().normalize();
	 
		// For keeping track of names
		List<String> names = new ArrayList<String>();
		// Where to start in hierarchy when recursively calling processChild()
		NodeList nodeList = doc.getChildNodes();
		processChildren(nodeList, names, configFileData);
				
		// Now that have read data from config file, output data for debugging
		if (logger.isDebugEnabled()) {
			logger.debug("Config data from xml file:");
			Set<String> keys = configFileData.keySet();
			for (String key : keys) {
				List<String> values = configFileData.get(key);
				String resultStr = "key=" + key + " value=";
				for (String value : values) {
					resultStr += value + ", ";
				}
				logger.debug(resultStr);
			}
			logger.debug(""); // To separate out chunks in log file
		}
	}
	
	/**
	 * If want to reread the config files then should first call
	 * this method so that all old data is wiped out. Otherwise
	 * if a parameter is removed from config file would still
	 * be using its old value.
	 */
	public static void resetConfigFileData() {
		configFileData = new HashMap<String, List<String>>();
	}
	
	/**
	 * processChildren() is called recursively to process data from 
	 * an xml file. 
	 * @param nodeList The data from the XML file that was processed by the 
	 * DOM XML processor
	 * @param names For keeping track of the full hierarchical parameter name
	 * such as "transitest.predictor.maxDistance".
	 * @param values Contains the resulting full hierarchical parameter names
	 * as keys and the parameter values as the values. Since a parameter can
	 * have a list of values the value is a List<String> 
	 */
	private static void processChildren(NodeList nodeList, 
										List<String> names, 
										HashMap<String, List<String>> values) {
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
				processChildren(childNodeList, names, values);				
			} else {				
				// Determine full name of parameter by appending node
				// names together, separated by a period. So get something
				// like "transitime.predictor.radius".
				StringBuilder propertyName = new StringBuilder("");
				for (int j=0; j<names.size(); ++j) {
					String name = names.get(j);
					propertyName.append(name);
					if (j != names.size()-1)
						propertyName.append(".");
				}
				String propertyNameStr = propertyName.toString();
				
				// Determine value specified in xml file
				String value = node.getTextContent().trim();
				
				// Add property to valuesForId.
				// Note that can handle arrays by list the same param 
				// multiple times.
				List<String> valuesForId = values.get(propertyNameStr);
				if (valuesForId == null) {
					valuesForId = new ArrayList<String>(); 
					values.put(propertyNameStr, valuesForId);
				}
				valuesForId.add(value);
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
	 * Processes config files. Reads in data and stores it in the config
	 * objects so that it is programmatically accessible. Does a write
	 * lock so that readers will only execute when data is not being
	 * modified.
	 * @param fileName Name of the config file. If null then default
	 * values will be used for each parameter.
	 * @throws ConfigException
	 * @throws ConfigParamException
	 */
	public static void processConfig(String fileName) 
			throws ConfigException, ConfigParamException {
		// Don't want reading while writing so lock access
		writeLock.lock();
		
		try {
			// Read in the data from config file
			if (fileName != null)
				readConfigFile(fileName);
		} finally {
			// Make sure the write lock gets unlocked no matter what happens
			writeLock.unlock();
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
	 * For testing
	 * @param args args[0] is name of file to be read in
	 */
	public static void main(String[] args) {
		// Get name of config file to parse
		String fileName = args[0];
		
		try {
			// Read in the data from config file
			processConfig(fileName);

			// Output the test results
			int i = CoreConfig.getIntTest();
			float f = CoreConfig.getFloatTest();
			String s = CoreConfig.getStringTest();
			List<String> sList = CoreConfig.getStringListTest();
			System.out.println("i=" + i + " f=" + f + " s=\"" + s + "\" sList=" + sList);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
