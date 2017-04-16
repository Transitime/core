package org.transitime.custom.gtt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.Time;

public class GTTCsvAvlModule extends Module {
	private static final Logger logger = LoggerFactory.getLogger(GTTCsvAvlModule.class);

	private static StringConfigValue csvfolder = new StringConfigValue("transitime.gtt.csvfolder", "/data",
			"Folder to find archived GTT avl files.");

	// For running in real time
	private long lastAvlReportTimestamp = -1;

	private static BooleanConfigValue processInRealTime = new BooleanConfigValue("transitime.avl.processInRealTime",
			false,
			"For when getting batch of AVL data from a CSV file. "
					+ "When true then when reading in do at the same speed as "
					+ "when the AVL was created. Set to false it you just want " + "to read in as fast as possible.");

	public GTTCsvAvlModule(String agencyId) {
		super(agencyId);

	}

	private static int date_index = 1;
	private static int lat_index = 4;
	private static int long_index = 3;
	private static int vehicle_id_index = 2;

	@Override
	public void run() {

		File folder = new File(csvfolder.getValue());

		try {
			File[] files = folder.listFiles();
			Arrays.sort(files);
			for (File file : files) {
				logger.info(file.getAbsolutePath());
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					String data[] = line.split(",");

					SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					Date timestamp = dateFormatter.parse(data[date_index]);
					
					Double latitude = new Double(data[lat_index]);

					Double longitude = new Double(data[long_index]);
					
					AvlReport avlReport = new AvlReport(data[vehicle_id_index], timestamp.getTime(),
							latitude.doubleValue(), longitude.doubleValue(), Float.NaN, Float.NaN, "GTT");

					String assignmentId = "1079682";
					
					avlReport.setAssignment(assignmentId, AssignmentType.ROUTE_ID);

					logger.info("Processing avlReport={}", avlReport);

					// If configured to process data in real time them delay
					// the appropriate amount of time
					delayIfRunningInRealTime(avlReport);

					// Use the AVL report time as the current system time
					Core.getInstance().setSystemTime(avlReport.getTime());

					// Actually process the AVL report
					AvlProcessor.getInstance().processAvlReport(avlReport);
				}

			}
		} catch (FileNotFoundException e) {
			logger.error("Cannot find file.", e);
		} catch (IOException e) {
			logger.error("Cannot read file.", e);
		} catch (ParseException e) {
			logger.error("Date in wrong format.", e);
		}
	}

	/**
	 * If configured to process data in real time them delay the appropriate
	 * amount of time
	 * 
	 * @param avlReport
	 */
	private void delayIfRunningInRealTime(AvlReport avlReport) {
		if (processInRealTime.getValue()) {
			long delayLength = 0;

			if (lastAvlReportTimestamp > 0) {
				delayLength = avlReport.getTime() - lastAvlReportTimestamp;
				lastAvlReportTimestamp = avlReport.getTime();
			} else {
				lastAvlReportTimestamp = avlReport.getTime();
			}

			if (delayLength > 0)
				Time.sleep(delayLength);
		}
	}

}
