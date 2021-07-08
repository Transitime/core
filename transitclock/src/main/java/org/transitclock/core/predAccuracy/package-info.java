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
 * For measuring prediction accuracy, both from external prediction feed and
 * for using internal predictions.
 * <p>
 * The prediction accuracy system is a Module that runs in a separate thread
 * and polls a sampling of predictions every once in a while (every few minutes).
 * The predictions are stored in memory. Can look at both internally generated
 * predictions and ones from an external API feed. When the core system generates
 * an arrival/departure then looks at predictions stored in memory to see if 
 * find corresponding one. If so then the corresponding prediction accuracy
 * for that prediction is stored in the database. Then queries into the database
 * can be made to determine prediction accuracy based on time, routes, etc.
 *
 * @author SkiBu Smith
 *
 */
package org.transitclock.core.predAccuracy;