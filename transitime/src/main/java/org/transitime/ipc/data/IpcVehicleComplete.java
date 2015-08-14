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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.List;

import org.transitime.applications.Core;
import org.transitime.core.BlockAssignmentMethod;
import org.transitime.core.SpatialMatch;
import org.transitime.core.TemporalDifference;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Trip;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * Extension of IpcVehicle class so that all info describing vehicle is
 * included. All this info is put into the VehicleDataCache so that can get
 * whatever data is needed be the remote clients. In particular, includes
 * additional info for SIRI feed. Made a separate class from IpcVehicle since
 * the SIRI feed is not expected to get significant use (it is a poor interface)
 * and don't want to bog down all the other IpcVehicle requests with extraneous
 * data.
 *
 * @author SkiBu Smith
 *
 */
public class IpcVehicleComplete extends IpcVehicleGtfsRealtime {

	private final String routeName;
	private final String originStopId;
	private final String destinationId;
	private final double distanceToNextStop;
	private final double distanceOfNextStopFromTripStart;
	private final double distanceAlongTrip;
	
	private static final long serialVersionUID = 8154105842499551461L;

	/********************** Member Functions **************************/

	/**
	 * The constructor.
	 * 
	 * @param vs The current vehicle state. Must not be null.
	 */
	public IpcVehicleComplete(VehicleState vs) {
		super(vs);
		
		// Determine the route name. Can be null.
		Route route = Core.getInstance().getDbConfig()
				.getRouteById(vs.getRouteId());
		this.routeName = route!=null ? route.getName() : null;
		
		// If vehicle assigned then can set the parameters
		Trip trip = vs.getTrip();
		if (trip != null) {
			List<String> stopIdsForTrip = 
					vs.getTrip().getTripPattern().getStopIds();
			this.originStopId = stopIdsForTrip.get(0);
			this.destinationId = stopIdsForTrip.get(stopIdsForTrip.size()-1);
			
			// Get the match. If match is just after a stop then adjust
			// it to just before the stop so that can determine proper 
			// stop ID and such.
			SpatialMatch match = vs.getMatch().getMatchBeforeStopIfAtStop();
			// If vehicle is at a stop then "next" stop will actually be
			// the current stop.
			this.distanceToNextStop = match.getDistanceRemainingInStopPath();

			// Determine how far from beginning of trip to the next stop
			double sumOfStopPathLengths = 0.0;
			for (int spIdx=0; spIdx <= match.getStopPathIndex(); ++spIdx) {
				sumOfStopPathLengths += trip.getStopPath(spIdx).length();
			}
			this.distanceOfNextStopFromTripStart = sumOfStopPathLengths;
			this.distanceAlongTrip = 
					sumOfStopPathLengths - this.distanceToNextStop;			
		} else {
			// Vehicle not assigned to trip so null out parameters
			this.originStopId = null;
			this.destinationId = null;
			this.distanceToNextStop = Double.NaN;
			this.distanceOfNextStopFromTripStart = Double.NaN;
			this.distanceAlongTrip = Double.NaN;
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
	 * @param tripPatternId
	 * @param directionId
	 * @param headsign
	 * @param predictable
	 * @param realTimeSchdAdh
	 * @param isDelayed
	 * @param isLayover
	 * @param layoverDepartureTime
	 * @param nextStopId
	 * @param nextStopName
	 * @param vehicleType
	 * @param tripStartDateStr
	 * @param atStop
	 * @param atOrNextStopId
	 * @param atOrNextGtfsStopSeq
	 * @param routeName
	 * @param originStopId
	 * @param destinationId
	 * @param distanceToNextStop
	 * @param distanceOfNextStopFromTripStart
	 * @param distanceAlongTrip
	 */
	private IpcVehicleComplete(String blockId,
			BlockAssignmentMethod blockAssignmentMethod, IpcAvl avl,
			float pathHeading, String routeId, String routeShortName,
			String tripId, String tripPatternId, String directionId,
			String headsign, boolean predictable, boolean schedBasedPred,
			TemporalDifference realTimeSchdAdh, boolean isDelayed,
			boolean isLayover, long layoverDepartureTime, String nextStopId,
			String nextStopName, String vehicleType, long tripStartEpochTime,
			boolean atStop, String atOrNextStopId, Integer atOrNextGtfsStopSeq,
			String routeName, String originStopId, String destinationId,
			double distanceToNextStop, double distanceOfNextStopFromTripStart,
			double distanceAlongTrip) {
		super(blockId, blockAssignmentMethod, avl, pathHeading, routeId,
				routeShortName, tripId, tripPatternId, directionId, headsign,
				predictable, schedBasedPred, realTimeSchdAdh, isDelayed,
				isLayover, layoverDepartureTime, nextStopId, nextStopName,
				vehicleType, tripStartEpochTime, atStop, atOrNextStopId,
				atOrNextGtfsStopSeq);

		this.routeName = routeName;
		this.originStopId = originStopId;
		this.destinationId = destinationId;
		this.distanceToNextStop = distanceToNextStop;
		this.distanceOfNextStopFromTripStart = distanceOfNextStopFromTripStart;
		this.distanceAlongTrip = distanceAlongTrip;
	}
	
	/*
	 * SerializationProxy is used so that this class can be immutable and so
	 * that can do versioning of objects.
	 */
	protected static class CompleteVehicleSerializationProxy 
		extends GtfsRealtimeVehicleSerializationProxy {
		// Exact copy of fields of IpcCompleteVehicle enclosing class object
		private String routeName;
		private String originStopId;
		private String destinationId;
		private double distanceToNextStop;
		private double distanceOfNextStopFromTripStart;
		private double distanceAlongTrip;

		private static final short currentSerializationVersion = 0;
		
		private static final long serialVersionUID = 6982458672576764027L;

		private CompleteVehicleSerializationProxy(IpcVehicleComplete v) {
			super(v);
			this.routeName = v.routeName;
			this.originStopId = v.originStopId;
			this.destinationId = v.destinationId;
			this.distanceToNextStop = v.distanceToNextStop;
			this.distanceOfNextStopFromTripStart = v.distanceOfNextStopFromTripStart;
			this.distanceAlongTrip = v.distanceAlongTrip;
		}
		
		/*
		 * When object is serialized writeReplace() causes this
		 * SerializationProxy object to be written. Write it in a custom way
		 * that includes a version ID so that clients and servers can have two
		 * different versions of code.
		 */
		protected void writeObject(java.io.ObjectOutputStream stream)
				throws IOException {
			// Write the data for IpcGtfsRealtimeVehicle super class
			super.writeObject(stream);
			
			// Write the data for this class
			stream.writeShort(currentSerializationVersion);
			
		    stream.writeObject(routeName);
		    stream.writeObject(originStopId);
		    stream.writeObject(destinationId);
		    stream.writeDouble(distanceToNextStop);
		    stream.writeDouble(distanceOfNextStopFromTripStart);
		    stream.writeDouble(distanceAlongTrip);
		}

		/*
		 * Custom method of deserializing a SerializationProy object.
		 */
		protected void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			// Read the data for IpcVehicle super class
			super.readObject(stream);

			// If reading from a newer version of protocol then don't
			// know how to handle it so throw exception
			short readVersion = stream.readShort();			
			if (currentSerializationVersion < readVersion) {
				throw new IOException("Serialization error when reading "
						+ getClass().getSimpleName()
						+ " object. Read version=" + readVersion 
						+ " but currently using software version=" 
						+ currentSerializationVersion);
			}

			// Read in data for this class
			routeName = (String) stream.readObject();
			originStopId = (String) stream.readObject();
			destinationId = (String) stream.readObject();
			distanceToNextStop = stream.readDouble();
			distanceOfNextStopFromTripStart = stream.readDouble();
			distanceAlongTrip = stream.readDouble();
		}
		
		/*
		 * When an object is read in it will be a SerializatProxy object due to
		 * writeReplace() being used by the enclosing class. When such an object
		 * is deserialized this method will be called and the SerializationProxy
		 * object is converted to an enclosing class object.
		 */
		private Object readResolve() {
			return new IpcVehicleComplete(blockId, blockAssignmentMethod, avl,
					heading, routeId, routeShortName, tripId, tripPatternId,
					directionId, headsign, predictable, schedBasedPred,
					realTimeSchdAdh, isDelayed, isLayover,
					layoverDepartureTime, nextStopId, nextStopName,
					vehicleType, tripStartEpochTime, atStop, atOrNextStopId,
					atOrNextGtfsStopSeq, routeName, originStopId,
					destinationId, distanceToNextStop,
					distanceOfNextStopFromTripStart, distanceAlongTrip);
		}

	} // End of class SiriVehicleSerializationProxy
	
	/*
	 * Needed as part of using a SerializationProxy. When IpcSerialVehicle 
	 * object is serialized the SerializationProxy will instead be used.
	 */
	private Object writeReplace() {
		return new CompleteVehicleSerializationProxy(this);
	}

	/*
	 * Needed as part of using a SerializationProxy. Makes sure that Vehicle
	 * object cannot be deserialized without using proxy, thereby eliminating
	 * possibility of such an attack as described in "Effective Java".
	 */
	private void readObject(ObjectInputStream stream)
			throws InvalidObjectException {
		throw new InvalidObjectException("Must use proxy instead");
	}

	public String getRouteName() {
		return routeName;
	}

	public String getOriginStopId() {
		return originStopId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public double getDistanceToNextStop() {
		return distanceToNextStop;
	}

	public double getDistanceOfNextStopFromTripStart() {
		return distanceOfNextStopFromTripStart;
	}
	
	public double getDistanceAlongTrip() {
		return distanceAlongTrip;
	}

	@Override
	public String toString() {
		return "IpcExtVehicle [" 
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
				+ ", isDelayed=" + isDelayed()
				+ ", isLayover=" + isLayover()
				+ ", layoverDepartureTime=" 
					+ Time.timeStrNoTimeZone(getLayoverDepartureTime())
				+ ", nextStopId=" + getNextStopId() 
				+ ", avl=" + getAvl()
				+ ", heading=" + getHeading() 
				+ ", vehicleType=" + getVehicleType()
				+ ", atStop=" + isAtStop()
				+ ", atOrNextStopId=" + getAtOrNextStopId()
				+ ", atOrNextGtfsStopSeq=" + getAtOrNextGtfsStopSeq()
				+ ", tripStartEpochTime=" + getTripStartEpochTime()
				+ ", tripStartEpochTime=" + new Date(getTripStartEpochTime())
				+ ", routeName=" + routeName 
				+ ", originStopId="	+ originStopId 
				+ ", destinationId=" + destinationId
				+ ", distanceToNextStop=" 
					+ Geo.distanceFormat(distanceToNextStop)
				+ ", distanceOfNextStopFromTripStart=" 
					+ Geo.distanceFormat(distanceOfNextStopFromTripStart)
				+ ", distanceAlongTrip=" 
					+ Geo.distanceFormat(distanceAlongTrip) 
				+ "]";
	}

}
