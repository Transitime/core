/**
 * 
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
package org.transitime.gtfs;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;


/**
 * Special logging appender. Idea is to overload the logback logging so that
 * when error() or warn() called can do the usual logging but also
 * record errors and warnings to lists and then be able to output those
 * lists in coherent forms at the end of processing. This important 
 * information can then be more effectively presented to the user, 
 * hopefully causing them to fix the configuration problems.
 * 
 * To use create a logbackGtfs.xml file with the following:
 *   <appender name="GTFS_CUSTOM_APPENDER" class="org.transitime.gtfs.GtfsLoggingAppender">
 *   </appender>
 *   
 *   <logger name="org.transitime.gtfs" level="debug" additivity="true">
 *     <appender-ref ref="GTFS_CUSTOM_APPENDER" />
 *   </logger>
 *
 * And use the special gtfsLogback by setting a VM arg to something like:
 *   -Dlogback.configurationFile=C:/Users/Mike/git/testProject/testProject/src/main/config/logbackGtfs.xml
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsLoggingAppender extends AppenderBase<ILoggingEvent> {
	// Where to store the errors and warnings
	private static List<String> warnings = new ArrayList<String>();
	private static List<String> errors = new ArrayList<String>();
	
	/********************** Member Functions **************************/

	/**
	 * Logs WARN and ERROR level messages
	 * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
	 */
	@Override
	protected void append(ILoggingEvent event) {
		Level level = event.getLevel();
		if (level == Level.WARN) {
			warnings.add(event.getFormattedMessage());
		} else if (level == Level.ERROR) {
			errors.add(event.getFormattedMessage());
		}
	}
	
	/**
	 * @return list of errors processed so far
	 */
	public static List<String> getErrors() {
		return errors;
	}
	
	/**
	 * @return list of warnings processed so far
	 */
	public static List<String> getWarnings() {
		return warnings;
	}
	
	/**
	 * For debugging. Dumps all the errors and warnings to stdout
	 */
	public static void outputMessagesToSysErr() {
		System.err.println("Errors:");
		if (errors.isEmpty())
			System.err.println("  None");
		for (String s : errors) 
			System.err.println("  " + s);
		
		System.err.println();
		
		System.err.println("Warnings:");
		if (warnings.isEmpty())
			System.err.println("  None");
		for (String s : warnings) 
			System.err.println("  " + s);
	}
}
