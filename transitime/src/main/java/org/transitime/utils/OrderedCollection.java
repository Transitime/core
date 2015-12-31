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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Takes several ordered lists of String objects and combines them into a single
 * one. Useful for determining things such as a stop list for a direction when
 * there are multiple trip patterns.
 *
 * @author SkiBu Smith
 *
 */
public class OrderedCollection {

	private final List<String> list = new LinkedList<String>();
	
	/********************** Member Functions **************************/

	/**
	 * Quick way of adding the first list.
	 * 
	 * @param originalList
	 */
	public void addOriginal(Collection<String> originalList) {
		list.addAll(originalList);
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
		}
	}
	
	/**
	 * Finds where item already exists in list. It starts looking at the
	 * startIndex so that can handle situations such as a stop being in a trip
	 * more than once. This can happen for routes that loop back on themselves,
	 * such as a route that is a loop or that contains a loop.
	 * 
	 * @param startIndex
	 *            Where to start looking
	 * @param item
	 *            The String to look for
	 * @return The index into this OrderedCollection where the item was found,
	 *         or -1 if item not in list.
	 */
	private int indexWhereItemExists(int startIndex, String item) {
		for (int i=startIndex; i<list.size(); ++i) {
			if (list.get(i).equals(item))
				return i;
		}
		return -1;
	}
	
	/**
	 * Adds items that are not already in the list to the list. Items are
	 * carefully added at proper place in list. New items are either added
	 * at beginning, middle, or end of list, depending on other items that
	 * match to the original list.
	 * 
	 * @param newList
	 *            items to be added to the ordered list
	 */
	private void add(Collection<String> newList) {
		// Groups of items in a row that haven't yet been added to list.
		// They are grouped so that can find the right place to insert them.
		List<String> itemsToAdd = new ArrayList<String>();
		
		// For each of the new items...
		int insertionPoint = 0;
		for (String item : newList) {
			// If item already in the list then don't need to add it
			int indexWhereItemExists = 
					indexWhereItemExists(insertionPoint, item);
			if (indexWhereItemExists >= 0) {
				// If there are items to add, then add them now
				if (!itemsToAdd.isEmpty()) {
					// Add the items to the proper place
					add(insertionPoint, itemsToAdd);
					
					// Determine the new insertion point
					insertionPoint = insertionPoint + itemsToAdd.size();
					
					// Done inserting these new items so clear the list
					itemsToAdd.clear();
				} else {
					// There were no items to add so just update insertion point
					insertionPoint = indexWhereItemExists + 1;
				}
			} else {
				// Item not already in list so keep track of it so it can be 
				// added at appropriate place
				itemsToAdd.add(item);
			}
		}
		
		// If still have extra items from newList then append those to end
		add(insertionPoint, itemsToAdd);
	}
	
	// For sorting lists by their size descending
	private Comparator<List<String>> comparator = new Comparator<List<String>>() {
		@Override
		public int compare(List<String> l1, List<String> l2) {
			return l2.size() - l1.size();
		}
	};
	
	/**
	 * Adds the lists to the OrderedCollection. Need to add all lists at once so
	 * that this method can first sort them by length so that the end result is
	 * usually proper. Important for complicated routes like sfmta route 38
	 * outbound.
	 * 
	 * @param listOfLists
	 */
	public void add(List<List<String>> listOfLists) {
		Collections.sort(listOfLists, comparator);
		for (Collection<String> list : listOfLists) {
			add(list);
		}
	}
	
	/**
	 * Returns this OrderedCollection a regular List
	 * 
	 * @return This as a regular List
	 */
	public List<String> get() {
		return list;
	}
	
	@Override
	public String toString() {
		return "OrderedCollection [list=" + list + "]";
	}

	/**
	 * For testing.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		List<String> a1 = Arrays.asList("1", "2", "3", "4");
		List<String> a1x = Arrays.asList("1", "2", "3", "3.1", "3.2", "4");
		List<String> a1xx = Arrays.asList("3", "4", "1", "2", "3");
		List<String> a2 = Arrays.asList("a21", "a22", "1", "2", "3");
		List<String> a3 = Arrays.asList("1", "2", "a31", "a32", "3","4", "a35", "a36");
		
		OrderedCollection oc = new OrderedCollection();
		oc.addOriginal(a1);
		oc.add(a1x);
		System.out.println(oc.get());
		
		oc.add(a1xx);
		System.out.println(oc.get());
		
		oc.add(a2);
		System.out.println(oc.get());
		
		oc.add(a3);
		System.out.println(oc.get());
	}
}
