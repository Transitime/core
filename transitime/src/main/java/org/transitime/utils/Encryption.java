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

package org.transitime.utils;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;

/**
 * For encrypting and decrypting strings.
 *
 * @author SkiBu Smith
 *
 */
public class Encryption {

	private static final StringConfigValue encryptionPassword =
			new StringConfigValue("transitime.db.encryptionPassword",
					"SET THIS!",
					"Used for encrypting, deencrypting passwords for storage "
					+ "in a database. This value should be customized for each "
					+ "implementation and should be hidden from users.");
	
	// Must call getEncryptor() to initialize and access
	private static BasicTextEncryptor textEncryptor = null;
	
	private static final Logger logger = LoggerFactory
			.getLogger(Encryption.class);

	/********************** Member Functions **************************/

	/**
	 * Encrypts the specified string using the configured encryptionPassword
	 * 
	 * @param str
	 *            String to be encrypted
	 * @return The encrypted string
	 */
	public static String encrypt(String str) {
		return getEncryptor().encrypt(str);
	}
	
	/**
	 * Decrypts the encrypted string using the configured encryptionPassword
	 * 
	 * @param encryptedStr
	 *            The string to decrypt
	 * @throws EncryptionOperationNotPossibleException
	 *             When the encryptionPassword is not correct
	 * @return The decrypted string
	 */
	public static String decrypt(String encryptedStr) 
			throws EncryptionOperationNotPossibleException {
		try {
			return getEncryptor().decrypt(encryptedStr);
		} catch (EncryptionOperationNotPossibleException e) {
			logger.error("Problem decrypting the encrypted string " 
					+ encryptedStr);
			throw e;
		}
	}
	 
	/**
	 * Creates the BasicTextEncryptor textEncryptor member and sets the password
	 * 
	 * @return A BasicTextEncryptor with the password set
	 */
	private static TextEncryptor getEncryptor() {
		if (textEncryptor == null) {
			textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(encryptionPassword.getValue());
		}
		
		return textEncryptor;
	}
	
}
