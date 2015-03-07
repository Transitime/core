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

/**
 * Contains the classes for running a module in a separate thread in order
 * to do processing, such as for an AVL feed that polls for the data.
 * <p>
 * For the core predictor application modules can be started simply 
 * by configuring on the command line
 * which ones should be run by using the VM argument 
 * -Dtransitime.modules.optionalModulesList. Multiple modules can be specified
 * by separating them with a semicolon. For example, to use one module to
 * read AVL data and put it into JMS, and another module for reading the
 * AVL data from JMS and processing it could use something like:
 * -Dtransitime.modules.optionalModulesList=org.transitime.avl.MbtaNextBusAvlModule;org.transitime.avl.AvlJmsClientModule
 * 
 * @author SkiBu Smith
 *
 */
package org.transitime.modules;