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
import org.transitime.ipc.servers.ConfigServer;

public class Message {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ConfigServer.class);

	/************************ Methods *************************/
		
	/**
	 * Convenience method for reading in a 4 byte integer from the bytes
	 * 
	 * @param bytes
	 * @param offset
	 * @return the read in integer
	 */
	public static int readInt(byte[] bytes, int offset) {
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
	public static short readShort(byte[] bytes, int offset) {
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
	public static int readByte(byte[] bytes, int offset) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, 1);
		return byteBuffer.get();
	}
	
	
	public static void parseMessage(DatagramPacket packet) {
		byte[] bytes = packet.getData();

		// Log the entire message in hexadecimal format
		if (logger.isDebugEnabled()) {
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

			// Read message header
			MessageHeader messageHeader =
					MessageHeader.getMessageHeader(bytes, messageStartIdx);
			logger.debug("Message header {}", messageHeader);

			if (messageHeader.isMiniEventReport()) {
				MiniEventReport miniEventReport =
						MiniEventReport.getMiniEventReport(bytes,
								messageHeader.getNextPart());
				logger.debug("Mini event report {}", miniEventReport);
			}
		} catch (Exception e) {
			logger.error("Exception while parsing CalAmp message. {}", 
					e.getMessage(), e);
		}
	}
	
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
				socket.receive(packet);
				parseMessage(packet);
			}
		} catch (Exception e) {
			logger.error("Exception while parsing CalAmp message. {}", 
					e.getMessage(), e);
		}

	}
}