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

/**
 * For processing CalAmp message header.
 * 
 * @author Skibu Smith
 *
 */
public class MessageHeader {
	private final byte serviceType;
	private final byte messageType;
	private final short sequenceNumber;
	private final int nextPart;
	
	private static final byte EVENT_REPORT_MESSAGE = 2;
	private static final byte ID_REPORT_MESSAGE = 3;
	private static final byte MINI_EVENT_REPORT_MESSAGE = 10;

	/************************ Methods *************************/
	
	/**
	 * Constructor private to force use of getMessageHeader().
	 * 
	 * @param serviceType
	 * @param messageType
	 * @param sequenceNumber
	 * @param nextPart
	 */
	private MessageHeader(byte serviceType, byte messageType,
			short sequenceNumber, int nextPart) {
		this.serviceType = serviceType;
		this.messageType = messageType;
		this.sequenceNumber = sequenceNumber;
		this.nextPart = nextPart;
	}

	/**
	 * Reads message header from byte stream
	 * 
	 * @param bytes
	 * @param offset
	 * @return The MessageHeader read
	 */
	public static MessageHeader getMessageHeader(byte[] bytes, int offset) {
		byte serviceType = bytes[offset++];
		byte messageType = bytes[offset++];
		short sequenceNumber = Message.readShort(bytes, offset);
		offset += 2;
		
		return new MessageHeader(serviceType, messageType, sequenceNumber,
				offset);
	}
	
	public boolean isEventReport() {
		return messageType == EVENT_REPORT_MESSAGE;
	}
	
	public boolean isIdReport() {
		return messageType == ID_REPORT_MESSAGE;
	}
	
	public boolean isMiniEventReport() {
		return messageType == MINI_EVENT_REPORT_MESSAGE;
	}
	
	/**
	 * Returns offset in byte stream of where the message header starts.
	 * 
	 * @return index of start of message header
	 */
	public int getNextPart() {
		return nextPart;
	}
	
	@Override
	public String toString() {
		return "MessageHeader [serviceType=" + serviceType
				+ ", messageType=" + messageType + ", sequenceNumber="
				+ sequenceNumber + ", nextPart=" + nextPart + "]";
	}	

}
