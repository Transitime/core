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
import org.transitime.db.structs.Trip;

/**
 * Extension of IpcVehicle class so that additional info can be
 * provided for SIRI feed.
 *
 * @author SkiBu Smith
 *
 */
public class IpcExtVehicle extends IpcVehicle {

	private final String routeName;
	private final String originStopId;
	private final String destinationId;
	private final String destinationName;
	private final String nextStopId;
	private final double distanceToNextStop;
	private final double distanceAlongTrip;
	private final boolean isLayover;
	private final long tripStartEpochTime;
	
	private static final long serialVersionUID = 8154105842499551461L;

	/********************** Member Functions **************************/

	public IpcExtVehicle(VehicleState vs) {
		super(vs);
		
		this.routeName = Core.getInstance().getDbConfig()
				.getRouteById(vs.getRouteId()).getName();
		
		// If vehicle assigned then can set the parameters
		Trip trip = vs.getTrip();
		if (trip != null) {
			List<String> stopIdsForTrip = 
					vs.getTrip().getTripPattern().getStopIds();
			this.originStopId = stopIdsForTrip.get(0);
			this.destinationId = stopIdsForTrip.get(stopIdsForTrip.size()-1);
			this.destinationName = vs.getTrip().getHeadsign();
			
			// Get the match. If match is just after a stop then adjust
			// it to just before the stop so that can determine proper 
			// stop ID and such.
			SpatialMatch match = vs.getMatch().getMatchBeforeStopIfAtStop();
			// If vehicle is at a stop then "next" stop will actually be
			// the current stop.
			this.nextStopId = match.getStopPath().getStopId();
			this.distanceToNextStop = match.getDistanceRemainingInStopPath();

			// Determine how far traveled along trip
			double distanceForPreviousStopPaths = 0.0;
			for (int spIdx=0; spIdx < match.getStopPathIndex(); ++ spIdx) {
				distanceForPreviousStopPaths += trip.getStopPath(spIdx).length();
			}
			this.distanceAlongTrip = 
					distanceForPreviousStopPaths + this.distanceToNextStop;
			
			this.isLayover = match.isLayover();
			this.tripStartEpochTime = Core.getInstance().getTime()
					.getEpochTime(trip.getStartTime(), new Date());
			
		} else {
			// Vehicle not assigned to trip so null out parameters
			this.originStopId = null;
			this.destinationId = null;
			this.destinationName = null;
			this.nextStopId = null;
			this.distanceToNextStop = Double.NaN;
			this.distanceAlongTrip = Double.NaN;
			this.isLayover = false;
			this.tripStartEpochTime = 0;
		}
	}
	
	/**
	 * Constructor used for when deserializing a proxy object. 
	 *
	 * @param routeName
	 * @param originStopId
	 * @param destinationId
	 * @param destinationName
	 * @param nextStopId
	 * @param distanceToNextStop
	 * @param distanceAlongTrip
	 * @param isLayover
	 * @param tripStartEpochTime
	 * @param blockId
	 * @param blockAssignmentMethod
	 * @param avl
	 * @param pathHeading
	 * @param routeId
	 * @param routeShortName
	 * @param tripId
	 * @param directionId
	 * @param predictable
	 * @param realTimeSchdAdh
	 */
	private IpcExtVehicle(String routeName, String originStopId,
			String destinationId, String destinationName,
			String nextStopId, double distanceToNextStop,
			double distanceAlongTrip, boolean isLayover, long tripStartEpochTime,
			String blockId, BlockAssignmentMethod blockAssignmentMethod, 
			IpcAvl avl, float pathHeading, String routeId, String routeShortName,
			String tripId, String directionId, boolean predictable,
			TemporalDifference realTimeSchdAdh) {
		super(blockId, blockAssignmentMethod, avl, pathHeading, routeId,
				routeShortName, tripId, directionId, predictable,
				realTimeSchdAdh);
		
		this.routeName = routeName;
		this.originStopId = originStopId;
		this.destinationId = destinationId;
		this.destinationName = destinationName;
		this.nextStopId = nextStopId;
		this.distanceToNextStop = distanceToNextStop;
		this.distanceAlongTrip = distanceAlongTrip;
		this.isLayover = isLayover;
		this.tripStartEpochTime = tripStartEpochTime;
	}
	
	/*
	 * SerializationProxy is used so that this class can be immutable and so
	 * that can do versioning of objects.
	 */
	protected static class SiriVehicleSerializationProxy 
		extends SerializationProxy {
		// Exact copy of fields of IpcSiriVehicle enclosing class object
		private String routeName;
		private String originStopId;
		private String destinationId;
		private String destinationName;
		private String nextStopId;
		private double distanceToNextStop;
		private double distanceAlongTrip;
		private boolean isLayover;
		private long tripStartEpochTime;

		private static final long serialVersionUID = 6982458672576764027L;

		private SiriVehicleSerializationProxy(IpcExtVehicle v) {
			super(v);
			this.routeName = v.routeName;
			this.originStopId = v.originStopId;
			this.destinationId = v.destinationId;
			this.destinationName = v.destinationName;
			this.nextStopId = v.nextStopId;
			this.distanceToNextStop = v.distanceToNextStop;
			this.distanceAlongTrip = v.distanceAlongTrip;
			this.isLayover = v.isLayover;
			this.tripStartEpochTime = v.tripStartEpochTime;
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
		    stream.writeObject(routeName);
		    stream.writeObject(originStopId);
		    stream.writeObject(destinationId);
		    stream.writeObject(destinationName);
		    stream.writeObject(nextStopId);
		    stream.writeDouble(distanceToNextStop);
		    stream.writeDouble(distanceAlongTrip);
		    stream.writeBoolean(isLayover);
		    stream.writeLong(tripStartEpochTime);
		}

		/*
		 * Custom method of deserializing a SerializationProy object.
		 */
		protected void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			// Read the data for IpcVehicle super class
			super.readObject(stream);

			// Read in data for this class
			routeName = (String) stream.readObject();
			originStopId = (String) stream.readObject();
			destinationId = (String) stream.readObject();
			destinationName = (String) stream.readObject();
			nextStopId = (String) stream.readObject();
			distanceToNextStop = stream.readDouble();
			distanceAlongTrip = stream.readDouble();
			isLayover = stream.readBoolean();
			tripStartEpochTime = stream.readLong();
		}
		
		/*
		 * When an object is read in it will be a SerializatProxy object due to
		 * writeReplace() being used by the enclosing class. When such an object
		 * is deserialized this method will be called and the SerializationProxy
		 * object is converted to an enclosing class object.
		 */
		private Object readResolve() {
			return new IpcExtVehicle(routeName, originStopId, destinationId,
					destinationName, nextStopId, distanceToNextStop,
					distanceAlongTrip, isLayover, tripStartEpochTime, blockId,
					blockAssignmentMethod, avl, pathHeading, routeId,
					routeShortName, tripId, directionId, predictable,
					realTimeSchdAdh);
		}

	} // End of class SiriVehicleSerializationProxy
	
	/*
	 * Needed as part of using a SerializationProxy. When IpcSerialVehicle 
	 * object is serialized the SerializationProxy will instead be used.
	 */
	private Object writeReplace() {
		return new SiriVehicleSerializationProxy(this);
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

	public String getDestinationName() {
		return destinationName;
	}

	public String getNextStopId() {
		return nextStopId;
	}

	public double getDistanceToNextStop() {
		return distanceToNextStop;
	}

	public double getDistanceAlongTrip() {
		return distanceAlongTrip;
	}

	public boolean isLayover() {
		return isLayover;
	}

	public long getTripStartEpochTime() {
		return tripStartEpochTime;
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
				+ ", predictable=" + isPredictable()
				+ ", realTimeSchedAdh=" + getRealTimeSchedAdh() 
				+ ", avl=" + getAvl()
				+ ", pathHeading=" + getPathHeading() 
				+ ", routeName=" + routeName 
				+ ", originStopId="	+ originStopId 
				+ ", destinationId=" + destinationId
				+ ", destinationName=" + destinationName 
				+ ", nextStopId=" + nextStopId 
				+ ", distanceToNextStop=" + distanceToNextStop
				+ ", distanceAlongTrip=" + distanceAlongTrip 
				+ ", isLayover=" + isLayover
				+ ", tripStartEpochTime=" + new Date(tripStartEpochTime)
				+ "]";
	}

}
