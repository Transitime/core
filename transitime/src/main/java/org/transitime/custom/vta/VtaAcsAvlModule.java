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
package org.transitime.custom.vta;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.PollUrlAvlModule;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

/**
 * For VTA Orbital ACS system. Reads in a text file containing the GPS info.
 * 
 * @author Michael Smith
 *
 */
public class VtaAcsAvlModule extends PollUrlAvlModule {

	private static StringConfigValue feedUrl = 
			new StringConfigValue("transitime.avl.vta.url", 
					"The URL of the ACS feed to use.");

	private static final Logger logger = LoggerFactory
			.getLogger(VtaAcsAvlModule.class);

	/********************** Member Functions **************************/

	public VtaAcsAvlModule(String agencyId) {
		super(agencyId);
	}

	@Override
	protected String getUrl() {
		return feedUrl.getValue();
	}

	/**
	 * Gets AVL data from input stream. Each line has data for a vehicle.
	 */
	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		BufferedReader buf =
				new BufferedReader(new InputStreamReader(in,
						StandardCharsets.UTF_8));

		// The return value for the method
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();

		String line;
		while ((line = buf.readLine()) != null) {
			String components[] = line.split(",");
			String vehicleId = components[0];
			@SuppressWarnings("unused")
			String route = components[1];
			String blockId = components[3];
			Double lat = Double.parseDouble(components[9]);
			Double lon = Double.parseDouble(components[10]);
			long epochSecs = Long.parseLong(components[11]);
			
			AvlReport avlReport =
					new AvlReport(vehicleId, epochSecs * Time.MS_PER_SEC, lat,
							lon, "VTA_ACS");
			avlReport.setAssignment(blockId, AssignmentType.BLOCK_ID);


			logger.debug("AVL report from VTA ACS feed: {}", avlReport);
			
			avlReportsReadIn.add(avlReport);
		}

		return avlReportsReadIn;
	}

	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		Module.start("org.transitime.custom.vta.VtaAcsAvlModule");
	}

}
