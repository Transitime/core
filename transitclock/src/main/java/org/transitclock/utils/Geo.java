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
package org.transitclock.utils;

import java.text.DecimalFormat;

import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.Vector;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Some possibly handle geometric conversions.
 * 
 * @author SkiBu Smith
 *
 */
public class Geo {

	// So can output latitudes and longitudes with a consistent number of decimal places
	private static final DecimalFormat geoFormat = new DecimalFormat("0.00000");
	
	// So can output distances and such with a consistent number of decimal places
	private static final DecimalFormat twoDigitFormat = new DecimalFormat("0.00");
	
	// So can output headings and such with a consistent number of decimal places
	private static final DecimalFormat oneDigitFormat = new DecimalFormat("0.0");
	
	// For converting kilometers per hour to meters per second
	public static final float KPH_TO_MPS = 0.277778f;
	
	// For converting miles per hour to meters per second
	public static final float MPH_TO_MPS = 0.44704f;
	public static final float MPS_TO_MPH = 1.0f/MPH_TO_MPS;
	
	public static final double RADIUS_OF_EARTH_IN_METERS = 6371000;
	
	/**
	 * For formatting latitudes and longitudes to consistent 5 decimal places
	 */
	public static String format(double arg) {
		return geoFormat.format(arg);
	}
	
	/**
	 * For formatting distances in meters to consistent 2 decimal places.
	 * Appends "m" to indicate units.
	 * 
	 * @param arg
	 * @return
	 */
	public static String distanceFormat(double arg) {
		// Handle NaN and other special cases
		if (Double.isNaN(arg))
			return "NaN";
		if (arg == Double.MAX_VALUE) 
			return "Double.MAX_VALUE";
		
		// Not a special case so output the value with just two digits
		// past decimal place and append "m" to indicate meters.
		return twoDigitFormat.format(arg) + "m";
	}

	/**
	 * For formatting distances in meters to consistent 2 decimal places.
	 * Appends "m" to indicate units.
	 * 
	 * @param arg
	 * @return
	 */
	public static String distanceFormat(Double arg) {
		// Handle NaN and other special cases
		if (arg == null)
			return "null";
		if (arg.isNaN())
			return "NaN";
		if (arg == Double.MAX_VALUE) 
			return "Double.MAX_VALUE";
		
		// Not a special case so output the value with just two digits
		// past decimal place and append "m" to indicate meters.
		return twoDigitFormat.format(arg) + "m";
	}

	/**
	 * Outputs arg with just a single digit. No other formatting is done.
	 * Useful when want to output speed or heading but don't want to
	 * include units.
	 * 
	 * @param value
	 * @return
	 */
	public static String oneDigitFormat(double value) {
		return oneDigitFormat.format(value);
	}
	
	/**
	 * Outputs heading in degrees with just a single digit past the
	 * decimal point. Appends "deg" to indicate units.
	 * 
	 * @param arg
	 * @return
	 */
	public static String headingFormat(float arg) {
		// Handle NaN specially
		if (Float.isNaN(arg))
			return "NaN";
		
		return oneDigitFormat.format(arg) + " deg";
	}
	
	/**
	 * Outputs speed in m/s with just a single digit past the
	 * decimal point. Appends "m/s" to indicate units.
	 * 
	 * @param arg
	 * @return
	 */
	public static String speedFormat(float arg) {
		// Handle NaN specially
		if (Float.isNaN(arg))
			return "NaN";

		return oneDigitFormat.format(arg) + "m/s";
	}
	
	public static float converKmPerHrToMetersPerSecond(float kmPerHr) {
		return kmPerHr * KPH_TO_MPS;
	}
	
	/**
	 * Returns the distance between two locations. Uses the "haversine" 
	 * formula to calculate the great-circle distance between two points. 
	 * Therefore it is quite accurate, even over longer distances. But it
	 * requires more trigonometric calculations so runs slower than
	 * distance(). Difference in speed is approximately a factor of 3-4, 
	 * but it should be noted that it still runs very fast. On an old
	 * laptop running in Eclipse took 400msec for calculating 1 million 
	 * distances. So talking about 2.5 million distance calculations per second!
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static double distanceHaversine(Location l1, Location l2) {
		double dLat = Math.toRadians(l2.getLat() - l1.getLat());
		double dLon = Math.toRadians(l2.getLon() - l1.getLon());
		double lat1 = Math.toRadians(l1.getLat());
		double lat2 = Math.toRadians(l2.getLat());

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		           Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = RADIUS_OF_EARTH_IN_METERS * c;
		
		return d;
	}

	public static double distanceGreatCircle(Location l1, Location l2) {
		double lat1 = Math.toRadians(l1.getLat());
		double lon1 = Math.toRadians(l1.getLon());
		double lat2 = Math.toRadians(l2.getLat());
		double lon2 = Math.toRadians(l2.getLon());
		double deltaLon = lon2 - lon1;

		double y = sqrt(p2(cos(lat2) * sin(deltaLon))
						+ p2(cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)));
		double x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon);
		double radius = 6371.01 * 1000; // radius of earth in meters
		return radius * atan2(y, x);

	}

	private static final double p2(double a) {
		return a * a;
	}
	
	/**
	 * Returns the distance between two locations. This method is quicker
	 * than distanceHaversine() because it just uses Pythagoras theorem
	 * and assumes a equirectangular projection. But it should be fine 
	 * for relatively small distances, such as would get with a city.
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static double distance(Location l1, Location l2) {
		double lat1 = Math.toRadians(l1.getLat());
		double lon1 = Math.toRadians(l1.getLon());
		double lat2 = Math.toRadians(l2.getLat());
		double lon2 = Math.toRadians(l2.getLon());

		double x = (lon2-lon1) * Math.cos((lat1+lat2)/2);
		double y = (lat2-lat1);
		double d = Math.sqrt(x*x + y*y) * RADIUS_OF_EARTH_IN_METERS;
		
		return d;
	}

	/**
	 * Returns the Location l offset by deltaX and deltaY.
	 * @param l
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	public static Location offset(Location l, double deltaX, double deltaY) {
		double longitudeCosineFactor = Math.cos(Math.toRadians(l.getLat()));
		double deltaLonInRads = deltaX/(longitudeCosineFactor*RADIUS_OF_EARTH_IN_METERS);
		double deltaLatInRads = deltaY/RADIUS_OF_EARTH_IN_METERS;
		
		Location result = new Location(l.getLat() + Math.toDegrees(deltaLatInRads), 
				                       l.getLon() + Math.toDegrees(deltaLonInRads));
		return result;
	}
	
	/**
	 * A convenience method that makes writing equation that squares numbers
	 * a bit easier to read.
	 * @param d
	 * @return
	 */
	private static double sqrd(double d) {
		return d*d;
	}
	
	/**
	 * Determines the distance between a location and a vector. 
	 * Looks for a line between the location that is orthogonal
	 * to the line representing the vector. If the intersection with
	 * the line would actually be before or after the vector then
	 * the distance from the location to the corresponding end 
	 * point of the vector is returned. But if the intersection
	 * of the orthogonal line to the vector line is within the
	 * vector then the length of the orthogonal line is returned.
	 * 
	 * There are other algorithms that can be more efficient, but
	 * they are for Cartesian space. See http://geomalgorithms.com/a02-_lines.html
	 * for an example. But since using latitudes & longitudes on a sphere
	 * need to do things differently. Instead, need to just use distances
	 * between locations.
	 * 
	 * @param vector
	 * @param loc
	 */
	public static double distance(Location loc, Vector vector) {
		// d1 is distance from the location l to the first location of the vector v
		double d1 = distance(loc, vector.getL1());
		// d2 is distance from the location l to the second location of the vector v
		double d2 = distance(loc, vector.getL2());
		// v is length of the vector
		double v = distance(vector.getL1(), vector.getL2());
		
		// Handle v==0 where we have a zero length vector as a special case
		// so that don't divide by zero and end up with a NaN.
		if (v == 0.0)
			return d1;

		// v1 is the distance from the vector to where the
		// distance to the location is the shortest. It is where a line to
		// the location will be at a right angle to the vector.
		// we get two right angle triangles that split the vector into two 
		// distances, v1 and v2. Because these are right angle triangles we know that
		// a^2 + b^2 = c^2, where c is the longer diagonal side of the triangle.
		// This means that we have the following formulas:
		//   v1^2 + d^2 = d1^2
		//   v2^2 + d^2 = d2^2
		//   v1 + v2 = v;
		// If you solve for v1 you will find that it is
		double v1 = (sqrd(v) + sqrd(d1) - sqrd(d2)) / (2 * v);
		
		// We can now determine if the shortest distance
		// from the Location to the Vector is d1, d2, or a right angle line 
		// intersecting middle of the Vector. If v1 is negative then the
		// intersection is before the Vector starts and the shortest distance
		// is d1. If v1 is greater than length of v then intersection is
		// beyond the vector and the shortest distance is d2. Otherwise
		// the intersection is in the middle of the vector and can use
		// Pythagorean theorem that a^2 + b^2 = c^2. 
		if (v1 <= 0.0)
			return d1;
		if (v1 > v)
			return d2;
		
		// The shortest distance isn't to one of the end points of the vector.
		// This means that the shortest distance, let's call it d, is a right 
		// angle line to somewhere in the middle of the vector. For this situation 
		// we get two right angle triangles and can use a^2 + b^2 = c^2.		
		double dSquared = sqrd(d1) - sqrd(v1);
		// If started out with a right angle then sqrd(d1) - sqrd(v1) can
		// be slightly negative due to rounding error. If take sqrt() of
		// negative number get NaN when actually want 0.0. Therefore make
		// sure that dSquared not negative.
		if (dSquared < 0.0)
			dSquared = 0.0;
		
		// Determine and return the shortest distance
		double d = Math.sqrt(dSquared);		
		return d;
	}
	
	/**
	 * Same as distance() but returns NaN if the location is not along the vector.
	 * In other words, if closest match to the vector is beyond the beginning
	 * or end of the vector then it is not considered along the vector and NaN
	 * is returned.
	 * 
	 * @param loc
	 * @param vector
	 * @return Distance from loc to vector if loc matches to vector. Otherwise, NaN.
	 */
	public static double distanceIfMatch(Location loc, Vector vector) {
		// d1 is distance from the location l to the first location of the vector v
		double d1 = distance(loc, vector.getL1());
		// d2 is distance from the location l to the second location of the vector v
		double d2 = distance(loc, vector.getL2());
		// v is length of the vector
		double v = distance(vector.getL1(), vector.getL2());
		
		// Handle v==0 where we have a zero length vector as a special case
		// so that don't divide by zero and end up with a NaN.
		if (v == 0.0)
			return Double.NaN;

		// v1 is the distance from the vector to where the
		// distance to the location is the shortest. It is where a line to
		// the location will be at a right angle to the vector.
		// we get two right angle triangles that split the vector into two 
		// distances, v1 and v2. Because these are right angle triangles we know that
		// a^2 + b^2 = c^2, where c is the longer diagonal side of the triangle.
		// This means that we have the following formulas:
		//   v1^2 + d^2 = d1^2
		//   v2^2 + d^2 = d2^2
		//   v1 + v2 = v;
		// If you solve for v1 you will find that it is
		double v1 = (sqrd(v) + sqrd(d1) - sqrd(d2)) / (2 * v);
		
		// We can now determine if the shortest distance
		// from the Location to the Vector is d1, d2, or a right angle line 
		// intersecting middle of the Vector. If v1 is negative then the
		// intersection is before the Vector starts and the shortest distance
		// is d1. If v1 is greater than length of v then intersection is
		// beyond the vector and the shortest distance is d2. Otherwise
		// the intersection is in the middle of the vector and can use
		// Pythagorean theorem that a^2 + b^2 = c^2. 
		if (v1 <= 0.0)
			return Double.NaN;
		if (v1 > v)
			return Double.NaN;
		
		// The shortest distance isn't to one of the end points of the vector.
		// This means that the shortest distance, let's call it d, is a right 
		// angle line to somewhere in the middle of the vector. For this situation 
		// we get two right angle triangles and can use a^2 + b^2 = c^2.		
		double dSquared = sqrd(d1) - sqrd(v1);
		// If started out with a right angle then sqrd(d1) - sqrd(v1) can
		// be slightly negative due to rounding error. If take sqrt() of
		// negative number get NaN when actually want 0.0. Therefore make
		// sure that dSquared not negative.
		if (dSquared < 0.0)
			dSquared = 0.0;
		
		// Determine and return the shortest distance
		double d = Math.sqrt(dSquared);		
		return d;		
	}
	
	/**
	 * Determines best match of location to the vector. Returns the distance
	 * along the vector to that match. Uses same basic algorithm as distance().
	 * @param loc
	 * @param vector
	 * @return
	 */
	public static double matchDistanceAlongVector(Location loc, Vector vector) {
		// d1 is distance from the location l to the first location of the vector v
		double d1 = distance(loc, vector.getL1());
		// d2 is distance from the location l to the second location of the vector v
		double d2 = distance(loc, vector.getL2());
		// v is length of the vector
		double v = distance(vector.getL1(), vector.getL2());
		
		// Handle v==0 where we have a zero length vector as a special case
		// so that don't divide by zero and end up with a NaN.
		if (v == 0.0)
			return 0.0;
		
		// v1 is the distance from the vector to where the
		// distance to the location is the shortest. It is where a line to
		// the location will be at a right angle to the vector.
		// we get two right angle triangles that split the vector into two 
		// distances, v1 and v2. Because these are right angle triangles we know that
		// a^2 + b^2 = c^2, where c is the longer diagonal side of the triangle.
		// This means that we have the following formulas:
		//   v1^2 + d^2 = d1^2
		//   v2^2 + d^2 = d2^2
		//   v1 + v2 = v;
		// If you solve for v1 you will find that it is
		double v1 = (sqrd(v) + sqrd(d1) - sqrd(d2)) / (2 * v);
				
		// We can now determine if the shortest distance
		// from the Location to the Vector is d1, d2, or a right angle line 
		// intersecting middle of the Vector. If v1 is negative then the
		// intersection is before the Vector starts and the shortest distance
		// is d1. If v1 is greater than length of v then intersection is
		// beyond the vector and the shortest distance is d2. Otherwise
		// the intersection is in the middle of the vector and can use
		// v1 which was already calculated. 
		if (v1 <= 0.0)
			return 0.0;
		if (v1 > v)
			return v;
		return v1;
	}
	
	/**
	 * For shifting a path to the right. This is useful when the path data is
	 * simply centerline street data, where the stopPaths for the different 
	 * directions overlap each other. By displacing the stopPaths to the right
	 * a bit they don't overlap when zoomed in.
	 * 
	 * This method takes in three locations that define two vectors and
	 * hence the vertex that needs to be relocated.
	 * 
	 * This method determines a stub line that goes from the vertex parameter
	 * to the new offset location. The stub line should be such that when 
	 * connecting all the new vertexes they new stopPaths will be the distance
	 * specified from the old path. The result will often not be exactly the
	 * distance away from the original vertex because if the angle between
	 * the vectors is greater than 180 degrees than the stub would need
	 * to be longer than the distance specified in order for the new stopPaths
	 * to be the distance away from the old stopPaths. But this would be 
	 * problematic because really sharp turns could cause the stub to be
	 * incredibly long. Therefore it is limited to 1.5 of distance.
	 * 
	 * @param l1
	 * @param vertex
	 * @param l2
	 * @param distance to offset location l1 to the right. If negative then
	 * offset to the left.
	 * @return New vertex that is roughly the distance away from the specified
	 * vertex.
	 */
	public static Location rightOffsetVertex(Location l1, 
			Location vertex, 
			Location l2, 
			double distance) {
		// Create the vectors going into and out of the vertex
		// so can do the necessary math.
		Vector v1 = new Vector(l1, vertex);
		Vector v2 = new Vector(vertex, l2);
		
		// First determine the angle between the vectors. If the vectors
		// go in the same direction they angle is 180 degrees, which
		// of course is Pi.
		double angleBetweenVectors = Math.PI - (v1.angle() - v2.angle());

		// Determine angle of the stub that goes from the original vertex to the new
		// vertex that is displaced to the right of the vectors by distance d.
		double angleOfStub = -angleBetweenVectors/2 + v2.angle();
		
		// Determine the necessary length of the stub between the old vertex
		// and the new displaced one
		double lengthOfStub = distance/Math.sin(angleBetweenVectors/2.0);
		
		// There are situations where the path can turn back 180 degrees
		// which causes the angleBetweenVectors to be approximately 0.0.
		// This causes a division by zero and can result in a large negative
		// value for lengthOfStub. For this situation limit lengthOfStub 
		// to 0.0 .
		if (Math.abs(angleBetweenVectors) < Math.toRadians(10 /* degrees */))
			lengthOfStub = 0.0;
		
		// If the path turns back onto itself then the stub path would have
		// to have infinite length for the two vectors to have the proper 
		// from the original vectors. This is not ideal. Therefore limit 
		// the length of the stub.
		if (Math.abs(lengthOfStub) > Math.abs(distance*1.5))
			lengthOfStub = distance*1.5;
		
		// Determine how much in meters in X and Y direction need to
		// offset from the vertex.
		double stubX = lengthOfStub * Math.cos(angleOfStub);
		double stubY = lengthOfStub * Math.sin(angleOfStub);
		
		// Determine and return the new vertex that is offset
		// by the specified distance.
		Location offsetVertex = offset(vertex, stubX, stubY);		
		return offsetVertex;
	}
	
	/**
	 * For determining an offset path that is the distance away from the
	 * original path. Useful for when centerline data used for shapes.
	 * This allows the stopPaths in the different directions to not completely 
	 * overlap. This method is for determining the offset for a first or
	 * last point of a set of points that define a shape/path.
	 * 
	 * @param l1 for specifying vector
	 * @param l2 for specifying vector
	 * @param distance to offset location l1 to the right. If negative then
	 * offset to the left.
	 * @return
	 */
	public static Location rightOffsetBeginningLoc(Location l1, Location l2, double distance) {
		// Determine orientation of the vector connecting the locations
		Vector v = new Vector(l1, l2);
		double vectorAngle = v.angle();
		
		// Determine how much need to offset l1 in order for the new location
		// to be distance away orthogonally to l1
		double deltaX = distance * Math.sin(vectorAngle);
		double deltaY = distance * -Math.cos(vectorAngle);
		
		return offset(l1, deltaX, deltaY);
	}
	
	/**
	 * Just like rightOffsetBeginningLoc() except offsets l2 instead of l1.
	 * 
	 * @param l1 for specifying vector
	 * @param l2 for specifying vector
	 * @param distance to offset location l1 to the right. If negative then
	 * offset to the left.
	 * @return
	 */
	public static Location rightOffsetEndLoc(Location l1, Location l2, double distance) {
		// Determine orientation of the vector connecting the locations
		Vector v = new Vector(l1, l2);
		double vectorAngle = v.angle();
		
		// Determine how much need to offset l1 in order for the new location
		// to be distance away orthogonally to l1
		double deltaX = distance * Math.sin(vectorAngle);
		double deltaY = distance * -Math.cos(vectorAngle);
		
		return offset(l2, deltaX, deltaY);
	}

	/**
	 * Just for debugging
	 * @param args
	 */
	public static void main(String[] args) {
		double d = 30.0;
		Location l1 = new Location(37.800, -122.436);
		Location l2 = new Location(37.805, -122.436);
		Location l3 = new Location(37.801, -122.437);
		Location offsetVertex = rightOffsetVertex(l1, l2, l3, d);
		
		System.err.println("name,color,lat,lon");
		System.err.println("l1, red, " + l1.getLat() + ", " + l1.getLon());
		System.err.println("l2, red, " + l2.getLat() + ", " + l2.getLon());
		System.err.println("l3, red, " + l3.getLat() + ", " + l3.getLon());
		System.err.println("offsetVertex, green, " + offsetVertex.getLat() + ", " + offsetVertex.getLon());
//		
//		Location loc = new Location(40.75, -73.97);
//		Location l1 = new Location(40.7486, -73.9864);
//		Location l2 = new Location(40.7586, -73.9664);
//		Vector v1 = new Vector(l1, l2);
//		Vector v2 = new Vector(l2, l1);
//		double vlength = v1.length();
//		
//		double d1 = distance(loc,  v1);
//		double md1 = matchDistanceAlongVector(loc, v1);
//		
//		double d2 = distance(loc,  v2);
//		double md2 = matchDistanceAlongVector(loc, v2);
//		
//		// Make sure the values are ok. Should get the same distance
//		// no matter which was vector is pointing.
//		assert Math.abs(d1-d2) < 0.000001;			
//		// The distance to the match for the vectors going in 
//		// different directions should add up to v1length
//		assert Math.abs(md1 + md2-vlength) < 0.000001;
//		
//		System.out.println("vlength=" + vlength + "\n" +
//				"d1=" + dFormat(d1) + " md1=" + dFormat(md1) + "\n" +
//				"d2=" + dFormat(d2) + " md2=" + dFormat(md2) + "\n");
	}
		
	/**
	 * Returns true if the heading specified by vehicleHeading is
	 * within the allowableDelta of segmentHeading. All angles
	 * are expressed in degrees. 
	 * 
	 * @param vehicleHeading Heading of vehicle. If Float.NaN then
	 * method will return true
	 * @param segmentheading
	 * @param allowableDelta
	 * @return
	 */
	public static boolean headingOK(float vehicleHeading, float segmentheading,
			float allowableDelta) {
		// If the heading is unknown then have to always consider heading 
		// acceptable.
		if (Float.isNaN(vehicleHeading))
			return true;
		
		float delta = Math.abs(vehicleHeading - segmentheading);
		// Make sure delta not above 360 degrees
		while (delta > 360.0f)
			delta -= 360.0f;
		
		// If greater than 180.0 then need to look at delta in the other direction
		if (delta > 180.0f)
			delta = 360.0f - delta;
		
		// Return results
		return delta < allowableDelta;
	}
}
