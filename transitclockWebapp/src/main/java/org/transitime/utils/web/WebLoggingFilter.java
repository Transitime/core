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

package org.transitime.utils.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebLoggingFilter implements Filter {
  
  private static final Logger logger = LoggerFactory
      .getLogger(WebLoggingFilter.class);
  @Override
  public void init(FilterConfig filterConfg) {
    logger.info("WebLoggingFilter init");
  }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
    try {
      filterChain.doFilter(request, response);
    } catch (Throwable ex) {
      logger.error("Filter caught exception: ", ex);
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public void destroy() {
    logger.info("WebLoggingFilter destroy");
  }
}