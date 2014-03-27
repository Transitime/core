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
package org.transitime.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;

/**
 * Just for debugging logging
 * 
 * @author SkiBu Smith
 *
 */
public class LoggingTest {
	private static final Logger s_logger= LoggerFactory.getLogger(Core.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Can easily access whether debug is enabled
		@SuppressWarnings("unused")
		boolean debug = s_logger.isDebugEnabled();

		// Play around with logging a big
		s_logger.info("Core Initializing Core...");
		float f = 1.0f/3.0f;
		s_logger.info("Core one third={}", String.format("%.3f", f));
		s_logger.debug("Core debug message");
		s_logger.error(Markers.email(), "Core error message", new Exception("special exception message"));
		s_logger.error(Markers.email(), "2nd Core error message", new Exception("2nd special exception message"));
	}

}
