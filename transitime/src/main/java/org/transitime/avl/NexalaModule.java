/**
 * 
 */
package org.transitime.avl;

import java.io.InputStream;

import org.transitime.config.StringConfigValue;

/**
 * @author SeanOg
 *
 */
public class NexalaModule extends PollUrlAvlModule {
	/*********** Configurable Parameters for this module ***********/
	public static String getGtfsRealtimeURI() {
		return nexalaURI.getValue();
	}
	private static StringConfigValue nexalaURI =
			new StringConfigValue("transitime.avl.nexalaRealtimeFeedURI", 
					"t",
					"The URI of the Nexala realtime feed to use.");

	protected NexalaModule(String agencyId) {
		super(agencyId);		
	}

	/* (non-Javadoc)
	 * @see org.transitime.avl.PollUrlAvlModule#getUrl()
	 */
	@Override
	protected String getUrl() {
		// TODO Auto-generated method stub
		return getGtfsRealtimeURI();
	}

	/* (non-Javadoc)
	 * @see org.transitime.avl.PollUrlAvlModule#processData(java.io.InputStream)
	 */
	@Override
	protected void processData(InputStream in) throws Exception {
		// TODO Auto-generated method stub

	}

}
