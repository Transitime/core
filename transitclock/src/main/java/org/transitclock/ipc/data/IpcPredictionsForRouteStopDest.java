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

package org.transitclock.ipc.data;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.transitclock.applications.Core;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.Stop;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.TripPattern;
import org.transitclock.utils.Geo;

import org.transitclock.utils.TrimmableArrayList;

import static org.transitclock.core.PredictionGeneratorDefaultImpl.isHistoricalPredictionForFutureStop;

/**
 * Contains list of predictions for a route/stop/destination. 
 *
 * @author SkiBu Smith
 *
 */
public class IpcPredictionsForRouteStopDest implements Serializable {

	private final String routeId;
	// routeShortName needed because routeId is sometimes not consistent over
	// schedule changes but routeShortName usually is.
	private final String routeShortName;
	private final String routeName;
	private final int routeOrder;
	
	private final String stopId;
	private final String stopName;
	private final Integer stopCode;
	
	private final String headsign;
	private final String directionId;
	
	// For when providing predictions based on location
	private final double distanceToStop;
	
	// The predictions associated with the route/stop/dest
	private final List<IpcPrediction> predictionsForRouteStopDest;

	private final static int INITIAL_CAPACITY = 5;
	
	private static final long serialVersionUID = 5875028328864504842L;

	/********************** Member Functions **************************/

	/**
	 * Constructor for creating a IpcPredictionsForRouteStopDest on the server
	 * side.
	 * 
	 * @param trip
	 * @param stopId
	 * @param distanceToStop
	 *            How far stop is away from user. If predictions are not
	 *            location based then use value of Double.NaN.
	 */
	public IpcPredictionsForRouteStopDest(Trip trip, String stopId, 
			double distanceToStop) {
		this.routeId = 
				trip != null ? trip.getRouteId() : null;
		this.routeShortName = 
				trip != null ? trip.getRouteShortName() : null;
		this.routeName = 
				trip != null ? trip.getRouteName() : null;
		this.routeOrder = 
				trip != null ? trip.getRoute().getRouteOrder() : -1;
		this.stopId = stopId;
		
		Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
		this.stopName = stop != null ? stop.getName() : null;
		this.stopCode = stop != null ? stop.getCode() : null;

		this.headsign = 
				trip != null ? trip.getHeadsign() : null;
		this.directionId = 
				trip != null ? trip.getDirectionId() : null;
		this.distanceToStop = distanceToStop;
		this.predictionsForRouteStopDest = 
				new TrimmableArrayList<IpcPrediction>(INITIAL_CAPACITY);
	}
	
	/**
	 * For constructing an IpcPredictionsForRouteStopDest for when there aren't 
	 * any predictions. 
	 * 
	 * @param tripPattern Must not be null
	 * @param stopId
	 * @param distanceToStop
	 */
	public IpcPredictionsForRouteStopDest(TripPattern tripPattern,  String stopId,
			double distanceToStop) {
		this.routeId = tripPattern.getRouteId();
		this.routeShortName = tripPattern.getRouteShortName();		
		Route route = Core.getInstance().getDbConfig()
				.getRouteById(tripPattern.getRouteId());
		if (route == null) {
			throw new IllegalArgumentException("RouteId="
					+ tripPattern.getRouteId() + " does not exist.");
		}
		this.routeName = route.getName();
		this.routeOrder = route.getRouteOrder();
		this.stopId = stopId;
		Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
		this.stopName = stop != null ? stop.getName() : null;
		this.stopCode = stop != null ? stop.getCode() : null;

		this.headsign = tripPattern.getHeadsign();
		this.directionId = tripPattern.getDirectionId();
		this.distanceToStop = distanceToStop;
		this.predictionsForRouteStopDest = new TrimmableArrayList<IpcPrediction>(0);

	}
	
	/**
	 * Constructor for cloning a PredictionsForRouteStop object.
	 * 
	 * @param toClone
	 * @param maxPredictionsPerStop
	 * @param maxSystemTimeForPrediction
	 *            Max point in future want predictions for. This way can limit
	 *            predictions when requesting a large number of them.
	 * @param terminatePredictionsAtEndOfTrip
	 * 			  if set continue after maxSystemTimeForPrediction to serve
	 * 			  predictions for the entirety of the trip
	 * @param distanceToStop
	 *            For when getting predictions by location
	 */
	private IpcPredictionsForRouteStopDest(
			IpcPredictionsForRouteStopDest toClone,
			int maxPredictionsPerStop, long maxSystemTimeForPrediction,
			boolean terminatePredictionsAtEndOfTrip,
			double distanceToStop) {
		this.routeId = toClone.routeId;
		this.routeShortName = toClone.routeShortName;
		this.routeName = toClone.routeName;
		this.routeOrder = toClone.routeOrder;
		this.stopId = toClone.stopId;
		this.stopName = toClone.stopName;
		this.stopCode = toClone.stopCode;
		this.headsign = toClone.headsign;
		this.directionId = toClone.directionId;
		this.distanceToStop = distanceToStop;
		
		// Copy all the prediction info. Do while synchronized since another
		// thread could otherwise be changing the data.
		synchronized (toClone) {	
			int size = Math.min(toClone.predictionsForRouteStopDest.size(),
					maxPredictionsPerStop);
			this.predictionsForRouteStopDest = new ArrayList<IpcPrediction>(size);			
			for (int i=0; i<size; ++i) {
				IpcPrediction prediction = toClone.predictionsForRouteStopDest.get(i);
				/* If prediction exceeds max time then done
				 * EXCEPT if terminatePredictionsAtEndOfTrip, in which case we assume
				 * we have exactly the amount of predictions expected (we don't filter)
				 */
				if (!terminatePredictionsAtEndOfTrip && prediction.getPredictionTime() > maxSystemTimeForPrediction)
					break;
				this.predictionsForRouteStopDest.add(i, prediction);
			}
		}
	}
	
	/**
	 * Constructor used for when deserializing a proxy object. Declared private
	 * because only used internally by the proxy class.
	 * 
	 * @param routeId
	 * @param routeShortName
	 * @param routeName
	 * @param routeOrder
	 * @param stopId
	 * @param stopName
	 * @param stopCode
	 * @param destination
	 * @param directionId
	 * @param distanceToStop
	 * @param predictions
	 */
	private IpcPredictionsForRouteStopDest(String routeId,
			String routeShortName, String routeName, int routeOrder,
			String stopId, String stopName, Integer stopCode,
			String destination, String directionId, double distanceToStop,
			List<IpcPrediction> predictions) {
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.routeName = routeName;
		this.routeOrder = routeOrder;
		this.stopId = stopId;
		this.stopName = stopName;
		this.stopCode = stopCode;
		this.headsign = destination;
		this.directionId = directionId;
		this.distanceToStop = distanceToStop;
		this.predictionsForRouteStopDest = predictions;
	}
	
	/**
	 * For constructing IpcPredictionsForRouteStopDest object when there are no
	 * current predictions.
	 * 
	 * @param routeShortName
	 * @param directionId
	 * @param stopId
	 * @param distanceToStop
	 */
	public IpcPredictionsForRouteStopDest(String routeShortName,
			String directionId, String stopId, double distanceToStop) {
		Route route = Core.getInstance().getDbConfig().getRouteByShortName(routeShortName);
		if (route == null) {
			throw new IllegalArgumentException("routeShortName="
					+ routeShortName + " does not exist.");
		}
		Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
		if (stop == null) {
			throw new IllegalArgumentException("stopId="
					+ stopId + " does not exist.");
		}
		
		this.routeId = route.getId();
		this.routeShortName = routeShortName;
		this.routeName = route.getName();
		this.routeOrder = route.getRouteOrder();
		this.stopId = stopId;
		this.stopName = stop.getName();
		this.stopCode = stop.getCode();
		this.headsign = null;
		this.directionId = directionId;
		this.distanceToStop = distanceToStop;
		this.predictionsForRouteStopDest = new TrimmableArrayList<IpcPrediction>(0);
	}
	
	/**
	 * SerializationProxy is used so that this class can be immutable and so
	 * that can do versioning of objects.
	 */
	private static class SerializationProxy implements Serializable {
		private String routeId;
		private String routeShortName;
		private String routeName;
		private int routeOrder;
		private String stopId;
		private String stopName;
		private Integer stopCode;
		private String headsign;
		private String directionId;
		private double distanceToStop;
		private List<IpcPrediction> predictionsForRouteStop;

		private static final short currentSerializationVersion = 1;
		private static final long serialVersionUID = -2312925771271829358L;

		/*
		 * Only to be used within this class.
		 */
		private SerializationProxy(IpcPredictionsForRouteStopDest p) {
			this.routeId = p.routeId;
			this.routeShortName = p.routeShortName;
			this.routeName = p.routeName;
			this.routeOrder = p.routeOrder;
			this.stopId = p.stopId;
			this.stopName = p.stopName;
			this.stopCode = p.stopCode;
			this.headsign = p.headsign;
			this.directionId = p.directionId;
			this.distanceToStop = p.distanceToStop;
			this.predictionsForRouteStop = p.predictionsForRouteStopDest;
		}

		/*
		 * When object is serialized writeReplace() causes this
		 * SerializationProxy object to be written. Write it in a custom way
		 * that includes a version ID so that clients and servers can have two
		 * different versions of code.
		 */
		private void writeObject(java.io.ObjectOutputStream stream)
				throws IOException {
			stream.writeShort(currentSerializationVersion);
			
			stream.writeObject(routeId);
			stream.writeObject(routeShortName);
			stream.writeObject(routeName);
			stream.writeInt(routeOrder);
			stream.writeObject(stopId);
			stream.writeObject(stopName);
			stream.writeObject(headsign);
			stream.writeObject(directionId);
			stream.writeDouble(distanceToStop);
			stream.writeObject(predictionsForRouteStop);
			// Can be null so use writeObject()
			stream.writeObject(stopCode);
		}
		
		/*
		 * Custom method of deserializing a SerializationProy object.
		 */
		@SuppressWarnings("unchecked")
		private void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			short readVersion = stream.readShort();
			if (currentSerializationVersion < readVersion) {
				throw new IOException("Serialization error when reading "
						+ getClass().getSimpleName()
						+ " object. Read version=" + readVersion 
						+ " but currently using software version=" 
						+ currentSerializationVersion);
			}

			// serialization version is OK so read in object
			routeId = (String) stream.readObject();
			routeShortName = (String) stream.readObject();
			routeName = (String) stream.readObject();
			routeOrder = stream.readInt();
			stopId = (String) stream.readObject();
			stopName = (String) stream.readObject();
			headsign = (String) stream.readObject();
			directionId = (String) stream.readObject();
			distanceToStop = stream.readDouble();
			predictionsForRouteStop = (List<IpcPrediction>) stream.readObject();
			
			if (readVersion >=1) {
				// Can be null so use readObject()
				stopCode = (Integer) stream.readObject();
			}
		}

		/*
		 * When an object is read in it will be a SerializatProxy object due to
		 * writeReplace() being used by the enclosing class. When such an object
		 * is deserialized this method will be called and the SerializationProxy
		 * object is converted to an enclosing class object.
		 */
		private Object readResolve() {
			return new IpcPredictionsForRouteStopDest(routeId, routeShortName,
					routeName, routeOrder, stopId, stopName, stopCode,
					headsign, directionId, distanceToStop,
					predictionsForRouteStop);
		}
	} /* End of SerializationProxy inner class */
	
	/*
	 * Needed as part of using a SerializationProxy. When Vehicle object is
	 * serialized the SerializationProxy will instead be used.
	 */
	private Object writeReplace() {
		return new SerializationProxy(this);
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

	/**
	 * Gets a copy of this object. This is done with the object being
	 * copied synchronized so that the predictions remain coherent. Limits
	 * number of predictions to maxPredictionsPerStop.
	 *
	 * @param maxPredictionsPerStop
	 * @param distanceToStop
	 *            For when getting predictions by location
	 * @return
	 */
	public IpcPredictionsForRouteStopDest getClone(int maxPredictionsPerStop,
			double distanceToStop) {
		// Get copy of predictions. Don't limit by how far predictions
		// are into the future. Therefore maxPredictionTime set to
		// Integer.MAX_VALUE and currentTime set to 0L because it 
		// doesn't matter.
		IpcPredictionsForRouteStopDest clone = new IpcPredictionsForRouteStopDest(this,
				maxPredictionsPerStop, Long.MAX_VALUE, false, distanceToStop);
		return clone;
	}
	
	/**
	 * Gets a copy of this object. This is done with the object being copied
	 * synchronized so that the predictions remain coherent. Limits number of
	 * predictions to maxPredictionsPerStop.
	 * 
	 * @param maxPredictionsPerStop
	 *            Won't copy more then this number of predictions
	 * @param maxSystemTimeForPrediction
	 *            Max point in future want predictions for. This way can limit
	 *            predictions when requesting a large number of them.
	 * @param terminatePredictionsAtEndOfTrip
	 * 			  if set continue after maxSystemTimeForPrediction to complete
	 * 			  predictions for the trip.
	 * @param distanceToStop
	 *            For when getting predictions by location
	 * @return
	 */
	public IpcPredictionsForRouteStopDest getClone(int maxPredictionsPerStop,
			long maxSystemTimeForPrediction, boolean terminatePredictionsAtEndOfTrip, double distanceToStop) {
		IpcPredictionsForRouteStopDest clone = new IpcPredictionsForRouteStopDest(
				this, maxPredictionsPerStop, maxSystemTimeForPrediction, terminatePredictionsAtEndOfTrip, distanceToStop);
		return clone;
	}
	
	/**
	 * Gets a copy of this object. This is done with the object being copied
	 * synchronized so that the predictions remain coherent. Limits number of
	 * predictions to maxPredictionsPerStop.
	 * 
	 * @param maxPredictionsPerStop
	 *            Won't copy more then this number of predictions
	 * @param maxSystemTimeForPrediction
	 *            Max point in future want predictions for. This way can limit
	 *            predictions when requesting a large number of them.
	 * @return
	 */
	public IpcPredictionsForRouteStopDest getClone(int maxPredictionsPerStop,
			long maxSystemTimeForPrediction, boolean terminatePredictionsAtEndOfTrip) {
		IpcPredictionsForRouteStopDest clone = getClone(maxPredictionsPerStop,
				maxSystemTimeForPrediction, terminatePredictionsAtEndOfTrip, Double.NaN);
		return clone;
	}
	
	/**
	 * Removes a prediction.
	 * <p>
	 * Not sure if really need to synchronize removal of predictions
	 * from list since it is only a single operation. But synching
	 * it is certainly the safe thing to do.
	 * 
	 * @param oldPrediction
	 */
	public synchronized void removePrediction(IpcPrediction oldPrediction) {
		predictionsForRouteStopDest.remove(oldPrediction);
	}

	/**
	 * Removes predictions that are older than the current time. Useful for when
	 * getting predictions. Synchronized because don't want predictions list to
	 * be changing by another thread when removing predictions.
	 * 
	 * @param currentTime
	 *            Should use PredictionDataCache.systemTime.get() so that works
	 *            even when in playback mode.
	 */
	public synchronized void removeExpiredPredictions(long currentTime) {
		Iterator<IpcPrediction> iterator = predictionsForRouteStopDest.iterator();
		while (iterator.hasNext()) {
			IpcPrediction currentPrediction = iterator.next();


			// Remove predictions that are expired. It makes sense to do this
			// here when adding predictions since only need to take out 
			// predictions if more are being added.
			if (currentPrediction.getPredictionTime() < currentTime) {
				// per the spec, we need to serve predictions until the scheduled time has past
				if (!isHistoricalPredictionForFutureStop(currentPrediction, currentTime)) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Updates the predictions for this object with the new predictions for a
	 * vehicle.
	 * <p>
	 * Synchronized because there are multiple steps in removing old predictions
	 * and creating new ones.
	 * 
	 * @param newPredsForRouteStopDest
	 *            The new predictions for the vehicle
	 * @param currentTime
	 *            So can get rid of predictions that have expired.
	 */
	public synchronized void updatePredictionsForVehicle(
			List<IpcPrediction> newPredsForRouteStopDest,
			long currentTime) {
		// If no predictions then nothing to do so return.
		if (newPredsForRouteStopDest == null
				|| newPredsForRouteStopDest.isEmpty())
			return;
	
		// Determine which vehicle we are updating predictions for
		String vehicleId = newPredsForRouteStopDest.get(0).getVehicleId();
		
		// Go through current predictions and get rid of existing ones for
		// this vehicle or ones that have expired
		Iterator<IpcPrediction> iterator = predictionsForRouteStopDest.iterator();
		while (iterator.hasNext()) {
			IpcPrediction currentPrediction = iterator.next();

			// Remove existing predictions for this vehicle
			if (currentPrediction.getVehicleId().equals(vehicleId)) {
				// hold on to past prediction for future stop
				if (!isHistoricalPredictionForFutureStop(currentPrediction, currentTime)) {
					iterator.remove();
					continue;
				}
			}
			
			// Remove predictions that are expired. It makes sense to do this 
			// here when adding predictions since only need to take out 
			// predictions if more are being added.
			if (currentPrediction.getPredictionTime() < currentTime) {
					iterator.remove();
					continue;
			}
		}

		// Go through list and insert the new predictions into the 
		// appropriate places
		for (IpcPrediction newPredForRouteStop : newPredsForRouteStopDest) {
			boolean insertedPrediction = false;
			for (int i=0; i<predictionsForRouteStopDest.size(); ++i) {
				// If the new prediction is before the previous prediction
				// in currentPredsForRouteStop then insert it.
				if (newPredForRouteStop.getPredictionTime() < 
						predictionsForRouteStopDest.get(i).getPredictionTime()) {			
					// Actually add the prediction to the list
					predictionsForRouteStopDest.add(i, newPredForRouteStop);
					insertedPrediction = true;
					
					// Done with the inner for loop so break out of loop
					// and continue with next prediction.
					break;
				}
			}
			
			// If didn't find that the prediction was before one of the 
			// existing ones then insert it onto the end
			if (!insertedPrediction) {
				predictionsForRouteStopDest.add(newPredForRouteStop);
			}
		}
	}
	
	@Override
	public String toString() {
		return "IpcPredictionsForRouteStopDest [" 
				+ "routeId=" + routeId
				+ ", routeShortName=" + routeShortName 
				+ ", routeName=" + routeName 
				+ ", routeOrder=" + routeOrder
				+ ", stopId=" + stopId
				+ ", stopName=" + stopName 
				+ (stopCode != null ? (", stopCode=" + stopCode) : "")
				+ ", headsign=" + headsign
				+ ", directionId=" + directionId 
				+ ", distanceToStop=" + Geo.distanceFormat(distanceToStop)
				+ ", predictionsForRouteStop=" + predictionsForRouteStopDest 
				+ "]";
	}

	/**
	 * Intended for debugging
	 * @return
	 */
	public String toShortString() {
		return "IpcPredictionsForRouteStopDest [" 
				+ "routeId=" + routeId
				+ ", routeShortName=" + routeShortName
				+ ", routeOrder=" + routeOrder
				+ ", stopId=" + stopId
				+ ", stopName=" + stopName 
				+ (stopCode != null ? (", stopCode=" + stopCode) : "")
				+ ", headsign=" + headsign
				+ ", directionId=" + directionId 
				+ ", distanceToStop=" + Geo.distanceFormat(distanceToStop)
				+ "]";
	}
	
	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public String getRouteName() {
		return routeName;
	}
	
	public String getStopId() {
		return stopId;
	}

	public String getStopName() {
		return stopName;
	}

	public Integer getStopCode() {
		return stopCode;
	}
	
	public String getHeadsign() {
		return headsign;
	}

	public String getDirectionId() {
		return directionId;
	}

	/**
	 * Distance from location to the stop if getting predictions
	 * by location. Otherwise will return Double.NaN.
	 * 
	 * @return
	 */
	public double getDistanceToStop() {
		return distanceToStop;
	}
	
	public List<IpcPrediction> getPredictionsForRouteStop() {
		return predictionsForRouteStopDest;
	}

	public int getRouteOrder() {
		return routeOrder;
	}

}
