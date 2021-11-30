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
package org.transitclock.db.structs;

import java.io.Serializable;

import org.transitclock.utils.Geo;

/**
 * Simple vector that contains two locations.
 * 
 * @author SkiBu Smith
 *
 */
public class Vector implements Serializable {
	
	protected Location l1;
	protected Location l2;
	
	private static final long serialVersionUID = -3504110880635334536L;

	/********************** Member Functions **************************/
	
	public Vector(Location l1, Location l2) {
		this.l1 = l1;
		this.l2 = l2;
	}
	
	/**
	 * Returns the first Location of the Vector
	 * @return
	 */
	public Location getL1() {
		return l1;
	}
	
	/**
	 * Returns the second Location of the Vector
	 * @return
	 */
	public Location getL2() {
		return l2;
	}
	
	/**
	 * Returns the length of the Vector in meters.
	 * @return length of vector in meters
	 */
	public double length() {
		return l1.distance(l2);
	}

	public double lengthExact() {
		return l1.distanceExact(l2);
	}

	/**
	 * Determines the distance between a location and this vector. 
	 * Looks for a line between the location that is orthogonal
	 * to the line representing the vector. If the intersection with
	 * the line would actually be before or after the vector then
	 * the distance from the location to the corresponding end 
	 * point of the vector is returned. But if the intersection
	 * of the orthogonal line to the vector line is within the
	 * vector then the length of the orthogonal line is returned.
	 * 
	 * @param l The location
	 * @return Distance from location parameter to the vector
	 */
	public double distance(Location l) {
		return Geo.distance(l, this);
	}
	
	/**
	 * Returns length along vector where this location is closest
	 * to the vector. 
	 * @param l
	 * @return Distance along the vector that the location best matches
	 */
	public double matchDistanceAlongVector(Location l) {
		return Geo.matchDistanceAlongVector(l, this);
	}

	/**
	 * Returns in radians either the angle counterclockwise from the equator or
	 * the heading clockwise from north.
	 * 
	 * @param headingInsteadOfAngle
	 *            Specifies whether to return heading or angle
	 * @return
	 */
	private double orientation(boolean headingInsteadOfAngle) {
		Vector vx = new Vector(l1, new Location(l1.getLat(), l2.getLon()));
		double xLength = vx.length();
		if (l2.getLon() < l1.getLon())
			xLength = -xLength;
		
		Vector vy = new Vector(new Location(l1.getLat(), l2.getLon()), l2);
		double yLength = vy.length();
		if (l2.getLat() < l1.getLat())
			yLength = -yLength;

		// Return either the heading or the angle
		if (headingInsteadOfAngle)
			return Math.atan2(xLength, yLength); // heading
		else
			return Math.atan2(yLength, xLength); // angle
	}
	
	/**
	 * Returns the angle in radians of the vector from the equator. 
	 * Note that this is very different from heading().
	 * @return
	 */
	public double angle() {
		boolean headingInsteadOfAngle = false;
		return orientation(headingInsteadOfAngle);
	}
	
	/**
	 * Returns number of degrees clockwise from due North. Note that this
	 * is very different from angle().
	 * @return
	 */
	public double heading() {
		boolean headingInsteadOfAngle = true;
		return Math.toDegrees(orientation(headingInsteadOfAngle));
	}
	
	/**
	 * Returns the location that is the length specified
	 * along the vector.
	 * @param length
	 * @return Location along vector specified by length parameter
	 */
	public Location locAlongVector(double length) {
		Vector beginningVector = beginning(length);
		return beginningVector.getL2();
	}
	
	/**
	 * Returns the first part of the vector that is the length specified.
	 * 
	 * @param beginningLength The length of the beginning part of the vector
	 * to be returned
	 * @return The beginning part of the vector, having the length specified
	 */
	public Vector beginning(double beginningLength) {
		double l = length();
		double ratio = l == 0.0 ? 0.0 : beginningLength / length();
		Location newL2 = new Location(l1.getLat() + ratio * (l2.getLat() - l1.getLat()), 
                                   l1.getLon() + ratio * (l2.getLon() - l1.getLon()));
		return new Vector (l1, newL2);
	}
	
	/**
	 * Returns the last part of the vector, starting after the beginningLength
	 * 
	 * @param beginningLength The length after which the resulting vector 
	 * is to be returned.
	 * @return The end part of the vector, starting after the length specified
	 */
	public Vector end(double beginningLength) {
		double l = length();
		double ratio = l == 0.0 ? 0.0 : beginningLength / length();
		Location newL1 = new Location(l1.getLat() + ratio * (l2.getLat() - l1.getLat()), 
                                   l1.getLon() + ratio * (l2.getLon() - l1.getLon()));
		return new Vector (newL1, l2);		
	}
	
	/**
	 * Returns the middle of this vector that starts at length1 and
	 * ends at length2. The resulting length should be length2-length1.
	 * @param length1
	 * @param length2
	 * @return
	 */
	public Vector middle(double length1, double length2) {
		Vector beginningVector = beginning(length2);
		Vector middleVector = beginningVector.end(length1);
		return middleVector;
	}
	
	public static void main(String[] args) {
		Vector v = new Vector(new Location(37.79971, -122.43595), new Location(37.79972, -122.43596));
		double h1 = v.heading();
		System.err.println("h1=" + Geo.headingFormat((float) h1) + " degrees");

		double a1 = v.angle();
		System.err.println("a1=" + a1 + 
				" radians or " + Geo.headingFormat((float) Math.toDegrees(a1)));
		
		Location l = new Location(37.79971, -122.43595);
		Location offset = Geo.offset(l, 20.0, -70.0);
		System.err.println("\nname, lat, lon");
		System.err.println("l, " + Geo.format(l.getLat()) + ", " + Geo.format(l.getLon()));
		System.err.println("offset, " + Geo.format(offset.getLat()) + ", " + Geo.format(offset.getLon()));
	}

	@Override
	public String toString() {
		return "Vector [" +
				"l1=" + l1 + 
				", l2=" + l2 +
				", length=" + length() +
				"]";
	}
}
