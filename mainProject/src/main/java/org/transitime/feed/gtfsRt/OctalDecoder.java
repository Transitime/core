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

package org.transitime.feed.gtfsRt;

/**
 * GTFS-RT doesn't handle UTF-8 characters well when outputing human readable
 * format. Each UTF-8 character is output as a set of octal characters and the
 * octal characters are output as strings. Therefore a Chinese character will be
 * output as something like "\304\201\147". The convertOctalEscapedStringToUtf()
 * method in this class converts the octal strings into readable UTF-8
 * characters.
 * 
 * @author SkiBu Smith
 * 
 */
public class OctalDecoder {

	/**
	 * This method is from http://mindprod.com/jgloss/utf.html Copyright: (c)
	 * 2009-2014 Roedy Green, Canadian Mind Products, http://mindprod.com
	 * 
	 * decode a String from UTF-8 bytes. We handle only 16-bit chars.
	 * <p/>
	 * UTF-8 is normally decoded simply with new String( byte[], "UTF-8" ) or
	 * with an InputStreamReader but this is roughly what goes on under the
	 * hood, if you ever need to write your own decoder for some non-Java
	 * platform, or you are just curious how it works.
	 * <p/>
	 * This works for 16-bit characters only. It does not handle 32-bit
	 * characters encoded with the contortionist use of the low (0xdc00..0xdfff)
	 * and high(0xd800..0xdbff) bands of surrogate characters.
	 * 
	 * @param input
	 *            bytes encoded with UTF-8.
	 * 
	 * @return decoded string
	 */
	private static String decode(byte[] input) {
		char[] output = new char[input.length];
		// index input[]
		int i = 0;
		// index output[]
		int j = 0;
		while (i < input.length && input[i] != 0) {
			// get next byte unsigned
			int b = input[i++] & 0xff;
			// classify based on the high order 3 bits
			switch (b >>> 5) {
			default:
				// one byte encoding
				// 0xxxxxxx
				// use just low order 7 bits
				// 00000000 0xxxxxxx
				output[j++] = (char) (b & 0x7f);
				break;
			case 6:
				// two byte encoding
				// 110yyyyy 10xxxxxx
				// use low order 6 bits
				int y = b & 0x1f;
				// use low order 6 bits of the next byte
				// It should have high order bits 10, which we don't check.
				int x = input[i++] & 0x3f;
				// 00000yyy yyxxxxxx
				output[j++] = (char) (y << 6 | x);
				break;
			case 7:
				// three byte encoding
				// 1110zzzz 10yyyyyy 10xxxxxx
				assert (b & 0x10) == 0 : "UTF8Decoder does not handle 32-bit characters";
				// use low order 4 bits
				int z = b & 0x0f;
				// use low order 6 bits of the next byte
				// It should have high order bits 10, which we don't check.
				y = input[i++] & 0x3f;
				// use low order 6 bits of the next byte
				// It should have high order bits 10, which we don't check.
				x = input[i++] & 0x3f;
				// zzzzyyyy yyxxxxxx
				int asint = (z << 12 | y << 6 | x);
				output[j++] = (char) asint;
				break;
			}// end switch
		}// end while
		return new String(output, 0/* offset */, j/* count */);
	}

	/**
	 * 
	 * @param octalEscapedString
	 * @return
	 */
	public static String convertOctalEscapedString(
			String octalEscapedString) {
		byte[] originalBytes= octalEscapedString.getBytes();
		byte[] convertedBytes = new byte[originalBytes.length];
		
		// Goes through each byte of the original octal escaped string.
		// If escaped octal sequence found then the for "\xxx" bytes
		// are converted to a regular byte.
		for (int i=0, j=0; i<originalBytes.length; ++i,++j) {
			byte originalByte = originalBytes[i];
			// If escaped octal character handle it specially
			if (originalByte == '\\') {
				// Start of octal sequence
				int octalDigit1 = originalBytes[i+1]-'0';
				int octalDigit2 = originalBytes[i+2]-'0';
				int octalDigit3 = originalBytes[i+3]-'0';
				originalByte = (byte) ((octalDigit1 << 6) + (octalDigit2 << 3) + 
						octalDigit3);
				// Handle 3 extra characters so increment the index accordingly
				i = i+3;
			} 
			convertedBytes[j] = originalByte;
			
		}
		// Now that have proper byte stream, convert it to a string
		// and change to proper UTF-8 string.
		String decodedMessage = decode(convertedBytes);
		return decodedMessage;
	}

}
