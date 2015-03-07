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

import java.io.Serializable;

import org.transitime.db.structs.Extent;
import org.transitime.db.structs.Route;

/**
 * Contains configuration information for a single route. For providing info to
 * client. This class is Immutable so that it is threadsafe.
 * 
 * @author SkiBu Smith
 * 
 */
public class IpcRouteSummary implements Serializable {

	protected final String id;
	protected final String shortName;
	protected final String name;
	protected final Extent extent;
	protected final String type;
	protected final String color;
	protected final String textColor;
	
	private static final long serialVersionUID = -3670639103802632389L;

	/********************** Member Functions **************************/

//	/**
//	 * Constructor used for when deserializing a proxy object. Declared private
//	 * because only used internally by the proxy class.
//	 * 
//	 * @param id
//	 * @param shortName
//	 * @param longName
//	 * @param extent
//	 * @param type
//	 * @param color
//	 * @param textColor
//	 */
//    private RouteSummary(String id, String shortName, String longName, Extent extent,
//	    String type, String color, String textColor) {
//		this.id = id;
//		this.shortName = shortName;
//		this.name = longName;
//		this.extent = extent;
//		this.type = type;
//		this.color = color;
//		this.textColor = textColor;
//	}

    /**
     * Constructs a new RouteSummary object using a Route object from
     * the database. Used by the server to create an object to be
     * transmitted via RMI.
     * 
     * @param dbRoute
     */
	public IpcRouteSummary(Route dbRoute) {
		this.id = dbRoute.getId();
		this.shortName = dbRoute.getShortName();
		this.name = dbRoute.getName();
		this.extent = dbRoute.getExtent();
		this.type = dbRoute.getType();
		this.color = dbRoute.getColor();
		this.textColor = dbRoute.getTextColor();
	}
	
//	/*
//	 * SerializationProxy is used so that this class can be immutable
//	 * and so that can do versioning of objects.
//	 */
//	private static class SerializationProxy implements Serializable {
//		// Exact copy of fields of Route enclosing class object
//		private String id;
//		private String shortName;
//		private String name;
//		private Extent extent;
//		private String type;
//		private String color;
//		private String textColor;
//		
//		private static final long serialVersionUID = 5940164509337028725L;
//		private static final short serializationVersion = 0;
//
//		/*
//		 * Only to be used within this class.
//		 */
//		private SerializationProxy(RouteSummary r) {
//			this.id = r.id;
//			this.shortName = r.shortName;
//			this.name = r.name;
//			this.extent = r.extent;
//			this.type = r.type;
//			this.color = r.color;
//			this.textColor = r.textColor;
//		}
//		
//		/*
//		 * When object is serialized writeReplace() causes this
//		 * SerializationProxy object to be written. Write it in a
//		 * custom way that includes a version ID so that clients
//		 * and servers can have two different versions of code.
//		 */
//		private void writeObject(java.io.ObjectOutputStream stream)
//				throws IOException {
//			stream.writeShort(serializationVersion);
//			
//			stream.writeObject(id);
//			stream.writeObject(shortName);
//			stream.writeObject(name);
//			stream.writeObject(extent);
//			stream.writeObject(type);
//			stream.writeObject(color);
//			stream.writeObject(textColor);
//		}
//
//		/*
//		 * Custom method of deserializing a SerializationProy object.
//		 */
//		private void readObject(java.io.ObjectInputStream stream)
//				throws IOException, ClassNotFoundException {
//			short readVersion = stream.readShort();
//			if (serializationVersion != readVersion) {
//				throw new IOException("Serialization error when reading "
//						+ getClass().getSimpleName()
//						+ " object. Read serializationVersion=" + readVersion);
//			}
//
//			// serialization version is OK so read in object
//			id = (String) stream.readObject();
//			shortName = (String) stream.readObject();
//			name = (String) stream.readObject();
//			extent = (Extent) stream.readObject();
//			type = (String) stream.readObject();
//			color = (String) stream.readObject();
//			textColor = (String) stream.readObject();
//		}
//		
//		/*
//		 * When an object is read in it will be a SerializatProxy object
//		 * due to writeReplace() being used by the enclosing class. When
//		 * such an object is deserialized this method will be called and
//		 * the SerializationProxy object is converted to an enclosing
//		 * class object.
//		 */
//		private Object readResolve() {
//			return new RouteSummary(id, shortName, name, extent, type, color, textColor);
//		}
//	} // End of SerializationProxy class
//
//	/*
//	 * Needed as part of using a SerializationProxy. When Vehicle object
//	 * is serialized the SerializationProxy will instead be used.
//	 */
//	private Object writeReplace() {
//		return new SerializationProxy(this);
//	}
//
//	/*
//	 * Needed as part of using a SerializationProxy. Makes sure that Vehicle
//	 * object cannot be deserialized without using proxy, thereby eliminating
//	 * possibility of such an attack as described in "Effective Java".
//	 */
//	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
//		throw new InvalidObjectException("Must use proxy instead");
//	}

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
	
	public String getType() {
	    return type;
	}

	public String getColor() {
	    return color;
	}

	public String getTextColor() {
	    return textColor;
	}

	@Override
	public String toString() {
		return "IpcRouteSummary [" 
				+ "id=" + id 
				+ ", shortName=" + shortName 
				+ ", name="	+ name
				+ ", extent=" + extent
				+ ", type=" + type
				+ ", color=" + color
				+ ", textColor=" + textColor
				+ "]";
	}

}
