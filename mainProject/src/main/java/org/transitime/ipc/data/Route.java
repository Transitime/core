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

import org.transitime.db.structs.Extent;

/**
 * Contains configuration information for a single route. For providing info to
 * client. This class is Immutable so that it is threadsafe.
 * 
 * @author SkiBu Smith
 * 
 */
public class Route implements Serializable {

	private final String id;
	private final String shortName;
	private final String name;
	private final Extent extent;
	
	private static final long serialVersionUID = -3670639103802632389L;

	/********************** Member Functions **************************/

	/**
	 * @param id
	 * @param shortName
	 * @param name
	 */
	private Route(String id, String shortName, String longName, Extent extent) {
		this.id = id;
		this.shortName = shortName;
		this.name = longName;
		this.extent = extent;
	}

	public Route(org.transitime.db.structs.Route dbRoute) {
		this.id = dbRoute.getId();
		this.shortName = dbRoute.getShortName();
		this.name = dbRoute.getName();
		this.extent = dbRoute.getExtent();
	}
	/*
	 * SerializationProxy is used so that this class can be immutable
	 * and so that can do versioning of objects.
	 */
	private static class SerializationProxy implements Serializable {
		// Exact copy of fields of Route enclosing class object
		private String id;
		private String shortName;
		private String name;
		private Extent extent;
		
		private static final long serialVersionUID = 5940164509337028725L;
		private static final short serializationVersion = 0;

		/*
		 * Only to be used within this class.
		 */
		private SerializationProxy(Route r) {
			this.id = r.id;
			this.shortName = r.shortName;
			this.name = r.name;
			this.extent = r.extent;
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
			stream.writeObject(id);
			stream.writeObject(shortName);
			stream.writeObject(name);
			stream.writeObject(extent);
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
			id = (String) stream.readObject();
			shortName = (String) stream.readObject();
			name = (String) stream.readObject();
			extent = (Extent) stream.readObject();
		}
		
		/*
		 * When an object is read in it will be a SerializatProxy object
		 * due to writeReplace() being used by the enclosing class. When
		 * such an object is deserialized this method will be called and
		 * the SerializationProxy object is converted to an enclosing
		 * class object.
		 */
		private Object readResolve() {
			return new Route(id, shortName, name, extent);
		}
	} // End of SerializationProxy class

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
		return id;
	}

	public String getShortName() {
		return shortName;
	}

	public String getName() {
		return name;
	}

	public Extent getExtent() {
		return extent;
	}
	
	@Override
	public String toString() {
		return "Route [" 
				+ "id=" + id 
				+ ", shortName=" + shortName 
				+ ", name="	+ name
				+ ", extent=" + extent
				+ "]";
	}

}
