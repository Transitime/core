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
package org.transitclock.avl;

import java.util.Collection;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.AvlConfig;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.ipc.jms.JMSWrapper;
import org.transitclock.ipc.jms.RestartableMessageProducer;
import org.transitclock.modules.Module;

import javax.jms.JMSException;
import java.util.Collection;


/**
 * Low-level abstract AVL module class that handles the processing
 * of the data. Uses JMS to queue the data if JMS enabled.
 * 
 * @author SkiBu Smith
 * 
 */
public abstract class AvlModule extends Module {
	// For writing the AVL data to the JMS topic
	protected RestartableMessageProducer jmsMsgProducer = null; 

	private static final Logger logger = 
			LoggerFactory.getLogger(AvlModule.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	protected AvlModule(String agencyId) {
		super(agencyId);		
	}
	
	/**
	 * Initializes JMS if need be. Needs to be done from same thread that
	 * JMS is written to. Otherwise get concurrency error. 
	 */
	private void initializeJmsIfNeedTo() {
		// If JMS already initialized then can return
		if (jmsMsgProducer != null)
			return;
		
		// JMS not already initialized so create the MessageProducer 
		// that the AVL data can be written to
		try {
			String jmsTopicName = AvlJmsClientModule.getTopicName(agencyId);
			JMSWrapper jmsWrapper = JMSWrapper.getJMSWrapper();
			jmsMsgProducer = jmsWrapper.createTopicProducer(jmsTopicName);
		} catch (Exception e) {
			logger.error("Problem when setting up JMSWrapper for the AVL feed", e);			
		}

	}
	
	/**
	 * Processes AVL report read from feed. To be called from the subclass for
	 * each AVL report. Can use JMS or bypass it, depending on how configured.
	 */
	protected void processAvlReport(AvlReport avlReport) {
		if (AvlConfig.shouldUseJms()) {
			processAvlReportUsingJms(avlReport);
		} else {
			processAvlReportWithoutJms(avlReport);
		}
	}
	
	/**
	 * Processes entire collection of AVL reports read from feed by calling
	 * processAvlReport() on each one.
	 * 
	 * @param avlReports
	 */
	protected void processAvlReports(Collection<AvlReport> avlReports) {
		for (AvlReport avlReport : avlReports) {
			processAvlReport(avlReport);
		}
	}
	
	/**
	 * Sends the AvlReport object to the JMS topic so that AVL clients can read it.
	 * @param avlReport
	 */
	private void processAvlReportUsingJms(AvlReport avlReport) {
		// Make sure the JMS stuff setup successfully
		initializeJmsIfNeedTo();
		if (jmsMsgProducer == null) {
			logger.error("Cannot write AvlReport to JMS because JMS tools " + 
					"were not initialized successfully.");
			return;
		}
			
		// Send the AVL report to the JMS topic
		try {
			jmsMsgProducer.sendObjectMessage(avlReport);
		} catch (JMSException e) {
			logger.error("Problem sending AvlReport to the JMS topic", e);
		}		
	}

	/**
	 * Instead of writing AVL report to JMS topic this method directly processes
	 * it. By doing this one can bypass the need for a JMS server. Uses a thread
	 * executor so that can both use multiple threads and queue up requests.
	 * This is especially important if getting a dump of AVL data from an AVL
	 * feed hitting the Transitime web server and the AVL data getting then
	 * pushed to the core system in batches.
	 * 
	 * @param avlReport
	 *            The AVL report to be processed
	 */
	private void processAvlReportWithoutJms(AvlReport avlReport) {
		// Use AvlExecutor to actually process the data using a thread executor
		AvlExecutor.getInstance().processAvlReport(avlReport);	
	}
}
