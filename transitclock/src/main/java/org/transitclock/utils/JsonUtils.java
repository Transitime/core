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
package org.transitclock.utils;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utilities for JSON parsing.
 */
public class JsonUtils {

  /**
   * Converts the input stream into a JSON string. Useful for when processing
   * a JSON feed.
   *
   * @param in
   * @return the JSON string
   * @throws IOException
   * @throws JSONException
   */
  public static  String getJsonString(InputStream in) throws IOException,
          JSONException {
    BufferedReader streamReader =
            new BufferedReader(new InputStreamReader(in, "UTF-8"));
    StringBuilder responseStrBuilder = new StringBuilder();

    String inputStr;
    while ((inputStr = streamReader.readLine()) != null)
      responseStrBuilder.append(inputStr);

    String responseStr = responseStrBuilder.toString();
    return responseStr;
  }

}
