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
package org.transitime.avl;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.configData.AvlConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.jms.JMSWrapper;
import org.transitime.ipc.jms.RestartableMessageProducer;
import org.transitime.modules.Module;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;


/**
 * Subclass of Module to be used when reading AVL data from a feed. 
 * Only functionality is that the module is only run if not in 
 * playback mode.
 * 
 * @author SkiBu Smith
 *
 */
public abstract class AvlModule extends Module {
	// For writing the AVL data to the JMS topic
	protected JMSWrapper jmsWrapper = null;
	protected RestartableMessageProducer msgProducer = null; 

	private static final Logger logger= 
			LoggerFactory.getLogger(AvlModule.class);	

	/********************** Member Functions **************************/

	/**
	 * @param projectId
	 */
	protected AvlModule(String projectId) {
		super(projectId);
		
		// Create the MessageProducer that the AVL data can be written to
		try {
			jmsWrapper = JMSWrapper.getJMSWrapper();
			String jmsTopicName = AvlJmsClient.getTopicName(projectId);
			msgProducer = jmsWrapper.createTopicProducer(jmsTopicName);
		} catch (Exception e) {
			logger.error("Problem when setting up JMSWrapper for the AVL feed", e);			
		}
	}

	/**
	 * Calls the Module.start() method, but only if not in playback mode
	 */
	public void start() {
		// If in playback mode then will be getting avl data from db.
		// Therefore shouldn't be running AVL feed.
		if (Core.inPlaybackMode()) {
			logger.info("In playback mode so not running module {} for projectId={}", 
					getClass().getName(), getProjectId());
			return;
		}
		
		super.start();
	}
	
	/**
	 * Actually reads data from feed and writes it to JMS topic.
	 */
	protected abstract void getAndProcessData();
	
	/** 
	 * Does all of the work for the class. Runs forever and reads in 
	 * AVL data from feed and writes it to the appropriate JMS topic
	 * so that AVL clients can access it.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Started module {} for projectId={}", 
				getClass().getName(), getProjectId());
		
		// Run forever
		while (true) {
			try {
				IntervalTimer timer = new IntervalTimer();
								
				// Process data
				getAndProcessData();
				
				// Wait appropriate amount of time till poll again
				long elapsedMsec = timer.elapsedMsec();
				long sleepTime = 
						AvlConfig.getSecondsBetweenAvlFeedPolling()*Time.MS_PER_SEC - 
						elapsedMsec;
				if (sleepTime < 0) {
					logger.warn("Supposed to have a polling rate of " + 
							AvlConfig.getSecondsBetweenAvlFeedPolling()*Time.MS_PER_SEC +
							" msec but processing previous data took " +
							elapsedMsec + " msec so polling again immediately.");
				} else {
					Thread.sleep(sleepTime);
				}
			} catch (Exception e) {
				logger.error("Error occurred.", e);
			}
		}
	}
	
	/**
	 * Sends the AvlReport object to the JMS topic so that AVL clients can read it.
	 * @param avlReport
	 */
	protected void writeAvlReportToJms(AvlReport avlReport) {
		// Make sure the JMS stuff setup successfully
		if (jmsWrapper == null || msgProducer == null) {
			logger.error("Cannot write AvlReport to JMS because JMS tools " + 
					"were not initialized successfully.");
			return;
		}
			
		// Send the AVL report to the JMS topic
		try {
			msgProducer.sendObjectMessage(avlReport);
		} catch (JMSException e) {
			logger.error("Problem sending AvlReport to the JMS topic", e);
		}		
	}

}
