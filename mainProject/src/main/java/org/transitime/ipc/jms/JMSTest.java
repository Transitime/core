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
package org.transitime.ipc.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;

/**
 *
 * @author SkiBu Smith
 *
 */
public class JMSTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.err.println("Starting up JMSTest");
			JMSWrapper tester = JMSWrapper.getJMSWrapper();
			
			MessageConsumer msgConsumer = tester.createTopicConsumer("TopicTest");
			while (true) {
				System.err.println("About to read message");
				System.err.println("Received message from msgConsumer: " + 
						JMSWrapper.receiveTextMessage(msgConsumer));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		} catch (NamingException e2) {
			e2.printStackTrace();
		} catch (JMSException e2) {
			e2.printStackTrace();
		}				
	}

}
