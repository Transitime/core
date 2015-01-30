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
package org.transitime.core;

/**
 * Specifies the state of the block assignment for a vehicle.
 * 
 * @author SkiBu Smith
 *
 */
public enum BlockAssignmentMethod {
	// Block assignment came from AVL feed
	AVL_FEED_BLOCK_ASSIGNMENT,
	
	// AVL feed provided a route assignment
	AVL_FEED_ROUTE_ASSIGNMENT,
	
	// Separate block feed provided the assignment. Not currently implemented!
	BLOCK_FEED,
	
	// The auto assignment feature provided the assignment. 
	AUTO_ASSIGNER,
	
	// Vehicle finished the assignment or was assigned to another block
	ASSIGNMENT_TERMINATED,
	
	// For when another vehicle gets an exclusive assignment indicating that
	// the assignment needs to be removed from the old vehicle
	ASSIGNMENT_GRABBED,
	
	// Vehicle could not be matched to the assignment
	COULD_NOT_MATCH;
}
