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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;
import org.transitime.logging.Markers;

/**
 * For programmatically sending out an e-mail. Uses a file specified by
 * Java property transitime.utils.emailConfigFile to configure parameters.
 *
 * @author SkiBu Smith
 *
 */
public class EmailSender {

	/********************** Members ***********************************/
	
	private final Session session;
	
	/********************** Parameters ********************************/
	
	private static StringConfigValue emailConfigFile =
			new StringConfigValue("transitime.utils.emailConfigFile", 
					"/home/ec2-user/transitimeScripts/emailConfig.txt",
					"Specifies name of configuration file used for sending "
					+ "out e-mails.");

	/********************** Logging ***********************************/
	
	private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

	/********************** Member Functions **************************/

	/**
	 * Reads config properties from config file and creates a session that can
	 * be used to send e-mails.
	 */
	public EmailSender() {
		// Set user and password and returns session that can be used to
		// send a message.
		Properties props = getProperties();
		final String user = props.getProperty("mail.smtp.user");
		final String password = props.getProperty("mail.smtp.password");
		Authenticator authenticator = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		};
		
		session = Session.getDefaultInstance(props, authenticator);
	}
	
	/**
	 * Reads properties from file specified by emailConfigFile.
	 * <p>
	 * Unfortunately with Gmail can't seem to set the from address. It always
	 * ends up being the address of the Gmail account.
	 * 
	 * @return Java properties read from file
	 */
	private static synchronized Properties getProperties() {
		// Read in email properties file
		Properties properties = new Properties();
		Path path = Paths.get(emailConfigFile.getValue());
		try (InputStream input = Files.newInputStream(path)) {
			properties.load(input);
		} catch (IOException ex) {
			logger.error("Cannot open and load e-mail server properties "
					+ "file \"{}\".", emailConfigFile.getValue());
		}
		
		return properties;
	}

	/**
	 * Sends e-mail to the specified recipients.
	 * 
	 * @param recipients
	 *            Comma separated list of recipient e-mail addresses
	 * @param subject
	 *            Subject for the e-mail
	 * @param messageBody
	 *            The body for the e-mail
	 */
	public void send(String recipients, String subject, String messageBody) {
		Message message = new MimeMessage(session);
		
		try {
 
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipients));
			message.setSubject(subject);
			message.setText(messageBody);
 
			Transport.send(message);
 
			logger.info("Successfully sent e-mail to {} . The e-mail "
					+ "message subject was: \"{}\", and body was: \"{}\"", 
					recipients, subject, messageBody);
		} catch (MessagingException e) {
			// Since this is a serious issue log the error and send an e-mail 
			// via logback
			logger.error(Markers.email(), 
					"Failed sending e-mail. The e-mail config file {} "
					+ "specified by the Java property {} contains the login "
					+ "info to the SMTP server. Exception message: {}. "
					+ "The e-mail message subject was: \"{}\", and body "
					+ "was: \"{}\"",
					emailConfigFile.getID(), emailConfigFile.getValue(), 
					e.getMessage(), subject, messageBody);
		}
	}
	
	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		EmailSender email = new EmailSender();
		email.send("monitoring@transitime.org", "test subject", "test message");
	}

}
