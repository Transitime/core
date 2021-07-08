/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.data.siri;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.transitclock.api.utils.AgencyTimezoneCache;
import org.transitclock.ipc.data.IpcVehicleComplete;
import org.transitclock.utils.Time;

/**
 * Top level XML element for SIRI VehicleMonitoring command.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "Siri")
@XmlType(propOrder = { "version", "xmlns", "delivery" })
public class SiriVehiclesMonitoring {

	@XmlAttribute
	private String version = "1.3";

	@XmlAttribute
	private String xmlns = "http://www.siri.org.uk/siri";

	@XmlElement(name = "ServiceDelivery")
	private SiriServiceDelivery delivery;

	@XmlTransient
	// Defines how times should be output in Siri
	private final DateFormat siriDateTimeFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXXX");

	// Defines how dates should be output in Siri
	@XmlTransient
	private final DateFormat siriDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
 
	/**
	 * Simple sub-element so using internal class.
	 */
	private static class SiriServiceDelivery {
		@XmlElement(name = "ResponseTimestamp")
		private String responseTimestamp;

		@XmlElement(name = "VehicleMonitoringDelivery ")
		private SiriVehicleMonitoringDelivery vehicleMonitoringDelivery;

		/**
		 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
		 * obtuse "MessageBodyWriter not found for media type=application/json"
		 * exception.
		 */
		@SuppressWarnings("unused")
		protected SiriServiceDelivery() {
		}

		public SiriServiceDelivery(Collection<IpcVehicleComplete> vehicles,
				String agencyId, DateFormat timeFormatter, DateFormat dateFormatter) {
			responseTimestamp =
					timeFormatter.format(new Date(System
							.currentTimeMillis()));
			vehicleMonitoringDelivery =
					new SiriVehicleMonitoringDelivery(vehicles, agencyId,
							timeFormatter, dateFormatter);
		}
	}

	/**
	 * Simple sub-element so using internal class.
	 */
	private static class SiriVehicleMonitoringDelivery {
		// Required by SIRI spec
		@XmlAttribute
		private String version = "1.3";

		// Required by SIRI spec
		@XmlElement(name = "ResponseTimestamp")
		private String responseTimestamp;

		// Required by SIRI spec
		@XmlElement(name = "ValidUntil")
		private String validUntil;

		@XmlElement(name = "VehicleActivity")
		private List<SiriVehicleActivity> vehicleActivityList;

		/**
		 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
		 * obtuse "MessageBodyWriter not found for media type=application/json"
		 * exception.
		 */
		@SuppressWarnings("unused")
		protected SiriVehicleMonitoringDelivery() {
		}

		public SiriVehicleMonitoringDelivery(
				Collection<IpcVehicleComplete> vehicles, String agencyId,
				DateFormat timeFormatter, DateFormat dateFormatter) {
			long currentTime = System.currentTimeMillis();
			responseTimestamp = timeFormatter.format(new Date(currentTime));
			validUntil = timeFormatter.format(new Date(currentTime + 2 * Time.MS_PER_MIN));

			vehicleActivityList = new ArrayList<SiriVehicleActivity>();
			for (IpcVehicleComplete vehicle : vehicles) {
				vehicleActivityList.add(new SiriVehicleActivity(vehicle,
						agencyId, timeFormatter, dateFormatter));
			}
		}
	}

	/**
	 * Simple sub-element so using internal class.
	 */
	private static class SiriVehicleActivity {
		// GPS time for vehicle
		@XmlElement(name = "RecordedAtTime")
		private String recordedAtTime;

		// Addition SIRI MonitoredVehicleJourney element
		@XmlElement(name = "MonitoredVehicleJourney")
		private SiriMonitoredVehicleJourney monitoredVehicleJourney;

		/**
		 * Need a no-arg constructor for Jersey for JSON. Otherwise get really
		 * obtuse "MessageBodyWriter not found for media type=application/json"
		 * exception.
		 */
		@SuppressWarnings("unused")
		protected SiriVehicleActivity() {
		}

		public SiriVehicleActivity(IpcVehicleComplete ipcCompleteVehicle,
				String agencyId,
				DateFormat timeFormatter, DateFormat dateFormatter) {
			recordedAtTime =
					timeFormatter.format(new Date(ipcCompleteVehicle
							.getGpsTime()));
			monitoredVehicleJourney =
					new SiriMonitoredVehicleJourney(ipcCompleteVehicle, null,
							agencyId, timeFormatter, dateFormatter);
		}
	}

	/********************** Member Functions **************************/

	// No-args needed because this class is an XML root element
	protected SiriVehiclesMonitoring() {
	}

	public SiriVehiclesMonitoring(Collection<IpcVehicleComplete> vehicles,
			String agencyId) {
		// Set the time zones for the date formatters
		siriDateTimeFormat.setTimeZone(AgencyTimezoneCache.get(agencyId));
		siriDateFormat.setTimeZone(AgencyTimezoneCache.get(agencyId));

		delivery =
				new SiriServiceDelivery(vehicles, agencyId, siriDateTimeFormat,
						siriDateFormat);
	}
}
