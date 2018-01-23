package org.transitime.avl.calAmp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.AvlModule;
import org.transitime.config.IntegerConfigValue;

public class CalAmpAvlModule extends AvlModule {

	private static IntegerConfigValue calAmpFeedPort = new IntegerConfigValue(
			"transitime.avl.calAmpFeedPort", 20500,
			"The port number for the UDP socket connection for the "
					+ "CalAmp GPS tracker feed.");

	private static final Logger logger = 
			LoggerFactory.getLogger(CalAmpAvlModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	public CalAmpAvlModule(String agencyId) {
		super(agencyId);
	}

	private void processPackets(DatagramSocket socket) {
		// Read from the socket
		try {
			// Create the DatagramPacket that data is read into
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			while (true) {
				// Get the bytes containing the report via UDP
				socket.receive(packet);
				
				// Convert the bytes into a report
				Report report = Report.parseReport(packet);
				
				// Actually process the report. Creates an AvlReport and 
				// processes it
				if (report != null)
					report.process();
			}
		} catch (Exception e) {
			logger.error("Exception while parsing CalAmp message. {}", 
					e.getMessage(), e);
		}		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Started module {} for agencyId={}", getClass().getName(),
				getAgencyId());

		while (true) {
			logger.info("Starting DatagramSocket on port {}",
					calAmpFeedPort.getValue());

			try {
				// Open up the DatagramSocket
				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket(calAmpFeedPort.getValue());
				} catch (SocketException e1) {
					logger.error("Exception occurred opening DatagramSocket "
							+ "on port {}. {}", calAmpFeedPort.getValue(),
							e1.getMessage(), e1);
					System.exit(-1);
				}

				// Process the data from the socket
				processPackets(socket);

				// If made it here something went wrong so close up
				// DatagramSocket and try again.
				socket.close();
			} catch (Exception e) {
				logger.error("Unexpected exception {}", e.getMessage(), e);
			}
		}
	}
	
}
