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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Takes several ordered lists and combines them into a single one. Useful
 * for determining things such as a stop list for a direction when there
 * are multiple trip patterns. Each item will be included only once.
 *
 * @author SkiBu Smith
 *
 */
public class OrderedCollection {

	private final List<String> list = new LinkedList<String>();
	private final Set<String> set = new HashSet<String>();
	
	/********************** Member Functions **************************/

	/**
	 * Quick way of adding the first list.
	 * 
	 * @param originalList
	 */
	public void addOriginal(Collection<String> originalList) {
		list.addAll(originalList);
		set.addAll(originalList);
	}
	
	/**
	 * Adds items to the list at the specified index.
	 * 
	 * @param index
	 * @param itemsToAdd
	 */
	private void add(int index, List<String> itemsToAdd) {
		for (String itemToAdd : itemsToAdd) {
			// Insert into proper place in list
			list.add(index++, itemToAdd);
			set.add(itemToAdd);
		}
	}
	
	/**
	 * Adds items that are not already in the list to the list. 
	 * @param newList
	 */
	public void add(Collection<String> newList) {
		// Groups of items in a row that haven't yet been added to list.
		// They are grouped so that can find the right place to insert them.
		List<String> itemsToAdd = new ArrayList<String>();
		
		// For each of the new items...
		for (String item : newList) {
			// If item already in the list then don't need to add it
			if (set.contains(item)) {
				// If there are items to add, then add them now
				if (!itemsToAdd.isEmpty()) {
					// Determine the insertion point
					int insertionPoint = list.indexOf(item);
					
					// Add the items to the proper place
					add(insertionPoint, itemsToAdd);
					
					// Done inserting these new items so clear the list
					itemsToAdd.clear();
				}
			} else {
				// Item not already in list so keep track of it
				itemsToAdd.add(item);
			}
		}
		
		// If still have extra items from newList then append those to end
		add(list.size(), itemsToAdd);
	}
	
	public List<String> get() {
		return list;
	}
	
	/**
	 * For testing.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		List<String> a1 = Arrays.asList("1", "2", "3", "4");
		List<String> a2 = Arrays.asList("a21", "a22", "1", "2", "3");
		List<String> a3 = Arrays.asList("1", "2", "a31", "a32", "3","4", "a35", "a36");
		
		OrderedCollection oc = new OrderedCollection();
		oc.addOriginal(a1);
		oc.add(a2);
		System.out.println(oc.get());
		
		oc.add(a3);
		System.out.println(oc.get());
	}
}
