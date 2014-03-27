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
package org.transitime.ipc.rmi;

import java.rmi.RemoteException;

import org.transitime.utils.Time;

/**
 * A sample RMI server class. Shows how RMI server class can be implemented.
 * 
 * @author SkiBu Smith
 *
 */
public class HelloServer extends AbstractServer implements Hello {

	static private HelloServer singleton = null;
	
	/********************** Member Functions **************************/

	/**
	 * Constructor. Made private so that can only be instantiated by
	 * getInstance(). Doesn't actually do anything since all the work is done in
	 * the superclass constructor.
	 */
	private HelloServer(String projectId, String objectName) {
		super(projectId, objectName);
	}
	
	static public HelloServer getInstance(String projectId) {
		// Create the singleton if need be
		if (singleton == null)
			singleton = new HelloServer(projectId, Hello.class.getSimpleName());
		
		// Return it
		return singleton;
	}

	/* (non-Javadoc)
	 * @see org.transitime.rmi.Hello#concat(java.lang.String, java.lang.String)
	 */
	@Override
	public String concat(String s1, String s2) throws RemoteException {
		// Sleep for a bit to simulate server getting bogged down
		Time.sleep(2000);
		
		return getProjectId() + ": " + s1 + s2;
	}
	
	public static void main(String args[]) {
		String projectId = args.length > 0 ? args[0] : "testProjectId";
		HelloServer.getInstance(projectId);
	}
}
