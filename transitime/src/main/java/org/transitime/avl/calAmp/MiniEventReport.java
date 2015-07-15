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

import java.util.Date;

import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * Contains info for a CalAmp mini event report which is a simple GPS report.
 * Documentation is at https://puls.calamp.com/wiki/LM_Direct_Reference_Guide#
 * Mini_Event_Report_Message_.28Message_Type_10.29
 * 
 * @author SkiBu Smith
 * 
 */
public class MiniEventReport {

	private final int messageTime;
	private final double lat;
	private final double lon;
	private final short heading;
	private final short speedKph;
	private final byte fixStatus;
	private final short numberSatellites;
	private final byte communicationState;
	private final byte inputs;
	private final byte eventCode;
	
	/************************ Methods *************************/
	
	/**
	 * Constructor private to force use of getMiniEventReport().
	 * 
	 * @param gpsTime
	 * @param lat
	 * @param lon
	 * @param heading
	 * @param speedKph
	 * @param fixStatus
	 * @param numberSatellites
	 * @param communicationState
	 * @param inputs
	 * @param eventCode
	 */
	private MiniEventReport(int gpsTime, double lat, double lon, short heading,
			short speedKph, byte fixStatus, short numberSatellites,
			byte communicationState, byte inputs, byte eventCode) {
		super();
		this.messageTime = gpsTime;
		this.lat = lat;
		this.lon = lon;
		this.heading = heading;
		this.speedKph = speedKph;
		this.fixStatus = fixStatus;
		this.numberSatellites = numberSatellites;
		this.communicationState = communicationState;
		this.inputs = inputs;
		this.eventCode = eventCode;
	}
	
	/**
	 * Reads MiniEventReport from byte stream starting at the offset, which should
	 * be just past the message header.
	 * 
	 * @param bytes
	 * @param offset
	 * @return The MiniEventReport
	 */
	public static MiniEventReport getMiniEventReport(byte[] bytes, int offset) {
		int gpsTime = Message.readInt(bytes, offset);
		offset += 4;
		
		int latInt = Message.readInt(bytes, offset);
		double lat = latInt / 10000000.0;
		offset += 4;

		int lonInt = Message.readInt(bytes, offset);
		double lon = lonInt / 10000000.0;
		offset += 4;

		short heading = (short) Message.readShort(bytes, offset);
		offset += 2;
		
		short speedKph = bytes[offset];
		offset += 1;

		byte fixStatus = bytes[offset];
		offset += 1;
		
		short numberSatellites = (short) (fixStatus & 0x0F);
		
		byte communicationState = bytes[offset];
		offset += 1;
		
		byte inputs = bytes[offset];
		offset += 1;
		
		byte eventCode = bytes[offset];
		offset += 1;
		
		return new MiniEventReport(gpsTime, lat, lon, heading, speedKph,
				fixStatus, numberSatellites, communicationState, inputs,
				eventCode);
	}

	
	@Override
	public String toString() {
		return "MiniEventReport [" 
				+ "messageTime=" + messageTime + " " + new Date(getEpochTime())
				+ ", lat=" + lat
				+ ", lon=" + lon 
				+ ", heading=" + heading + "deg"
				+ ", speed=" + speedKph + "kph " + getSpeed() + "m/s"
				+ ", fixStatus=" + String.format("%02X", fixStatus)
				+ ", numberSatellites="	+ numberSatellites 
				+ ", communicationState=" + String.format("%02X", communicationState) 
				+ ", inputs=" + String.format("%02X", inputs)
				+ ", eventCode=" + String.format("%02X", eventCode) 
				+ "]";
	}

	/**
	 * Actually the message time, not the true GPS fix time, but it should be
	 * the same. Only message time is available for mini event reports.
	 * 
	 * @return epoch time in seconds
	 */
	public int getMessageTime() {
		return messageTime;
	}

	/**
	 * Actually the message time, not the true GPS fix time, but it should be
	 * the same. Only message time is available for mini event reports.
	 * 
	 * @return epoch time in msec
	 */
	public int getEpochTime() {
		return messageTime * Time.MS_PER_SEC;
	}
	
	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	/**
	 * @return Speed in degrees clockwise from true north
	 */
	public short getHeading() {
		return heading;
	}

	/**
	 * @return Speed in meters/second
	 */
	public float getSpeed() {
		return speedKph * Geo.KPH_TO_MPS;
	}

	public byte getFixStatus() {
		return fixStatus;
	}
	
	/**
	 * Whether GPS fix is actually valid. Looks at top 4 bits of fixStatus to
	 * determine if Invalid Time, Invalid Fix, Last Known, or Historic. If any
	 * of those bits are set then the fix is considered invalid.
	 * 
	 * @return true if GPS is not valid
	 */
	public boolean isValidGps() {
		return (fixStatus & 0xF0) != 0;
	}

	public short getNumberSatellites() {
		return numberSatellites;
	}

	public byte getCommunicationState() {
		return communicationState;
	}

	public byte getInputs() {
		return inputs;
	}

	public byte getEventCode() {
		return eventCode;
	}
	
}
