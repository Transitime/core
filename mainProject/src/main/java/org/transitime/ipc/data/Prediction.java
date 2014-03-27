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
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.structs.Trip;
import org.transitime.utils.StringUtils;
import org.transitime.utils.Time;

/**
 * Contains information on a single prediction. For providing info to client.
 * <p>
 * Declared serializable since using RMI to pass back Prediction objects and
 * RMI uses serialization.
 * 
 * @author SkiBu Smith
 * 
 */
public class Prediction implements Serializable {

	private final String vehicleId;
	private final String stopId;
	// stopName isn't truly needed but it can make debugging much simpler
	// so it has been added. It is only available on the server side since
	// want to send as little info as possible to client side, especially
	// when GTFS-RT feed is provided, which is really data intensive.
	private final String stopName;
	private final int stopSequence;
	private final String routeId;
	// routeShortName needed because routeId is sometimes not consistent over
	// schedule changes but routeShortName usually is.
	private final String routeShortName;
	private final String tripId;
	private final String blockId;
	private final long predictionTime;
	// The time of the fix so can tell how stale prediction is
	private final long avlTime;
	// The time the AVL data was processed and the prediction was created.
	private final long creationTime;
	private final boolean affectedByWaitStop;
	private final String driverId;
	private final short passengerCount;
	private final float passengerFullness;
	private final boolean isArrival;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(Prediction.class);

	private static final long serialVersionUID = 7264507678733060173L;

	/********************** Member Functions **************************/

	/**
	 * Constructs a Prediction object.
	 * 
	 * @param vehicleId
	 * @param stopId
	 * @param stopSequence
	 * @param trip
	 *            Can be set to null for testing but usually will be a valid
	 *            trip
	 * @param predictionTime
	 * @param avlTime
	 * @param creationTime
	 * @param predictionAffectedByWaitStop
	 * @param driverId
	 * @param passengerCount
	 * @param passengerFullness
	 * @param isArrival
	 */
	public Prediction(String vehicleId, String stopId, int stopSequence,
			Trip trip, long predictionTime, long avlTime, long creationTime,
			boolean predictionAffectedByWaitStop, String driverId,
			int passengerCount, float passengerFullness, boolean isArrival) {
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		this.stopName = Core.getInstance().getDbConfig().getStop(stopId).getName();
		this.stopSequence = stopSequence;
		this.routeId = trip != null ? trip.getRouteId() : null;
		this.routeShortName = trip != null ? trip.getRouteShortName() : null;
		// For when trip is null use "" instead of null for the tripId
		// so that when getting all predictions code for telling when 
		// tripId changes will still work when debugging. 
		this.tripId = trip != null ? trip.getId() : ""; 
		this.blockId = trip != null ? trip.getBlockId() : null;
		this.predictionTime = predictionTime;
		this.avlTime = avlTime;
		this.creationTime = creationTime;
		this.affectedByWaitStop = predictionAffectedByWaitStop;
		this.driverId = driverId;
		this.passengerCount = (short) passengerCount;
		this.passengerFullness = passengerFullness;
		this.isArrival = isArrival;
		
		// Log each creation of a Prediction
		logger.info(this.toString());
	}

	/**
	 * Constructor used for when deserializing a proxy object. Declared
	 * private because only used internally by the proxy class.
	 */
	private Prediction(String vehicleId, String stopId, int stopSequence,
			String routeId, String routeShortName, String tripId,
			String blockId, long predictionTime, long avlTime,
			long creationTime, boolean affectedByWaitStop, 
			String driverId, short passengerCount, float passengerFullness,
			boolean isArrival) {
		this.vehicleId = vehicleId;
		this.stopId = stopId;
		// stopName is only for debugging on the client side
		this.stopName = null;
		this.stopSequence = stopSequence;
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.tripId = tripId;
		this.blockId = blockId;
		this.predictionTime = predictionTime;
		this.avlTime = avlTime;
		this.creationTime = creationTime;
		this.affectedByWaitStop = affectedByWaitStop;
		this.driverId = driverId;
		this.passengerCount = passengerCount;
		this.passengerFullness = passengerFullness;
		this.isArrival = isArrival;
	}
	
	/**
	 * SerializationProxy is used so that this class can be immutable
	 * and so that can do versioning of objects.
	 */
	private static class SerializationProxy implements Serializable {
		// Exact copy of fields of Prediction enclosing class object
		private String vehicleId;
		private String stopId;
		private int stopSequence;
		private String routeId;
		private String routeShortName;
		private String tripId;
		private String blockId;
		private long predictionTime;
		private long avlTime;
		private long creationTime;
		private boolean affectedByWaitStop;
		private String driverId;
		private short passengerCount;
		private float passengerFullness;
		private boolean isArrival;
		
		private static final long serialVersionUID = -8585283691951746718L;
		private static final short serializationVersion = 0;

		/*
		 * Only to be used within this class.
		 */
		private SerializationProxy(Prediction p) {
			this.vehicleId = p.vehicleId;
			this.stopId = p.stopId;
			this.stopSequence = p.stopSequence;
			this.routeId = p.routeId;
			this.routeShortName =p.routeShortName;
			this.tripId = p.tripId;
			this.blockId = p.blockId;
			this.predictionTime = p.predictionTime;
			this.avlTime = p.avlTime;
			this.creationTime = p.creationTime;
			this.affectedByWaitStop = p.affectedByWaitStop;
			this.driverId = p.driverId;
			this.passengerCount = p.passengerCount;
			this.passengerFullness = p.passengerFullness;
			this.isArrival = p.isArrival;
		}
		
		/**
		 * When object is serialized writeReplace() causes this
		 * SerializationProxy object to be written. Write it in a
		 * custom way that includes a version ID so that clients
		 * and servers can have two different versions of code.
		 */
		private void writeObject(java.io.ObjectOutputStream stream)
				throws IOException {
			stream.writeShort(serializationVersion);
			stream.writeObject(vehicleId);
			stream.writeObject(stopId);
			stream.writeInt(stopSequence);
			stream.writeObject(routeId);
			stream.writeObject(routeShortName);
			stream.writeObject(tripId);
			stream.writeObject(blockId);
			stream.writeLong(predictionTime);
			stream.writeLong(avlTime);
			stream.writeLong(creationTime);
			stream.writeBoolean(affectedByWaitStop);
			stream.writeObject(driverId);
			stream.writeShort(passengerCount);
			stream.writeFloat(passengerFullness);
			stream.writeBoolean(isArrival);
		}

		/**
		 * Custom method of deserializing a SerializationProy object.
		 */
		private void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			short readVersion = stream.readShort();
			if (serializationVersion != readVersion) {
				throw new IOException("Serialization error when reading "
						+ getClass().getSimpleName()
						+ " object. Read serializationVersion=" + readVersion);
			}

			// serialization version is OK so read in object
			vehicleId = (String) stream.readObject();
			stopId = (String) stream.readObject();
			stopSequence = stream.readInt();
			routeId = (String) stream.readObject();
			routeShortName = (String) stream.readObject();
			tripId = (String) stream.readObject();
			blockId = (String) stream.readObject();
			predictionTime = stream.readLong();
			avlTime = stream.readLong();
			creationTime = stream.readLong();
			affectedByWaitStop = stream.readBoolean();
			driverId = (String) stream.readObject();
			passengerCount = stream.readShort();
			passengerFullness = stream.readFloat();
			isArrival = stream.readBoolean();
		}
		
		/**
		 * When an object is read in it will be a SerializatProxy object
		 * due to writeReplace() being used by the enclosing class. When
		 * such an object is deserialized this method will be called and
		 * the SerializationProxy object is converted to an enclosing
		 * class object.
		 */
		private Object readResolve() {
			return new Prediction(vehicleId, stopId, stopSequence, routeId,
					routeShortName, tripId, blockId, predictionTime, avlTime,
					creationTime, affectedByWaitStop, driverId, passengerCount, 
					passengerFullness, isArrival);
		}
	}

	/*
	 * Needed as part of using a SerializationProxy. When Vehicle object
	 * is serialized the SerializationProxy will instead be used.
	 */
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	/*
	 * Needed as part of using a SerializationProxy. Makes sure that Vehicle
	 * object cannot be deserialized without using proxy, thereby eliminating
	 * possibility of such an attack as described in "Effective Java".
	 */
	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Must use proxy instead");
	}
	    	
	@Override
	public String toString() {
		return "Prediction [" 
				+ "vehicleId=" + vehicleId
				+ ", routeId=" + routeId
				+ ", routeShortName=" + routeShortName
				+ ", stopId=" + stopId
				+ (stopName!=null ? ", stopName=" + stopName : "") 
				+ ", stopSequence=" + stopSequence
				+ ", tripId=" + tripId 
				+ ", blockId=" + blockId 
				+ ", predictionTime=" + Time.timeStrMsec(predictionTime) 
				+ ", avlTime=" + Time.timeStrMsec(avlTime)
				+ ", creationTime=" + Time.timeStrMsec(creationTime)
				+ ", affectedByWaitStop=" + affectedByWaitStop
				+ (driverId!=null ? ", driverId=" + driverId : "")
				+ (isPassengerCountValid() ? ", passengerCount=" + passengerCount : "")
				+ (!Float.isNaN(passengerFullness) ? 
						", passengerFullness=" + StringUtils.twoDigitFormat(passengerFullness) : "")
				+ ", isArrival=" + isArrival
				+ "]";
	}


	public String getVehicleId() {
		return vehicleId;
	}

	public String getRouteId() {
		return routeId;
	}
	
	public String getRouteShortName() {
		return routeShortName;
	}
	
	public String getStopId() {
		return stopId;
	}

	public int getStopSequence() {
		return stopSequence;
	}
	
	public String getTripId() {
		return tripId;
	}
	
	public String getBlockId() {
		return blockId;
	}
	
	public long getTime() {
		return predictionTime;
	}

	public boolean isAffectedByWaitStop() {
		return affectedByWaitStop;
	}
	
	public long getAvlTime() {
		return avlTime;
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	
	/**
	 * Returns the driver ID if it is available. Otherwise returns null.
	 * 
	 * @return
	 */
	public String getDriverId() {
		return driverId;
	}
	
	/**
	 * Returns the passenger count as obtained from the AVL feed. The value
	 * will not be valid for systems that do not have passenger counting
	 * sensors. Therefore should use isPassengerCountValid() to determine
	 * if the value is valid.
	 * 
	 * @return Passenger count from the AVL feed
	 */
	public short getPassengerCount() {
		return passengerCount;
	}
	
	/**
	 * Passenger counts only valid for systems where there actually are
	 * passenger counting sensors.
	 * 
	 * @return True if getPassengerCount() returns a valid value
	 */
	public boolean isPassengerCountValid() {
		return passengerCount  >= 0;
	}
	
	public boolean isArrival() {
		return isArrival;
	}
}
