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

import org.transitime.db.structs.AvlReport;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * A serializable object used by RMI to transfer AVL data to client.
 *
 * @author SkiBu Smith
 *
 */
public class Avl implements Serializable {

	private final String vehicleId;
	private final long time;
	private final float latitude;
	private final float longitude;
	private final float speed;
	private final float heading;
	private final String assignmentId;
	private final String driverId;
	private final String licensePlate;
	private final int passengerCount;

	private static final long serialVersionUID = 2506303490106709586L;

	/********************** Member Functions **************************/

	/**
	 * @param vehicleId
	 * @param time
	 * @param latitude
	 * @param longitude
	 * @param speed
	 * @param heading
	 * @param assignmentId
	 * @param driverId
	 * @param licensePlate
	 * @param passengerCount
	 */
	public Avl(String vehicleId, long time, float latitude, float longitude,
			float speed, float heading, String assignmentId, String driverId,
			String licensePlate, int passengerCount) {
		this.vehicleId = vehicleId;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		this.heading = heading;
		this.assignmentId = assignmentId;
		this.driverId = driverId;
		this.licensePlate = licensePlate;
		this.passengerCount = passengerCount;
	}
	
	/**
	 * @param lastAvlReport
	 */
	public Avl(AvlReport a) {
		this.vehicleId = a.getVehicleId();
		this.time = a.getTime();
		this.latitude = (float) a.getLat();
		this.longitude = (float) a.getLon();
		this.speed = a.getSpeed();
		this.heading = a.getHeading();
		this.assignmentId = a.getAssignmentId();
		this.driverId = a.getDriverId();
		this.licensePlate = a.getLicensePlate();
		this.passengerCount = a.getPassengerCount();

	}

	/*
	 * SerializationProxy is used so that this class can be immutable
	 * and so that can do versioning of objects.
	 */
	private static class SerializationProxy implements Serializable {
		// Exact copy of fields of Vehicle object
		private String vehicleId;
		private long time;
		private float latitude;
		private float longitude;
		private float speed;
		private float heading;
		private String assignmentId;
		private String driverId;
		private String licensePlate;
		private int passengerCount;
		
		private static final long serialVersionUID = 6220698347690060245L;
		private static final short serializationVersion = 0;

		/*
		 * Only to be used within this class.
		 */
		private SerializationProxy(Avl avl) {
			this.vehicleId = avl.vehicleId;
			this.time = avl.time;
			this.latitude = avl.latitude;
			this.longitude = avl.longitude;
			this.speed = avl.speed;
			this.heading = avl.heading;
			this.assignmentId = avl.assignmentId;
			this.driverId = avl.driverId;
			this.licensePlate = avl.licensePlate;
			this.passengerCount = avl.passengerCount;
		}
		
		/*
		 * When object is serialized writeReplace() causes this
		 * SerializationProxy object to be written. Write it in a
		 * custom way that includes a version ID so that clients
		 * and servers can have two different versions of code.
		 */
		private void writeObject(java.io.ObjectOutputStream stream)
				throws IOException {
			stream.writeShort(serializationVersion);
			stream.writeObject(vehicleId);
			stream.writeLong(time);
			stream.writeFloat(latitude);
			stream.writeFloat(longitude);
			stream.writeFloat(speed);
			stream.writeFloat(heading);
			stream.writeObject(assignmentId);
			stream.writeObject(driverId);
			stream.writeObject(licensePlate);
			stream.writeInt(passengerCount);
		}

		/*
		 * When an object is read in it will be a SerializatProxy object
		 * due to writeReplace() being used by the enclosing class. When
		 * such an object is deserialized this method will be called and
		 * the SerializationProxy object is converted to an enclosing
		 * class object.
		 */
		private Object readResolve() {
			return new Avl(vehicleId, time, latitude, longitude, speed,
					heading, assignmentId, driverId, licensePlate,
					passengerCount);
		}

		/*
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
			time = stream.readLong();
			latitude = stream.readFloat();
			longitude = stream.readFloat();
			speed = stream.readFloat();
			heading = stream.readFloat();
			assignmentId = (String) stream.readObject();
			driverId = (String) stream.readObject();
			licensePlate = (String) stream.readObject();
			passengerCount = stream.readInt();
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

	public String getVehicleId() {
		return vehicleId;
	}

	public long getTime() {
		return time;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	/**
	 * @return Speed of vehicle, or null if speed not defined.
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @return Heading of vehicle, or null if speed not defined.
	 */
	public float getHeading() {
		return heading;
	}

	public String getDriverId() {
		return driverId;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public int getPassengerCount() {
		return passengerCount;
	}

	@Override
	public String toString() {
		return "Avl [vehicleId=" + vehicleId 
				+ ", time=" + Time.dateTimeStr(time) 
				+ ", latitude=" + latitude 
				+ ", longitude=" + longitude 
				+ ", speed=" + Geo.speedFormat(speed)
				+ ", heading=" + Geo.headingFormat(heading) 
				+ ", assignmentId=" + assignmentId
				+ ", driverId=" + driverId 
				+ ", licensePlate=" + licensePlate
				+ ", passengerCount=" + passengerCount 
				+ "]";
	}
	    

}
