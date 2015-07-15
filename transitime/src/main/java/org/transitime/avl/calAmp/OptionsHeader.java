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
 * For processing CalAmp options header.
 * 
 * @author Skibu Smith
 *
 */
public class OptionsHeader {
	private final String mobileId;
	private final String mobileIdType;
	private final int nextPart;
	
	private final static byte OPTIONS_BYTE = (byte) 0x83;
	
	/************************ Methods *************************/
	
	/**
	 * Constructor private to force use of getOptionsHeader().
	 * 
	 * @param mobileId
	 * @param mobileIdType
	 * @param nextPart
	 */
	private OptionsHeader(String mobileId, String mobileIdType, int nextPart) {
		this.mobileId = mobileId;
		this.mobileIdType = mobileIdType;
		this.nextPart = nextPart;
	}

	/**
	 * Reads options header from byte stream
	 * 
	 * @param bytes
	 * @return The OptionsHeader or null if there isn't one
	 */
	public static OptionsHeader getOptionsHeader(byte[] bytes) {
		int i = 0;
		if (bytes[i++] == OPTIONS_BYTE) {
			int mobileIdFieldLength = bytes[i++];
			StringBuilder mobileId = new StringBuilder();
			int start = i;
			while (i < start + mobileIdFieldLength)
				mobileId.append(String.format("%02X", bytes[i++]));

			int mobileIdTypeLength = bytes[i++];
			StringBuilder mobileIdType = new StringBuilder();
			start = i;
			while (i < start + mobileIdTypeLength)
				mobileIdType.append(String.format("%02X", bytes[i++]));

			return new OptionsHeader(mobileId.toString(),
					mobileIdType.toString(), i);
		} else
			return null;
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
		return "OptionsHeader [mobileId=" + mobileId + ", mobileIdType="
				+ mobileIdType + ", nextPart=" + nextPart + "]";
	}	

}
