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

package org.transitclock.trip;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.logging.Markers;
import org.transitclock.modules.Module;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.JsonUtils;
import org.transitclock.utils.Time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URLConnection;

/**
 * Subclass of Module to be used when reading Trip data from a feed. Calls the
 * abstract method getAndProcessData() for the subclass to actually get data
 * from the feed.
 */
public abstract class PollUrlTripModule extends Module {

    private static StringConfigValue url =
            new StringConfigValue("transitclock.trip.feedUrl",
                    "The URL of the Trip feed to poll.");

    private static StringConfigValue authenticationUser =
            new StringConfigValue("transitclock.trip.authenticationUser",
                    "If authentication used for the feed then this specifies "
                            + "the user.");

    private static StringConfigValue authenticationPassword =
            new StringConfigValue("transitclock.trip.authenticationPassword",
                    "If authentication used for the feed then this specifies "
                            + "the password.");

    private static BooleanConfigValue shouldProcessTripFeed =
            new BooleanConfigValue("transitclock.trip.shouldProcessTripFeed",
                    true,
                    "Usually want to process the Trip Feed if it is provided and if it contains " +
                            "supplemental data such as information about canceled trips.");

    private static IntegerConfigValue secondsBetweenTripFeedPolling =
            new IntegerConfigValue("transitclock.trip.feedPollingRateSecs", 5,
                    "How frequently a Trip feed should be polled for new data.");

    private static IntegerConfigValue tripFeedTimeoutInMSecs =
            new IntegerConfigValue("transitclock.trip.feedTimeoutInMSecs", 10000,
                    "For when polling Trip XML feed. The feed logs error if "
                            + "the timeout value is exceeded when performing the XML "
                            + "request.");

    // Usually want to use compression when reading data but for some Trip Feeds
    // feeds might be binary where don't want additional compression. A
    // superclass can override this value.

    private static final Logger logger = LoggerFactory
            .getLogger(PollUrlTripModule.class);

    /********************** Member Functions **************************/

    /**
     * Constructor
     *
     * @param agencyId
     */
    protected PollUrlTripModule(String agencyId) {
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
     * Override this method if Trip feed needs to specify header info
     * @param con
     */
    protected void setRequestHeaders(URLConnection con) {}

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
     * Abstract method for getting feed from a data source and then processing it.
     *
     * @throws Exception
     *             Throws a generic exception since the processing is done in
     *             the abstract method processData() and it could throw any type
     *             of exception since we don't really know how the Trip feed will
     *             be processed.
     */
    protected abstract void getAndProcessData(String url) throws Exception;


    /**
     * Does all of the work for the class. Runs forever and reads in
     * Trip data from feed and processes it.
     * @see Runnable#run()
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
                String[] urls = getUrl().split(",");
                for(String url : urls){
                    getAndProcessData(url);
                }
            } catch (SocketTimeoutException e) {
                logger.error(Markers.email(),
                        "Error for agencyId={} accessing Trip feed using URL={} "
                                + "with a timeout of {} msec.",
                        AgencyConfig.getAgencyId(), getUrl(),
                        tripFeedTimeoutInMSecs.getValue(), e);
            } catch (Exception e) {
                logger.error("Error accessing Trip feed using URL={}.",
                        getUrl(), e);
            }

            // Wait appropriate amount of time till poll again
            long elapsedMsec = timer.elapsedMsec();
            long sleepTime =
                    secondsBetweenTripFeedPolling.getValue()*Time.MS_PER_SEC -
                            elapsedMsec;
            if (sleepTime < 0) {
                logger.warn("Supposed to have a polling rate of " +
                        secondsBetweenTripFeedPolling.getValue()*Time.MS_PER_SEC +
                        " msec but processing previous data took " +
                        elapsedMsec + " msec so polling again immediately.");
            } else {
                Time.sleep(sleepTime);
            }
        }
    }

}