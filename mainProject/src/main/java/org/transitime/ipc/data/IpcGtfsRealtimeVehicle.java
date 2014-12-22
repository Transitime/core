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

package org.transitime.ipc.data;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.transitime.core.BlockAssignmentMethod;
import org.transitime.core.SpatialMatch;
import org.transitime.core.TemporalDifference;
import org.transitime.core.TemporalMatch;
import org.transitime.core.VehicleAtStopInfo;
import org.transitime.core.VehicleState;
import org.transitime.utils.Time;


/**
 * Extension of IpcVehicle class so that additional info can be provided for
 * GTFS-realtime feed. Made a separate class from IpcVehicle since the expect
 * other interfaces, such as the JSON/XML interface, to be dominant and don't
 * want to bog down all the other IpcVehicle requests with extraneous data.
 *
 * @author SkiBu Smith
 *
 */
public class IpcGtfsRealtimeVehicle extends IpcVehicle {

	// Null if not at stop
	private final String atStopId; 
	// For GTFS-rt to disambiguate trips
	private final String tripStartDateStr; 

	// For outputting date in GTFS-realtime format
	private static SimpleDateFormat gtfsRealtimeDateFormatter = 
			new SimpleDateFormat("yyyyMMdd");
	
	private static final long serialVersionUID = -6611046660260490100L;

	/********************** Member Functions **************************/

	/**
	 * The constructor used on the server side
	 * 
	 * @param vs
	 */
	public IpcGtfsRealtimeVehicle(VehicleState vs) {
		super(vs);

		// Get the match. If match is just after a stop then adjust
		// it to just before the stop so that can determine proper 
		// stop ID and such.
		TemporalMatch temporalMatch = vs.getMatch();
		if (temporalMatch != null) {
			SpatialMatch match = vs.getMatch().getMatchBeforeStopIfAtStop();
			
			VehicleAtStopInfo stopInfo = match.getAtStop();
			this.atStopId = stopInfo != null ? stopInfo.getStopId() : null;
	
			this.tripStartDateStr = gtfsRealtimeDateFormatter.format(
					new Date(getTripStartEpochTime()));
		} else {
			this.atStopId = null;
			this.tripStartDateStr = null;
		}
	}

	/**
	 * Constructor used for when deserializing a proxy object.
	 * 
	 * @param blockId
	 * @param blockAssignmentMethod
	 * @param avl
	 * @param pathHeading
	 * @param routeId
	 * @param routeShortName
	 * @param tripId
	 * @param tripStartDateStr
	 * @param tripPatternId
	 * @param directionId
	 * @param headsign
	 * @param predictable
	 * @param schedBasedPred
	 * @param realTimeSchdAdh
	 * @param isLayover
	 * @param layoverDepartureTime
	 * @param nextStopId
	 * @param vehicleType
	 * @param atStopId
	 */
	protected IpcGtfsRealtimeVehicle(String blockId,
			BlockAssignmentMethod blockAssignmentMethod, IpcAvl avl,
			float pathHeading, String routeId, String routeShortName,
			String tripId, String tripPatternId,
			String directionId, String headsign, boolean predictable,
			boolean schedBasedPred, TemporalDifference realTimeSchdAdh,
			boolean isLayover, long layoverDepartureTime, String nextStopId,
			String vehicleType, String tripStartDateStr, String atStopId) {
		super(blockId, blockAssignmentMethod, avl, pathHeading, routeId,
				routeShortName, tripId, tripPatternId,
				directionId, headsign, predictable, schedBasedPred,
				realTimeSchdAdh, isLayover, layoverDepartureTime, nextStopId,
				vehicleType);
		this.tripStartDateStr = tripStartDateStr;
		this.atStopId = atStopId;
	}
	
	/*
	 * SerializationProxy is used so that this class can be immutable and so
	 * that can do versioning of objects.
	 */
	protected static class GtfsRealtimeVehicleSerializationProxy 
			extends SerializationProxy {
		protected String atStopId; 
		protected String tripStartDateStr; 

		private static final long serialVersionUID = 5804716921925188073L;

		protected GtfsRealtimeVehicleSerializationProxy(IpcGtfsRealtimeVehicle v) {
			super(v);
			this.atStopId = v.atStopId;
			this.tripStartDateStr = v.tripStartDateStr;
		}
		
		/*
		 * When object is serialized writeReplace() causes this
		 * SerializationProxy object to be written. Write it in a custom way
		 * that includes a version ID so that clients and servers can have two
		 * different versions of code.
		 */
		protected void writeObject(java.io.ObjectOutputStream stream)
				throws IOException {
			// Write the data for IpcVehicle super class
			super.writeObject(stream);
			
			// Write the data for this class
		    stream.writeObject(atStopId);
		    stream.writeObject(tripStartDateStr);
		}

		/*
		 * Custom method of deserializing a SerializationProy object.
		 */
		protected void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			// Read the data for IpcVehicle super class
			super.readObject(stream);

			// Read in data for this class
			atStopId = (String) stream.readObject();
			tripStartDateStr = (String) stream.readObject();
		}
		
		/*
		 * When an object is read in it will be a SerializatProxy object due to
		 * writeReplace() being used by the enclosing class. When such an object
		 * is deserialized this method will be called and the SerializationProxy
		 * object is converted to an enclosing class object.
		 */
		private Object readResolve() {
			return new IpcGtfsRealtimeVehicle(blockId, blockAssignmentMethod,
					avl, heading, routeId, routeShortName, tripId,
					tripPatternId, directionId, headsign, predictable,
					schedBasedPred, realTimeSchdAdh, isLayover,
					layoverDepartureTime, nextStopId, vehicleType,
					tripStartDateStr, atStopId);
		}

	} // End of class GtfsRealtimeVehicleSerializationProxy
	
	public String getTripStartDateStr() {
		return tripStartDateStr;
	}
	
	/**
	 * If vehicle currently at stop then returns the stop ID. Otherwise returns
	 * null.
	 * 
	 * @return
	 */
	public String getAtStopId() {
		return atStopId;
	}

	@Override
	public String toString() {
		return "IpcGtfsRealtimeVehicle [" 
				+ "vehicleId=" + getId()
				+ ", blockId=" + getBlockId() 
				+ ", blockAssignmentMethod=" + getBlockAssignmentMethod()
				+ ", routeId=" + getRouteId()
				+ ", routeShortName=" + getRouteShortName()
				+ ", tripId=" + getTripId()
				+ ", tripPatternId=" + getTripPatternId()
				+ ", directionId=" + getDirectionId()
				+ ", headsign=" + getHeadsign()
				+ ", predictable=" + isPredictable()
				+ ", schedBasedPred=" + isForSchedBasedPred()
				+ ", realTimeSchedAdh=" + getRealTimeSchedAdh() 
				+ ", isLayover=" + isLayover()
				+ ", layoverDepartureTime=" 
					+ Time.timeStrNoTimeZone(getLayoverDepartureTime())
				+ ", nextStopId=" + getNextStopId() 
				+ ", avl=" + getAvl()
				+ ", heading=" + getHeading() 
				+ ", vehicleType=" + getVehicleType()
				+ ", atStopId=" + atStopId
				+ ", tripStartDateStr=" + tripStartDateStr 
				+ "]";
	}
	

}
