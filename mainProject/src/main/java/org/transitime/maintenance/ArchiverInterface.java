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

package org.transitime.maintenance;


/**
 * Specifies how a file is archived. The implementers of the interface are for
 * specific storage types, such as AWS Glacier.
 * 
 * @author SkiBu Smith
 *
 */
public interface ArchiverInterface {
	
	/**
	 * Called when file is to be uploaded to the archive.
	 * 
	 * @param fileName
	 *            Name of the file to be uploaded
	 * @param description
	 *            Description to be associated with the file being uploaded
	 * @return the archive ID if successful, otherwise null
	 */
	public abstract String upload(String fileName, String description);
}
