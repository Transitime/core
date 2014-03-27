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
package org.transitime.avl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.AvlConfig;
import org.transitime.utils.IntervalTimer;
import org.xml.sax.SAXException;

/**
 *
 * Polls XML feed for AVL data. To be overridden with subclass that
 * specifies URL to use and how to process the data.
 * 
 * If in playback then feed will not be run since will be getting
 * AVL data from db instead of from AVL feed.
 *  
 * @author SkiBu Smith
 * 
 */
public abstract class XmlPollingAvlModule extends AvlModule {
	
	private static final Logger logger= 
			LoggerFactory.getLogger(XmlPollingAvlModule.class);	

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * @param projectId
	 */
	public XmlPollingAvlModule(String projectId) {
		super(projectId);		
	}
	
	/**
	 * Feed specific URL to use when accessing data.
	 * @return
	 */
	protected abstract String getUrl();
	
	/**
	 * Extracts the AVL data from the XML document.
	 * Uses JDOM to parse the XML because it makes the Java code much simpler.
	 * @param doc
	 * @throws NumberFormatException
	 */
	protected abstract void extractAvlData(Document doc) 
			throws NumberFormatException;
	
	/**
	 * Reads the XML data file and processes it.
	 */
	protected void getAndProcessData() {
		// Get from the AVL feed subclass the URL to use for this feed
		String fullUrl = getUrl();
		
		// Log what is happening
		logger.info("Getting data from feed using url=" + fullUrl);
		
		// Load the document
		Document doc;
		try {
			doc = getDocument(fullUrl, AvlConfig.getAvlFeedTimeoutInMSecs());
		} catch (SocketTimeoutException e) {
			logger.error("Error parsing XML AVL feed using URL={} with a " +
					"timeout of {} msec.", 
					fullUrl, AvlConfig.getAvlFeedTimeoutInMSecs(), e);
			return;
		} catch (Exception e) {
			logger.error("Error parsing XML AVL feed using URL={}", fullUrl, e);
			return;
		}
		
		// Have the AVL feed subclass processes the document and extract the AVL data
		extractAvlData(doc);
	}
	
	/**
	 * Reads the specified XML document. 
	 * Uses JDOM to parse the XML because it makes the Java code much simpler.
	 * @param urlStr
	 * @param timeoutMsec Throws a SocketTimeoutException if accessing file 
	 * exceeds the timeout
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws JDOMException 
	 */
	protected Document getDocument(String urlStr, int timeoutMsec)
			throws IOException, SAXException, JDOMException,
			NumberFormatException {
		// For logging
		IntervalTimer timer = new IntervalTimer(); 
		
		// Create the connection
		URL url = new URL(urlStr);
		URLConnection con = url.openConnection();
		
		// Set the timeout so don't wait forever
		con.setConnectTimeout(timeoutMsec);
		con.setReadTimeout(timeoutMsec);
		
		// Request compressed data to reduce bandwidth used
		con.setRequestProperty("Accept-Encoding", "gzip,deflate");
		
		// Create appropriate input stream depending on whether content is 
		// compressed or not
		InputStream in = con.getInputStream();
		if ("gzip".equals(con.getContentEncoding())) {
		    in = new GZIPInputStream(in);
		    logger.debug("Returned XML data is compressed");
		} else {
		    logger.debug("Returned XML data is NOT compressed");			
		}

		// For debugging
		logger.debug("Time to access inputstream {} msec", 
				timer.elapsedMsec());
				
		// Read in the XML data to the document
		timer.resetTimer();
		SAXBuilder builder = new SAXBuilder();
		Document doc = (Document) builder.build(in);
		
		logger.debug("Time to parse XML document {} msec", timer.elapsedMsec());
		
		return doc;
	}
	
}
