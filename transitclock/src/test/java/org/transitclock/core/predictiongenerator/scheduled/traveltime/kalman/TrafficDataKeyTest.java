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
package org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman;

import org.junit.Test;
import org.transitclock.utils.Time;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

public class TrafficDataKeyTest {

  @Test
  public void shave() throws ParseException {
    long time = parseStr("2021-01-02 12:34:56.789");
    TrafficDataKey key = new TrafficDataKey(null, time);
    assertEquals(parseStr("2021-01-02 12:34:00.000"), key.getTime());

  }

  private Long parseStr(String s) throws ParseException {
    Date parse = Time.parse(s);
    return parse.getTime();
  }
}