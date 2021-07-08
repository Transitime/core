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

package org.transitclock.avl;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.AvlConfig;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.logging.Markers;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.JsonUtils;
import org.transitclock.utils.Time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * Subclass of AvlModule to be used when reading AVL data from a feed. Calls the
 * abstract method getAndProcessData() for the subclass to actually get data
 * from the feed. The getAndProcessData() should call
 * processAvlReport(avlReport) for each AVL report read in. If in JMS mode then
 * it outputs the data to the appropriate JMS topic so that it can be read from
 * an AvlClient. If not in JMS mode then uses a BoundedExecutor with multiple
 * threads to directly call AvlClient.run().
 *
 * @author Michael Smith (michael@transitclock.org)
 *
 */
public abstract class PollUrlAvlModule extends AvlModule {

	private static StringConfigValue url =
			new StringConfigValue("transitclock.avl.url", 
					"The URL of the AVL feed to poll.");

	private static StringConfigValue authenticationUser =
			new StringConfigValue("transitclock.avl.authenticationUser", 
					"If authentication used for the feed then this specifies "
					+ "the user.");

	private static StringConfigValue authenticationPassword =
			new StringConfigValue("transitclock.avl.authenticationPassword", 
					"If authentication used for the feed then this specifies "
					+ "the password.");

	private static BooleanConfigValue shouldProcessAvl = 
			new BooleanConfigValue("transitclock.avl.shouldProcessAvl", 
					true,
					"Usually want to process the AVL data when it is read in "
					+ "so that predictions and such are generated. But if "
					+ "debugging then can set this param to false.");
	
	// Usually want to use compression when reading data but for some AVL
	// feeds might be binary where don't want additional compression. A
	// superclass can override this value.
	protected boolean useCompression = true;
	
	private static final Logger logger = LoggerFactory
			.getLogger(PollUrlAvlModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	protected PollUrlAvlModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * Feed specific URL to use when accessing data. Will often be
	 * overridden by subclass.
	 * 
	 * @return
	 */
	protected String getUrl() {
		return url.getValue();
	}
	
	/**
	 * Override this method if AVL feed needs to specify header info
	 * @param con
	 */
	protected void setRequestHeaders(URLConnection con) {}
	
	/**
	 * Actually processes the data from the InputStream. Called by
	 * getAndProcessData(). Should be overwritten unless getAndProcessData() is
	 * overwritten by superclass.
	 * 
	 * 
	 * @param in
	 *            The input stream containing the AVL data
	 * @return List of AvlReports read in
	 * @throws Exception
	 *             Throws a generic exception since the processing is done in
	 *             the abstract method processData() and it could throw any type
	 *             of exception since we don't really know how the AVL feed will
	 *             be processed.
	 */
	protected abstract Collection<AvlReport> processData(InputStream in)
			throws Exception;
	
	/**
	 * Converts the input stream into a JSON string. Useful for when processing
	 * a JSON feed.
	 * 
	 * @param in
	 * @return the JSON string
	 * @throws IOException
	 * @throws JSONException
	 */
	protected String getJsonString(InputStream in) throws IOException,
			JSONException {
		return JsonUtils.getJsonString(in);
	}
	
	/**
	 * Actually reads data from feed and processes it by opening up a URL
	 * specified by getUrl() and then reading the contents. Calls the abstract
	 * method processData() to actually process the input stream.
	 * <p>
	 * This method needs to be overwritten if not real data from a URL
	 * 
	 * @throws Exception
	 *             Throws a generic exception since the processing is done in
	 *             the abstract method processData() and it could throw any type
	 *             of exception since we don't really know how the AVL feed will
	 *             be processed.
	 */
	protected void getAndProcessData() throws Exception {
		// For logging
		IntervalTimer timer = new IntervalTimer(); 

		// Get from the AVL feed subclass the URL to use for this feed
		String[] fullUrls = getUrl().split(",");
		for (String fullUrl : fullUrls) {
			// Log what is happening
			logger.warn("Getting data from feed using url=" + fullUrl);

			// Create the connection
			URL url = new URL(fullUrl);
			URLConnection con = url.openConnection();

			// Set the timeout so don't wait forever
			int timeoutMsec = AvlConfig.getAvlFeedTimeoutInMSecs();
			con.setConnectTimeout(timeoutMsec);
			con.setReadTimeout(timeoutMsec);

			// Request compressed data to reduce bandwidth used
			if (useCompression)
				con.setRequestProperty("Accept-Encoding", "gzip,deflate");

			// If authentication being used then set user and password
			if (authenticationUser.getValue() != null
					&& authenticationPassword.getValue() != null) {
				String authString =
						authenticationUser.getValue() + ":"
								+ authenticationPassword.getValue();
				byte[] authEncBytes =
						Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);
				con.setRequestProperty("Authorization", "Basic "
						+ authStringEnc);
			}

			// Set any additional AVL feed specific request headers
			setRequestHeaders(con);

			// Create appropriate input stream depending on whether content is
			// compressed or not
			InputStream in = con.getInputStream();
			if ("gzip".equals(con.getContentEncoding())) {
				in = new GZIPInputStream(in);
				logger.debug("Returned data is compressed");
			} else {
				logger.debug("Returned data is NOT compressed");
			}

			// For debugging
			logger.warn("Time to access inputstream {} msec",
					timer.elapsedMsec());

			// Call the abstract method to actually process the data
			timer.resetTimer();
			Collection<AvlReport> avlReportsReadIn = processData(in);
			in.close();
			logger.warn("Time to parse document {} msec", timer.elapsedMsec());

			// Process all the reports read in
			if (shouldProcessAvl.getValue())
				processAvlReports(avlReportsReadIn);
		}
	}
	
	/** 
	 * Does all of the work for the class. Runs forever and reads in 
	 * AVL data from feed and processes it.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Started module {} for agencyId={}", 
				getClass().getName(), getAgencyId());
		
		// Run forever
		while (true) {
			IntervalTimer timer = new IntervalTimer();
			
			try {
				// Process data
				getAndProcessData();
			} catch (SocketTimeoutException e) {
				logger.error(Markers.email(),
						"Error for agencyId={} accessing AVL feed using URL={} "
						+ "with a timeout of {} msec.", 
						AgencyConfig.getAgencyId(), getUrl(), 
						AvlConfig.getAvlFeedTimeoutInMSecs(), e);
			} catch (Exception e) {
				logger.error("Error accessing AVL feed using URL={}.", 
						getUrl(), e);
			}
			
			// Wait appropriate amount of time till poll again
			long elapsedMsec = timer.elapsedMsec();
			long sleepTime = (long) (
					AvlConfig.getSecondsBetweenAvlFeedPolling()*Time.MS_PER_SEC - 
					elapsedMsec);
			if (sleepTime < 0) {
				logger.warn("Supposed to have a polling rate of " + 
						AvlConfig.getSecondsBetweenAvlFeedPolling()*Time.MS_PER_SEC +
						" msec but processing previous data took " +
						elapsedMsec + " msec so polling again immediately.");
			} else {
				Time.sleep(sleepTime);
			}
		}
	}
	
}
