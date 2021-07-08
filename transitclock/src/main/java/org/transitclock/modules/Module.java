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
package org.transitclock.modules;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.utils.threading.NamedThreadFactory;

/**
 * Modules are run in a separate thread and continuously process data. They are
 * initiated by core prediction system by configuring which modules should be
 * started.
 * <p>
 * Each module should inherit from this class and implement run() and a
 * constructor that takes the agencyId as a string parameter.
 * 
 * @author SkiBu Smith
 * 
 */
public abstract class Module implements Runnable {
	protected static final Logger logger = 
			LoggerFactory.getLogger(Module.class);	

	protected final String agencyId;
	
	/**
	 * Constructor. Subclasses must implement a constructor that takes
	 * in agencyId and calls this constructor via super(agencyId).
	 * @param agencyId
	 */
	protected Module(String agencyId) {
		this.agencyId = agencyId;
	}
	
	/**
	 * Returns the agencyId that was set when the module was created.
	 * To be used by subclasses.
	 * @return
	 */
	protected String getAgencyId() {
		return agencyId;
	}
	
	/**
	 * So that each module can have a unique thread name.
	 * This eases debugging.
	 * @return
	 */
	private String threadName() {
		return getClass().getSimpleName();
	}

	/**
	 * Starts up the thread that runs the module by calling the Runnable.run()
	 * method for the Module's subclass.
	 */
	public void start() {
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadName());
		ExecutorService executor = 
				Executors.newSingleThreadExecutor(threadFactory);
		executor.execute(this);
	}
	
	/**
	 * Runs the named module in a separate thread using the default agencyId.
	 * 
	 * @param classname
	 * @return
	 */
	public static boolean start(String classname) {
		// Determine the default agencyId
		String agencyId = AgencyConfig.getAgencyId();
		
		// start the module
		return start(classname, agencyId);
	}
	
	/**
	 * Runs the named module in a separate thread. This is the main interface to
	 * this class.
	 * 
	 * @param classname
	 *            Full classname of the Module subclass to instantiate
	 * @param agencyId
	 *            ID of the agency
	 * @return true if successful. Otherwise false.
	 */
	public static boolean start(String classname, String agencyId) {
		try {
			// Create the module object using reflection by calling the constructor
			// and passing in agencyId
			Class<?> theClass = Class.forName(classname);
			Constructor<?> constructor = theClass.getConstructor(String.class);
			Module module = (Module) constructor.newInstance(agencyId);
			
			// Start the separate thread that allows the module to do all the work
			module.start();
			
			return true;
		} catch (Exception e) {
			logger.error("Could not run module {}", classname, e);
			return false;
		}
	}
		
}
