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

package org.transitime.custom.missionBay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.transitime.custom.missionBay.GtfsFromNextBus.Dir;

/**
 * Contains GTFS config information of stop and paths for the directions. Wanted
 * this info to be automatically generated from the NextBus API but there simply
 * is not sufficient information in the API. This is especially true given the
 * many different trip patterns there are for Mission Bay. For example, the west
 * route has 8 variations (morning/afternoon, nektar stop, caltrain stop.
 *
 * @author SkiBu Smith
 *
 */
public class MissionBayConfig {

	/********************** Member Functions **************************/

	/**
	 * Provides list of stops for each possible direction/trip pattern. Turns
	 * out the "west" loop for Mission Bay is completely screwy and has 4
	 * different patterns that are not specified via the NextBus API. So need to
	 * hard code it. Also, for all of the routes, some trips stop at Caltrain &
	 * 4th and some do not. Need to define every possibility.
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
			d1.tag = "loop";
			d1.shapeId = "east_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "powell", "library", "miss4th_s", "nektar",
					"miss4th_n", "chinbas", "calttown", "powell" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);

			Dir d1c = new Dir();
			d1c.tag = "loop_calt4th";
			d1c.shapeId = "east_loop";
			d1c.gtfsDirection = "0";
			String stops1c[] = { "powell", "calt4th", "library", "miss4th_s",
					"nektar", "miss4th_n", "chinbas", "calttown", "powell" };
			d1c.stopIds = Arrays.asList(stops1c);
			dirList.add(d1c);
		} else if (routeId.equals("west")) {
			// The morning loop without nektar
			Dir d1 = new Dir();
			d1.tag = "morning_loop";
			d1.shapeId = "west_morning_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "berr5th_n", "crescent_n",
					"1650owen", "409illi_w", "1650owen", "1500owen" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);

			Dir d1c = new Dir();
			d1c.tag = "morning_loop_calt4th";
			d1c.shapeId = "west_morning_loop";
			d1c.gtfsDirection = "0";
			String stops1c[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "calt4th", "berr5th_n", "crescent_n",
					"1650owen", "409illi_w", "1650owen", "1500owen" };
			d1c.stopIds = Arrays.asList(stops1c);
			dirList.add(d1c);

			// The morning loop with nektar
			Dir d2 = new Dir();
			d2.tag = "morning_loop_nektar";
			d2.shapeId = "west_morning_loop_nektar";
			d2.gtfsDirection = "0";
			String stops2[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "berr5th_n", "crescent_n",
					"1650owen", "409illi_w", "nektar", "1650owen", "1500owen" };
			d2.stopIds = Arrays.asList(stops2);
			dirList.add(d2);

			Dir d2c = new Dir();
			d2c.tag = "morning_loop_nektar_calt4th";
			d2c.shapeId = "west_morning_loop_nektar";
			d2c.gtfsDirection = "0";
			String stops2c[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "calt4th", "berr5th_n", "crescent_n",
					"1650owen", "409illi_w", "nektar", "1650owen", "1500owen" };
			d2c.stopIds = Arrays.asList(stops2c);
			dirList.add(d2c);

			// The afternoon loop without nektar
			Dir d3 = new Dir();
			d3.tag = "afternoon_loop";
			d3.shapeId = "west_afternoon_loop";
			d3.gtfsDirection = "0";
			String stops3[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "calt4th", "berr5th_n", "crescent_n",
					"409illi_e", "1650owen", "1500owen", };
			d3.stopIds = Arrays.asList(stops3);
			dirList.add(d3);

			Dir d3c = new Dir();
			d3c.tag = "afternoon_loop_calt4th";
			d3c.shapeId = "west_afternoon_loop";
			d3c.gtfsDirection = "0";
			String stops3c[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "berr5th_n", "crescent_n",
					"409illi_e", "1650owen", "1500owen" };
			d3c.stopIds = Arrays.asList(stops3c);
			dirList.add(d3c);

			// The afternoon loop with nektar
			Dir d4 = new Dir();
			d4.tag = "afternoon_loop_nektar";
			d4.shapeId = "west_afternoon_loop_nektar";
			d4.gtfsDirection = "0";
			String stops4[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "berr5th_n", "crescent_n",
					"409illi_e", "nektar", "1650owen", "1500owen" };
			d4.stopIds = Arrays.asList(stops4);
			dirList.add(d4);

			Dir d4c = new Dir();
			d4c.tag = "afternoon_loop_nektar_calt4th";
			d4c.shapeId = "west_afternoon_loop_nektar";
			d4c.gtfsDirection = "0";
			String stops4c[] = { "1500owen", "crescent_s", "berr5th_s",
					"calttown", "powell", "calt4th", "berr5th_n", "crescent_n",
					"409illi_e", "nektar", "1650owen", "1500owen" };
			d4c.stopIds = Arrays.asList(stops4c);
			dirList.add(d4c);		
		} else if (routeId.equals("loop")) {
			// For the loop route, the loop directions
			Dir d1 = new Dir();
			d1.tag = "loop";
			d1.shapeId = "loop_loop";
			d1.gtfsDirection = "0";
			String stops1[] = { "1650owen", "1400owen", "crescent_s",
					"berr5th_s", "calttown", "powell", "library",
					"miss4th", "nektar", "409illi_e", "1650owen" };
			d1.stopIds = Arrays.asList(stops1);
			dirList.add(d1);

			Dir d1c = new Dir();
			d1c.tag = "loop_calt4th";
			d1c.shapeId = "loop_loop";
			d1c.gtfsDirection = "0";
			String stops1c[] = { "1650owen", "1400owen", "crescent_s",
					"berr5th_s", "calttown", "powell", "calt4th", "library",
					"miss4th", "nektar", "409illi_e", "1650owen" };
			d1c.stopIds = Arrays.asList(stops1c);
			dirList.add(d1c);
		} else 
			return null;
		
		// Return results
		return dirList;
	}
	
	/**
	 * Defines the path data for all routes and puts it into the shapeIdsMap. Of
	 * course would have preferred to automatically determine the paths for each
	 * trip pattern using only the NextBus API but the naming conventions were
	 * far to haphazard. Only way to really make it work is to list the path IDs
	 * here manually by looking at data in (for route west for example)
	 * http://webservices
	 * .nextbus.com/service/publicXMLFeed?command=routeConfig&a
	 * =sf-mission-bay&verbose=true&r=west
	 * 
	 * @return Map containing all the shape information for the agency
	 */
	public static Map<String, List<String>> getPathData() {
		Map<String, List<String>> shapeIdsMap = new HashMap<String, List<String>>();
		
		// Route east, direction loop
		String pathIds_array_east_loop[] = { "east_powell_d",
				"east_powell_calt4th", "east_calt4th_d",
				"east_calt4th_library", "east_library_nektar", "east_nektar_d",
				"east_nektar_chinbas", "east_chinbas_d",
				"east_chinbas_calttown", "east_calttown_d",
				"east_calttown_powell" };
		List<String> pathIds_east_loop = Arrays.asList(pathIds_array_east_loop);
		shapeIdsMap.put("east_loop", pathIds_east_loop);

		// Route west, direction morning loop without nektar stop
		String pathIds_array_west_morning_loop[] = { 
				"west_1500owen_crescent_s", "west_crescent_s_berr5th_s",
				"west_berr5th_s_calttown", "west_calttown_powell",
				"west_powell_d", "west_powell_x_calt4th",
				"west_calt4th_berr5th_n", "west_berr5th_n_crescent_n",
				"west_crescent_1650owen", "west_1650owen_d",
				"west_1650owen_409illi_w", "west_409illi_w_d",
				"west_409illi_w_1650owen", "west_1500owen_d", };
		List<String> pathIds_west_morning_loop = Arrays
				.asList(pathIds_array_west_morning_loop);
		shapeIdsMap.put("west_morning_loop", pathIds_west_morning_loop);

		// Route west, direction morning loop with nektar stop
		String pathIds_array_west_morning_nektar_loop[] = { 
				"west_1500owen_crescent_s", "west_crescent_s_berr5th_s",
				"west_berr5th_s_calttown", "west_calttown_powell",
				"west_powell_d", "west_powell_x_calt4th",
				"west_calt4th_berr5th_n", "west_berr5th_n_crescent_n",
				"west_crescent_1650owen", "west_1650owen_d",
				"west_1650owen_409illi_w", "west_409illi_w_d",
				"west_409illi_w_nektar", "west_nektar_d",
				"west_nektar_1650owen", "west_1500owen_d", };
		List<String> pathIds_west_morning_nektar_loop = Arrays
				.asList(pathIds_array_west_morning_nektar_loop);
		shapeIdsMap.put("west_morning_loop_nektar",
				pathIds_west_morning_nektar_loop);

		// Route west, direction afternoon loop without nektar stop
		String pathIds_array_west_afternoon_loop[] = {
				"west_1500owen_crescent_s", "west_crescent_s_berr5th_s",
				"west_berr5th_s_calttown", "west_calttown_powell",
				"west_powell_d", "west_powell_x_calt4th",
				"west_calt4th_berr5th_n", "west_berr5th_n_crescent_n",
				"west_crescent_409illi_e", "west_409illi_e_d",
				"west_409illi_e_1650owen", "west_1500owen_d", };
		List<String> pathIds_west_afternoon_loop = Arrays
				.asList(pathIds_array_west_afternoon_loop);
		shapeIdsMap.put("west_afternoon_loop", pathIds_west_afternoon_loop);

		// Route west, direction afternoon loop with nektar stop
		String pathIds_array_west_afternoon_nektar_loop[] = {
				"west_1500owen_crescent_s", "west_crescent_s_berr5th_s",
				"west_berr5th_s_calttown", "west_calttown_powell",
				"west_powell_d", "west_powell_x_calt4th",
				"west_calt4th_berr5th_n", "west_berr5th_n_crescent_n",
				"west_crescent_409illi_e", "west_409illi_e_d",
				"west_409illi_e_nektar", "west_nektar_d",
				"west_nektar_1650owen", "west_1500owen_d", };
		List<String> pathIds_west_afternoon_nektar_loop = Arrays
				.asList(pathIds_array_west_afternoon_nektar_loop);
		shapeIdsMap.put("west_afternoon_loop_nektar",
				pathIds_west_afternoon_nektar_loop);
		
		// Route loop, direction loop
		String pathIds_array_loop_loop[] = { "loop_1650owen_d",
				"loop_1650owen_crescent_s", "loop_crescent_s_berr5th_s",
				"loop_berr5th_s_calttown", "loop_calttown_powell",
				"loop_powell_d", "loop_powell_calt4th", "loop_calt4th_library",
				"loop_library_nektar", "loop_nektar_d",
				"loop_nektar_409illi_e", "loop_409illi_e_d",
				"loop_409illi_e_1650owen" };
		List<String> pathIds_loop_loop = Arrays.asList(pathIds_array_loop_loop);
		shapeIdsMap.put("loop_loop", pathIds_loop_loop);
		
		return shapeIdsMap;
	}
	
}
