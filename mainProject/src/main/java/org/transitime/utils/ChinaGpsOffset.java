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
package org.transitime.utils;

/**
 * For dealing with the map offset issue in China. All maps have to be offset 
 * in China, a legacy from the cold war. So to properly show stops and shapes 
 * on a map in China need to offset the locations. Code is based on 
 * https://on4wp7.codeplex.com/SourceControl/changeset/view/21455#EvilTransform.cs 
 * as described at the very useful posting at 
 * http://www.sinosplice.com/life/archives/2013/07/16/a-more-complete-ios-solution-to-the-china-gps-offset-problem.
 * 
 * @author SkiBu Smith
 *
 */
public class ChinaGpsOffset {

    // Some constants - Krasovsky 1940
    //
    // a = 6378245.0, 1/f = 298.3
    // b = a * (1 - f)
    // ee = (a^2 - b^2) / a^2;
    final static double a = 6378245.0;
    final static double ee = 0.00669342162296594323;

    /**
     * This simple inner class created so don't depend 
     * on other existing class that is for use with db.
     */
    public static class LatLon {
    	private double lat;
    	private double lon;
    	
    	public LatLon(double lat, double lon) {
    		this.lat = lat;
    		this.lon = lon;
    	}
    	
    	public double getLat() {
    		return lat;
    	}
    	
    	public double getLon() {
    		return lon;
    	}
    }

    /**
     * World Geodetic System ==> Mars Geodetic System.
     * 
     * @param wgLat
     * @param wgLon
     * @return
     */
    public static LatLon transform(double wgLat, double wgLon) {
        if (outOfChina(wgLat, wgLon))
        	return new LatLon(wgLat, wgLon);

        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        double mgLat = wgLat + dLat;
        double mgLon = wgLon + dLon;
        return new LatLon(mgLat, mgLon);
    }

	/**
	 * Mars Geodetic System ==> World Geodetic System. Don't have an exact
	 * formula but the transformation is known to be pretty consistent and not
	 * vary that much over the roughly 500m difference that the transformation
	 * causes. Therefore simply determines for the lat/lon in Mars coordinates
	 * what the offset is for that location and then subtracts that offset. This
	 * provides a good initial result, but where the fifth digit of a lat/lon is
	 * still off by one. So now that we have a better idea of the location the
	 * geodetic==>mars offset is determined for this new location, new slightly
	 * more accurate offsets are determined, and the geodetic location is
	 * determined again. The second iteration results found to usually be
	 * accurate to about 7 decimal places but for a significant number of
	 * locations the 6th digit would change and for some positions the offset
	 * would continue to creep, which is undesirable. Therefore do one last
	 * iteration. After the third iteration very few locations offset the 6th
	 * digit and there is no creep, which is great.
	 * 
	 * @param marsLat
	 * @param marsLon
	 * @return
	 */
    public static LatLon transformBack(double marsLat, double marsLon) {
    	LatLon transformedLatLon = transform(marsLat, marsLon);
    	double latOffset = transformedLatLon.getLat() - marsLat;
    	double lonOffset = transformedLatLon.getLon() - marsLon;
    	LatLon firstResult = new LatLon(marsLat - latOffset, marsLon - lonOffset);
    	
    	transformedLatLon = transform(firstResult.getLat(), firstResult.getLon());
    	latOffset = transformedLatLon.getLat() - firstResult.getLat();
    	lonOffset = transformedLatLon.getLon() - firstResult.getLon();
    	LatLon secondResult = new LatLon(marsLat - latOffset, marsLon - lonOffset);
    	
    	transformedLatLon = transform(secondResult.getLat(), secondResult.getLon());
    	latOffset = transformedLatLon.getLat() - secondResult.getLat();
    	lonOffset = transformedLatLon.getLon() - secondResult.getLon();
    	LatLon thirdResult = new LatLon(marsLat - latOffset, marsLon - lonOffset);
    	
    	return thirdResult;
    }
    
    /**
     * Returns true if the lat/lon specified is within a rectangle that
     * very roughly describes China. Note that this will include locations
     * in parts of other adjacent countries that do not use the Mars
     * mapping!
     *  
     * @param lat
     * @param lon
     * @return
     */
    public static boolean outOfChina(double lat, double lon)  {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    /**
     * Yes, lots of undocumented transformation magic for latitudes.
     * 
     * @param x
     * @param y
     * @return
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * Yes, lots of undocumented transformation magic for longitudes.
     * 
     * @param x
     * @param y
     * @return
     */
    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
    
    public static void main(String[] args) {
    	LatLon original = new LatLon(34.79521,113.69259);
    	LatLon mars = transform(original.getLat(), original.getLon());
    	LatLon back = transformBack(mars.getLat(), mars.getLon());
    	System.out.println("Original = " + original.getLat() + ", " + original.getLon());
    	System.out.println("Mars     = " + mars.getLat() + ", " + mars.getLon());
    	System.out.println("Result   = " + back.getLat() + ", " + back.getLon());
    }
}