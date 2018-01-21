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
package org.transitime.avl.calAmp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Report {
	
	protected final OptionsHeader optionsHeader;
	protected final MessageHeader messageHeader;
	
	protected static final Logger logger = 
			LoggerFactory.getLogger(Report.class);

	/************************ Methods *************************/
	
	protected Report(OptionsHeader optionsHeader, MessageHeader messageHeader) {
		this.optionsHeader = optionsHeader;
		this.messageHeader = messageHeader;
	}
	
	/**
	 * Actually process the already created report.
	 */
	public abstract void process();
	
	/**
	 * Returns the mobile ID associated with the report
	 * 
	 * @return mobile ID
	 */
	protected String getMobileId() {
		return optionsHeader.getMobileId();
	}
	
	/**
	 * The type of Mobile ID being used by the LMU: 
     *   0 – OFF 
     *   1 – Electronic Serial Number (ESN) of the LMU 
     *   2 – International Mobile Equipment Identifier (IMEI) or Electronic Identifier (EID) of the wireless modem 
     *   3 – International Mobile Subscriber Identifier (IMSI) of the SIM card (GSM/GPRS devices only) 
     *   4 – User Defined Mobile ID 
     *   5 – Phone Number of the mobile (if available) 
     *   6 – The current IP Address of the LMU 
     *   7 - CDMA Mobile Equipment ID (MEID) or International Mobile Equipment Identifier (IMEI) of the wireless modem
     *
	 * @return mobile ID type
	 */
	protected byte getMobileIdType() {
		return optionsHeader.getMobileIdType();
	}
	
	/**
	 * Convenience method for reading in a 4 byte integer from the bytes
	 * 
	 * @param bytes
	 * @param offset
	 * @return the read in integer
	 */
	protected static int readInt(byte[] bytes, int offset) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, 4);
		return byteBuffer.getInt();
	}
	
	/**
	 * Convenience method for reading in a 2 byte short from the bytes
	 * 
	 * @param bytes
	 * @param offset
	 * @return the read in integer
	 */
	protected static short readShort(byte[] bytes, int offset) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, 2);
		return byteBuffer.getShort();
	}
	
	/**
	 * Convenience method for reading in a 4 byte integer from the bytes
	 * 
	 * @param bytes
	 * @param offset
	 * @return the read in integer
	 */
	protected static int readByte(byte[] bytes, int offset) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, 1);
		return byteBuffer.get();
	}
	
	/**
	 * Reads the CalAmp report from the DatagramPacket bytes
	 * 
	 * @param packet
	 *            Contains the data
	 * @return The Report, or null if not successful
	 */
	public static Report parseReport(DatagramPacket packet) {
		byte[] bytes = packet.getData();

		// Log the entire message in hexadecimal format
		if (logger.isDebugEnabled()) {
			// Log total length of packets so have an idea of how much data 
			// being used. Header sizes are from
			// https://puls.calamp.com/wiki/LM_Direct_Reference_Guide
			int IP_HEADER_SIZE = 20;
			int UDP_HEADER_SIZE = 8;
			logger.debug("Message data is {} bytes long. Including IP Header "
					+ "and UDP header total size is {} bytes long.", 
					bytes.length, 
					bytes.length + IP_HEADER_SIZE + UDP_HEADER_SIZE);
			
			// Actually log message
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < packet.getLength(); ++i) {
				sb.append(String.format("%02X", bytes[i]));
			}
			logger.debug("Message={}", sb.toString());
		}

		try {
			// Read options header
			OptionsHeader optionsHeader = OptionsHeader.getOptionsHeader(bytes);
			int messageStartIdx =
					optionsHeader != null ? optionsHeader.getNextPart() : 0;
			logger.debug("Options header {}", optionsHeader);

			// Read message header, which specifies type of report
			MessageHeader messageHeader =
					MessageHeader.getMessageHeader(bytes, messageStartIdx);
			logger.debug("Message header {}", messageHeader);

			if (messageHeader.isMiniEventReport()) {
				MiniEventReport miniEventReport =
						MiniEventReport.getMiniEventReport(optionsHeader,
								messageHeader, bytes,
								messageHeader.getNextPart());
				return miniEventReport;
			} else {
				logger.info("Not a Mini Event Report so ignoring.");
			}
		} catch (Exception e) {
			logger.error("Exception while parsing CalAmp message. {}", 
					e.getMessage(), e);
		}
		
		// Didn't successfully create a report so return null
		return null;
	}

	/**
	 * Just for testing.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		int portNumber = 20500;
		
		logger.info("Starting up CalAmp UDP server on port {}", portNumber);
		
		// Create the DatagramPacket that data is read into
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		// Open up the DatagramSocket
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(portNumber);
		} catch (SocketException e1) {
			logger.error("Exception occurred opening DatagramSocket on port {}. {}",
					portNumber, e1.getMessage(), e1);
			System.exit(-1);
		}

		// Read from the socket
		try {
			while (true) {
				// Get the bytes containing the report via UDP
				socket.receive(packet);
				
				// Convert the bytes into a report
				Report report = parseReport(packet);
				
				// Actually process the report
				report.process();
			}
		} catch (Exception e) {
			logger.error("Exception while parsing CalAmp message. {}", 
					e.getMessage(), e);
		}

	}
}
