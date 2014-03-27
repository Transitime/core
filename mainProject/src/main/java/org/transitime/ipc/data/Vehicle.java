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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Contains information on a single vehicle. For providing info to client.
 * This class is Immutable so that it is threadsafe.
 *
 * @author SkiBu Smith
 *
 */
@Immutable
public class Vehicle implements Serializable {

	private final Avl avl;
	private final String routeId;
	
	// routeShortName needed because routeId is sometimes not consistent over
	// schedule changes but routeShortName usually is.
	private final String routeShortName;
	private final String tripId;
	private final boolean predictable;
	
	private static final long serialVersionUID = -1744566765456572042L;

	/********************** Member Functions **************************/

	/**
	 * Constructors a Vehicle object.
	 * 
	 * @param avl
	 * @param routeId
	 * @param routeShortName
	 * @param predictable
	 */
	public Vehicle(Avl avl, String routeId, String routeShortName,
			String tripId, boolean predictable) {
		this.avl = avl;
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.tripId = tripId;
		this.predictable = predictable;
	}

	/*
	 * SerializationProxy is used so that this class can be immutable
	 * and so that can do versioning of objects.
	 */
	private static class SerializationProxy implements Serializable {
		// Exact copy of fields of Vehicle enclosing class object
		private Avl avl;
		private String routeId;
		private String routeShortName;
		private String tripId;
		private boolean predictable;
		
		private static final long serialVersionUID = -4996254752417270043L;
		private static final short serializationVersion = 0;

		/*
		 * Only to be used within this class.
		 */
		private SerializationProxy(Vehicle v) {
			this.avl = v.avl;
			this.routeId = v.routeId;
			this.routeShortName = v.routeShortName;
			this.tripId = v.tripId;
			this.predictable = v.predictable;
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
			stream.writeObject(avl);
			stream.writeObject(routeId);
			stream.writeObject(routeShortName);
			stream.writeObject(tripId);
			stream.writeBoolean(predictable);
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
			avl = (Avl) stream.readObject();
			routeId = (String) stream.readObject();
			routeShortName = (String) stream.readObject();
			tripId = (String) stream.readObject();
			predictable = stream.readBoolean();
		}
		
		/*
		 * When an object is read in it will be a SerializatProxy object
		 * due to writeReplace() being used by the enclosing class. When
		 * such an object is deserialized this method will be called and
		 * the SerializationProxy object is converted to an enclosing
		 * class object.
		 */
		private Object readResolve() {
			return new Vehicle(avl, routeId, routeShortName, tripId,
					predictable);
		}
	}  // End of SerializationProxy class

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
	    
	public String getId() {
		return avl.getVehicleId();
	}

	public Avl getAvl() {
		return avl;
	}

	/**
	 * @return Heading of vehicle, or null if speed not defined.
	 */
	public float getHeading() {
		return avl.getHeading();
	}
	
	/**
	 * @return Speed of vehicle, or null if speed not defined.
	 */
	public float getSpeed() {
		return avl.getSpeed();
	}
	
	public float getLatitude() {
		return avl.getLatitude();
	}
	
	public float getLongitude() {
		return avl.getLongitude();
	}
	
	public String getLicensePlate() {
		return avl.getLicensePlate();
	}
	
	public long getGpsTime() {
		return avl.getTime();
	}
	
	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public String getTripId() {
		return tripId;
	}
	
	public boolean isPredictable() {
		return predictable;
	}

	@Override
	public String toString() {
		return "Vehicle [" 
				+ "avl=" + avl
				+ ", routeId=" + routeId 
				+ ", routeShortName=" + routeShortName
				+ ", tripId=" + tripId
				+ ", predictable=" + predictable 
				+ "]";
	}

	/*
	 * Just for testing.
	 */
	public static void main(String args[]) {
		Avl avl = new Avl("avlVehicleId", 10, 1.23f, 4.56f, 0.0f, 0.0f, 
				"block", "driver", "license", 0);
		Vehicle v = new Vehicle(avl, "routeId", "routeShortName", "tripId", true);
		try {
			FileOutputStream fileOut = new FileOutputStream("foo.ser");
			ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
			outStream.writeObject(v);
			outStream.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		} 

		try {
			FileInputStream fileIn = new FileInputStream("foo.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			@SuppressWarnings("unused")
			Vehicle newVehicle = (Vehicle) in.readObject();
			in.close();
			fileIn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
}
