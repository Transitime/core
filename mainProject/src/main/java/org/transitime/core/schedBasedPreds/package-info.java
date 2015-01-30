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
 * The schedule based predictions module runs in the background. Every few
 * minutes it looks for blocks that do not have an associated vehicle. For
 * these blocks the module creates a fake AVL report at the location
 * of the beginning of the block. This in turn creates a schedule based 
 * vehicle and generates predictions for the entire 
 * block that are based on the scheduled departure time. The purpose of this
 * module is to generate predictions well in advance even if vehicles are 
 * assigned just a few minutes before a vehicle is scheduled to start a
 * block. This feature should of course only be used if most of the time the 
 * blocks are actually run. It should not be used for agencies such as
 * SFMTA where blocks/trips are often missed because would then be
 * often providing predictions when no vehicle will arrive.
 * <p>
 * Of course the User Interface should distinguish between predictions
 * that are based on AVL data (which are more reliable) and those
 * based on the schedule.
 *
 * @author SkiBu Smith
 *
 */
package org.transitime.core.schedBasedPreds;