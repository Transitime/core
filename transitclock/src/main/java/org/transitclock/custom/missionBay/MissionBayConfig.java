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

package org.transitclock.custom.missionBay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.transitclock.custom.missionBay.GtfsFromNextBus.Dir;

/**
 * Contains GTFS config information of stop and paths for the directions. Wanted
 * this info to be automatically generated from the NextBus API but there simply
 * is not sufficient information in the API. This is especially true given the
 * many different trip patterns there are for Mission Bay. For example, the caltrain
 * route has many variations.
 *
 * @author SkiBu Smith
 *
 */
public class MissionBayConfig {

	/********************** Member Functions **************************/

	/**
	 * Provides list of stops for each possible direction/trip pattern. Turns
	 * out this is rather complicated because of the variations in the trips
	 * so resorted to hard coding the stops.
	 * <p>
	 * Good way to determine which stops are part of a direction/trip is to look
	 * at the schedule API such as
	 * http://webservices.nextbus.com/s/xmlFeed?command=schedule&a=sf-mission-bay&r=west
	 * Then list the stops for each trip pattern below.
	 * 
	 * @param routeId
	 * @return
	 */
	public static List<Dir> getSpecialCaseDirections(String routeId) {
		// Array to be returned
		List<Dir> dirList = new ArrayList<Dir>();
		
		// Handle west route
		if (routeId.equals("east")) {
			// For east route, the loop directions
			Dir d1 = new Dir();
			d1.tag = "loop_am";
			d1.shapeId = "east_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "powell", "library",
					"chinbas", "nektar", "409illi_e", "powell" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);

//			Dir d2 = new Dir();
//			d2.tag = "loop_pm";
//			d2.shapeId = "east_loop_pm";
//			d2.gtfsDirection = "0";
//			String stops2[] = { "nektar", "409illi_e", "powell", "library",
//					"chinbas", "nektar" };
//			d2.stopIds = Arrays.asList(stops2);
//			dirList.add(d2);
		} else if (routeId.equals("west")) {
			// The morning loop without nektar
			Dir d1 = new Dir();
			d1.tag = "west_loop";
			d1.shapeId = "west_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "1650owen", "berr5th_s", "chinb185",
					"calcream", "powell", "calttown", "berr5th_n", "1650owen" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);
		} else if (routeId.equals("loop")) {
			// For the loop route, the loop directions
			Dir d1 = new Dir();
			d1.tag = "loop";
			d1.shapeId = "loop_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "1650owen", "berr5th_s", "calcream",
					"powell", "calttown", "library", "nektar",
					"409illi_e", "1650owen" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);
		} else if (routeId.equals("caltrain")) {
			// For the morning caltrain route, not going to Transbay
			Dir d1 = new Dir();
			d1.tag = "caltrain_am";
			d1.shapeId = "caltrain_am_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "1650owen", "calttown", "library", "chinbas",
					"nektar", "409illi_e", "1650owen" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);

			// For the morning caltrain route, going to Transbay
			Dir d2 = new Dir();
			d2.tag = "caltrain_am_transbay";
			d2.shapeId = "caltrain_am_transbay_loop";
			d2.gtfsDirection = "0";
			String stops2[] = { "1650owen", "berr5th_s", "chinb185", "ances153", "trans301", "130towns", "library", "chinbas",
					"nektar", "409illi_e", "1650owen" };
			d2.stopIds = Arrays.asList(stops2);
			dirList.add(d2);

			// For the afternoon caltrain route, not going to Transbay
			Dir d3 = new Dir();
			d3.tag = "caltrain_pm";
			d3.shapeId = "caltrain_pm_loop";
			d3.gtfsDirection = "0";
			String stops3[] = { "nektar", "409illi_e", "1650owen", "calttown",
					"library", "chinbas", "nektar" };
			d3.stopIds = Arrays.asList(stops3);
			dirList.add(d3);

			// For the afternoon caltrain route, going to Transbay
			Dir d4 = new Dir();
			d4.tag = "caltrain_pm_transbay";
			d4.shapeId = "caltrain_pm_transbay_loop";
			d4.gtfsDirection = "0";
			String stops4[] = { "nektar", "409illi_e", "1650owen", "calttown", "ances153", "trans301", "130towns", 
					"library", "chinbas", "nektar"};
			d4.stopIds = Arrays.asList(stops4);
			dirList.add(d4);
		} else 
			return null;
		
		// Return results
		return dirList;
	}
	
	/**
	 * Defines the path data for all routes and puts it into the shapeIdsMap. Of
	 * course would have preferred to automatically determine the paths for each
	 * trip pattern using only the NextBus API but the naming conventions were
	 * far too haphazard. Only way to really make it work is to list the path IDs
	 * here manually by looking at data in (for route west for example)
	 * http://webservices
	 * .nextbus.com/service/publicXMLFeed?command=routeConfig&a
	 * =sf-mission-bay&verbose=true&r=west
	 * From the URL you can see all of the paths. Then for each trip pattern defined
	 * in the above method getSpecialCaseDirections() you need to include all the 
	 * paths between the stops, including the "_d" departure paths, to get the full
	 * shape. 
	 * 
	 * @return Map containing all the shape information for the agency
	 */
	public static Map<String, List<String>> getPathData() {
		Map<String, List<String>> shapeIdsMap = new HashMap<String, List<String>>();
		
		// Route east, direction loop
		String pathIds_array_east_loop[] = { "east_powell_d",
				"east_powell_library", "east_library_chinbas",
				"east_chinbas_nektar",  "east_nektar_d",
				"east_nektar_409illi_e", "east_409illi_e_d",
				"east_409illi_e_powell", };
		List<String> pathIds_east_loop = Arrays.asList(pathIds_array_east_loop);
		shapeIdsMap.put("east_loop", pathIds_east_loop);

//		String pathIds_array_east_loop_pm[] = { "east_nektar_d",
//				"east_nektar_409illi_e", "east_409illi_e_d",
//				"east_409illi_e_powell", "east_powell_d",
//				"east_powell_library", "east_library_chinbas",
//				"east_chinbas_nektar",  };
//		List<String> pathIds_east_loop_pm = Arrays.asList(pathIds_array_east_loop_pm);
//		shapeIdsMap.put("east_loop_pm", pathIds_east_loop_pm);

		// Route west, direction loop
		String pathIds_array_west_loop[] = { 
				"west_1650owen_d", "west_1650owen_berr5th_s",
				"west_berr5th_s_chinb185", "west_chinb185_calcream",
				"west_calcream_powell", "west_powell_d",
				"west_powell_calttown", "west_calttown_berr5th_n",
				"west_berr5th_n_d", "west_berr5th_n_1650owen"};
		List<String> pathIds_west_loop = Arrays
				.asList(pathIds_array_west_loop);
		shapeIdsMap.put("west_loop", pathIds_west_loop);
		
		// Route loop, direction loop
		String pathIds_array_loop_loop[] = { "loop_1650owen_d",
				"loop_1650owen_berr5th_s", "loop_berr5th_s_calcream",
				"loop_calcream_powell", "loop_powell_d",
				"loop_powell_calttown", "loop_calttown_library",
				"loop_library_nektar", "loop_nektar_409illi_e",
				"loop_409illi_e_d", "loop_409illi_e_1650owen"};
		List<String> pathIds_loop_loop = Arrays.asList(pathIds_array_loop_loop);
		shapeIdsMap.put("loop_loop", pathIds_loop_loop);
		
		// Route caltrain, direction caltrain_am
		String pathIds_array_caltrain_am_loop[] = { "caltrain_1650owen_d",
				"caltrain_1650owen_calttown", "caltrain_calttown_d",
				"caltrain_calttown_library", "caltrain_library_d",
				"caltrain_library_chinbas", "caltrain_chinbas_nektar",
				"caltrain_nektar_d", "caltrain_nektar_409illi_e",
				"caltrain_409illi_e_1650owen", };
		List<String> pathIds_caltrain_am_loop = 
				Arrays.asList(pathIds_array_caltrain_am_loop);
		shapeIdsMap.put("caltrain_am_loop", pathIds_caltrain_am_loop);
		
		// Route caltrain, direction caltrain_am_transbay
		String pathIds_array_caltrain_am_transbay_loop[] = { 
				"caltrain_1650owen_d",
				"caltrain_1650owen_berr5th_s", 
				"caltrain_berr5th_s_chinb185",
				"caltrain_chinb185_ances153", "caltrain_ances153_d", 
				"caltrain_ances153_trans301", 
				"caltrain_trans301_130towns",
				"caltrain_130towns_library",
				"caltrain_library_d", "caltrain_library_chinbas",
				"caltrain_chinbas_nektar",
				"caltrain_nektar_d", "caltrain_nektar_409illi_e",
				"caltrain_409illi_e_1650owen" };
		List<String> pathIds_caltrain_am_transbay_loop = 
				Arrays.asList(pathIds_array_caltrain_am_transbay_loop);
		shapeIdsMap.put("caltrain_am_transbay_loop", pathIds_caltrain_am_transbay_loop);
		
		// Route caltrain, direction caltrain_pm
		String pathIds_array_caltrain_pm_loop[] = { 
				"caltrain_nektar_d", "caltrain_nektar_409illi_e",
				"caltrain_409illi_e_1650owen", "caltrain_1650owen_d",
				"caltrain_1650owen_calttown", "caltrain_calttown_d",
				"caltrain_calttown_library", "caltrain_library_d",
				"caltrain_library_chinbas", "caltrain_chinbas_nektar"};
		List<String> pathIds_caltrain_pm_loop = 
				Arrays.asList(pathIds_array_caltrain_pm_loop);
		shapeIdsMap.put("caltrain_pm_loop", pathIds_caltrain_pm_loop);
		
		// Route caltrain, direction caltrain_pm_transbay
		String pathIds_array_caltrain_pm_transbay_loop[] = { 
				"caltrain_nektar_d", "caltrain_nektar_409illi_e",
				"caltrain_409illi_e_1650owen", 				
				"caltrain_1650owen_d",
				"caltrain_1650owen_calttown", "caltrain_calttown_d",
				"caltrain_calttown_ances153", 
				"caltrain_ances153_trans301", 
				"caltrain_trans301_130towns",
				"caltrain_130towns_library",
				"caltrain_library_d", "caltrain_library_chinbas",
				"caltrain_chinbas_nektar",};
		List<String> pathIds_caltrain_pm_transbay_loop = 
				Arrays.asList(pathIds_array_caltrain_pm_transbay_loop);
		shapeIdsMap.put("caltrain_pm_transbay_loop", pathIds_caltrain_pm_transbay_loop);
		
		return shapeIdsMap;
	}
	
}
