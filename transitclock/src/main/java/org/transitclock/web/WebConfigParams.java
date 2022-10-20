/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.web;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.StringConfigValue;

/**
 * Contains Java properties use by web server. These parameters are read in
 * using ReadConfigListener class.
 * 
 * @author Michael Smith
 *
 */
public class WebConfigParams {
	public static String getMapTileUrl() {
		return mapTileUrl.getValue();
	}
	private static StringConfigValue mapTileUrl = 
			new StringConfigValue("transitclock.web.mapTileUrl", 
					"http://otile4.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png",
					"Specifies the URL used by Leaflet maps to fetch map "
					+ "tiles.");

	public static String getMapTileCopyright() {
		return mapTileCopyright.getValue();
	}
	private static StringConfigValue mapTileCopyright =
			new StringConfigValue("transitclock.web.mapTileCopyright", 
					"MapQuest",
					"For displaying as map attributing for the where map tiles "
					+ "from.");

	private static BooleanConfigValue showLogOut =
			new BooleanConfigValue(
					"transitclock.web.login.showLogOut",
					false,
					"Whether logout link should be shown. Assumes some sort of authentication system is in place");

	public static boolean isShowLogout() {
		return showLogOut.getValue();
	}

	private static StringConfigValue logOutUrl =
			new StringConfigValue(
					"transitclock.web.login.logOutUrl",
					null,
					"Logout url used to log out of current session");

	public static String getLogoutUrl() {
		return logOutUrl.getValue();
	}
}
