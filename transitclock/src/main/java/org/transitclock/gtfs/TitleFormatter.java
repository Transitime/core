/**
 * 
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
package org.transitclock.gtfs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;


/**
 * Tool for formatting titles in the GTFS data. Need to be able
 * to "unshout" titles (change "MAIN ST" to "Main St") yet 
 * capitalize abbreviations (e.g. "BART" or "US"). Can also
 * make sure that when using "&" or "@" that they have consistent
 * spaces around them.
 * 
 * The way this is done is that first the capitalization of the 
 * title is fixed. Each word starting at a delimiter is capitalized
 * while other characters are made lower case. Then regular expressions
 * are used to do special processing. The regular expressions are 
 * put into a file so that each agency can have a particular list.
 * The file name is passed to the constructor when creating a 
 * TitleFormatter.
 * 
 * Some examples of regular expressions and their corresponding replacement
 * text that can be useful for fixing titles are shown below. Useful documentation
 * at http://www.regular-expressions.info and at
 * http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
 * 
 *   -- For fixing capitalization of O'shaughnessy
 *   O's=>O'S 
 *   -- For fixing capitalization of abbreviation
 *   Bart=>BART
 *   -- For making sure there is space after a '&'.
 *   -- Note that "(?! )" means not a whitespace char
 *   -- and means the non-whitespace char
 *   -- will not actually get replaced.
 *   
 *   &(?! )=>&_      NOTE: last char actually a space!
 *   -- For making sure there is space before a '&'.
 *   (?<! )&=> &
 *   
 * @author SkiBu Smith
 *
 */
public class TitleFormatter {

	static private class RegexInfo {
		// The regex pattern of what is to be replaced
		public final String regex;
		
		// So don't have to compile pattern every time it is used.
		// Makes code more efficient.
		public final Pattern pattern; 
		
		// The string that the regex pattern is replaced with
		public final String replace;
		
		public RegexInfo(String regex, String replace) {
			this.regex = regex;
			this.pattern = Pattern.compile(regex);
			this.replace = replace;
		}
	}
	
	// It can be nice to know which regexs actually make a difference
	// so that if one isn't doing anything anymore it could be removed
	// and processing could then be sped up a bit since doing a 
	// regex for each title is expensive.
	private boolean logUnusedRegexs;
	private HashSet<String> regexesThatMadeDifference = 
			new HashSet<String>();
	
	private List<RegexInfo> regexReplaceList = 
			new ArrayList<RegexInfo>();
	
	private static final BooleanConfigValue capitalize = 
			new BooleanConfigValue("transitclock.gtfs.capitalize", 
					false, 
					"Sometimes GTFS titles have all capital letters or other "
					+ "capitalization issues. If set to true then will properly "
					+ "capitalize titles when process GTFS data. But note that "
					+ "this can require using regular expressions to fix things "
					+ "like acronyms that actually should be all caps.");
	
	private static final Logger logger= 
			LoggerFactory.getLogger(TitleFormatter.class);	

	/********************** Member Functions **************************/
	
	public TitleFormatter(String regexReplaceListFileName, boolean logUnusedRegexs) {
		this.logUnusedRegexs = logUnusedRegexs;
		
		try {
			processRegexFile(regexReplaceListFileName, "=>");
		} catch (IOException e) {
			logger.error("Could not open regexFile {}", 
					regexReplaceListFileName);
		}
	}
	
	/**
	 * Goes through list of regex replacements and returns true if the title
	 * passed in matches a replace string. Useful for determining if a title
	 * already replaced and shouldn't be changed further.
	 * 
	 * @param title
	 *            the title to check
	 * @return true if title matches a replace string
	 */
	public boolean isReplaceTitle(String title) {
		// Go through all title replacements that have been configured
		for (RegexInfo regexInfo : regexReplaceList) {
			if (regexInfo.replace.equals(title))
				// Found a match!
				return true;
		}
		
		// No match found
		return false;
	}
	
	/**
	 * Processes file containing a list of regular expressions along
	 * with the corresponding replacement text. 
	 * A line is considered a comment if it start with "--" or "//".
	 * 
	 * @param regexReplaceListFileName
	 * @throws IOException
	 */
	private void processRegexFile(String regexReplaceListFileName, String delimiter) 
			throws IOException {
		if (regexReplaceListFileName != null) {
			logger.info("Reading file {} for regex/replace pairs for titles",
					regexReplaceListFileName);
			
			FileInputStream fis = new FileInputStream(regexReplaceListFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {
				++lineNumber;
				
				// If line is a comment then skip to next line. 
				if (line.startsWith("--") || line.startsWith("//"))
					continue;
				
				// Also ignore blank lines.
				if (line.trim().isEmpty())
					continue;
				
				String[] contents = line.split(delimiter);
				if (contents.length != 2) {
					logger.error("Line #{} in file {} does not have two elements " + 
							"separated by the delimitor {}", 
							lineNumber, regexReplaceListFileName, delimiter);
					continue;
				}
				
				// Add the regex/replace pair to the list
				String regex = contents[0];
				String replace = contents[1];
				regexReplaceList.add(new RegexInfo(regex, replace));
				
				// Let user know what is being used
				logger.info("Adding regex/replace pair {}{}{}",
						regex, delimiter, replace);
			}
			reader.close();
		}	
	}
	
	/**
	 * Goes through list of regular expression and replaces the
	 * regular expression with the corresponding replacement text.
	 * @param original
	 * @return
	 */
	private String processRegexReplacements(final String original) {
		String result = original;
		for (RegexInfo regexInfo : regexReplaceList) {
			// Instead of just using String.replaceAll() use the already compiled
			// pattern to improve efficiency. pattern.matcher().replaceAll() is
			// same as String.replaceAll().
		    String newResult = regexInfo.pattern.matcher(result).replaceAll(regexInfo.replace);
			
			// If should log which regular expressions make a difference
			// do so. This won't always be enabled because the comparison
			// itself might be a bit expensive.
			if (logUnusedRegexs && !newResult.equals(result)) {
				regexesThatMadeDifference.add(regexInfo.regex);
			}
				
			result = newResult;
		}
		return result;
	}
	
	/**
	 * Capitalizes text so that first character after delimiter is capitalized
	 * but other characters are made lower case.
	 * 
	 * @param str
	 * @return
	 */
	public static String capitalize(String str) {
		// Delimiters specify word dividers. The text at beginning or  after a 
		// whitespace or to the right of a delimiter is capitalized. Otherwise
		// it will be in lower case.
		char delimiters[] = {'-', '/', '.', '&', '@', '(', ':', ';'};
		return capitalize(str, delimiters);
	}
	
	/**
	 * This method copied from org.apache.commons.lang.WordUtils
	 * but modified to also convert upper case characters to lower
	 * characters as needed.
	 * 
	 * @param str
	 * @param delimiters Characters after which should use capital letter.
	 * Don't need to add whitespace chars since those are already used
	 * as delimiters in isDelimiter().
	 * @return
	 */
	private static String capitalize(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}

		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		boolean capitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);

			if (isDelimiter(ch, delimiters)) {
				buffer.append(ch);
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(Character.toLowerCase(ch));
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Returns true of the character ch passed in is one of the 
	 * delimiters passed in. All whitespace automatically included
	 * as a delimiter.
	 * This method copied from org.apache.commons.lang.WordUtils
	 * @param ch
	 * @param delimiters
	 * @return
	 */
	private static boolean isDelimiter(char ch, char[] delimiters) {
		if (Character.isWhitespace(ch))
			return true;
		
		for (int i = 0, isize = delimiters.length; i < isize; i++) {
			if (ch == delimiters[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Takes a title, obtained from GTFS data, and makes it more 
	 * readable. First capitalized the text as well as possible.
	 * Then runs the configured regexs across the title so that
	 * other problems can be fixed. If many regexs are configured
	 * this could take a lot of processing time if there are a 
	 * large number of titles.
	 * 
	 * @param The original title. Can be null
	 * @return The formatted title. Null if passed in null.
	 */
	public String processTitle(String original) {
		// If pass in null then get back null
		if (original == null)
			return original;
		
		// First, properly capitalize the title
		String capitalizedStr = capitalize.getValue() ? 
				capitalize(original) : original;

		// Now that capitalization should mostly be correct, use
		// regexs configured in file to make other adjustments.
		// By doing the regexs after capitalization the regexs can
		// also be used to fixed complicated capitalization problems.
		String processed = processRegexReplacements(capitalizedStr);
		
		// Log any changes made
		if (!processed.equals(original)) {
			logger.debug("processTitle() changed title \"{}\" to \"{}\"",
					original, processed);
		}
		
		return processed;
	}

	/**
	 * Logs which configured regexs haven't made a difference. These could
	 * perhaps be unconfigured to speed up processing. 
	 */
	public void logRegexesThatDidNotMakeDifference() {
		// If logging of unused regexs wasn't even enable log an error
		if (!logUnusedRegexs) {
			logger.error("Cannot list regexs that made a difference because " + 
					"when the TitleFormatter was constructed this feature was " + 
					"not enabled.");
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		// For every regex that was configured...
		for (RegexInfo regexPair : regexReplaceList) {
			// If the configured regex didnd't make a difference, log such
			if (!regexesThatMadeDifference.contains(regexPair.regex)) {
				sb.append('"').append(regexPair.regex).append('"').append(", ");
			}
		}
		
		if (sb.length() > 0) {
			logger.info("Regexs that did not affect any titles and could " + 
					"be removed to possibly speed up processing are: " + sb.toString());
		} else {
			logger.info("All regexs that were configured made a difference. " + 
					"None need to be removed.");
		}
	}
	

}
