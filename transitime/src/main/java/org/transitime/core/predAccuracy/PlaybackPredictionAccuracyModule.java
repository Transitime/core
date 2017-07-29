package org.transitime.core.predAccuracy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.utils.PlaybackIntervalTimer;
import org.transitime.utils.Time;

public class PlaybackPredictionAccuracyModule extends PredictionAccuracyModule {
	private static final Logger logger = LoggerFactory
			.getLogger(PredictionAccuracyModule.class);
	public PlaybackPredictionAccuracyModule(String agencyId) {
		super(agencyId);
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Log that module successfully started
				logger.info("Started module {} for agencyId={}", 
						getClass().getName(), getAgencyId());
							
				// Run forever
				PlaybackIntervalTimer timer = new PlaybackIntervalTimer();
				while (true) {			
					// No need to run at startup since internal predictions won't be
					// generated yet. So sleep a bit first.
					
					Time.sleep(5000);
					if( timer.elapsedMsec() > getTimeBetweenPollingPredictionsMsec())
					{
						try {
							// Process data
							getAndProcessData(getRoutesAndStops(), Core.getInstance().getSystemDate());
							
							// Make sure old predictions that were never matched to an
							// arrival/departure don't stick around taking up memory.
							clearStalePredictions();
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("Error accessing predictions feed :  "+ e.getMessage(), e); 
						} 				
						timer.resetTimer();
					}			
				}
	}

}
