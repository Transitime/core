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
package org.transitime.xssfilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringEscapeUtils;

public class XSSFilter implements Filter {

	@Override
	public void init(FilterConfig arg0) throws ServletException {		
	}
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		ServletRequest wrappedRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {
			
			@Override
			public String getParameter(String param) {
				String str = super.getParameter(param);
				return StringEscapeUtils.escapeHtml4(str);
			}
						
			@Override
			public Map<String, String[]> getParameterMap() {
				Map<String, String[]> orig = super.getParameterMap();
				
				Map<String, String[]> map = new HashMap<String, String[]>();
				
				for (Entry<String, String[]> entry : orig.entrySet()) {
					String[] src = entry.getValue();
					String value[] = new String[src.length];
					for (int i = 0; i < src.length; i++)
						value[i] = StringEscapeUtils.escapeHtml4(src[i]);
					
					map.put(entry.getKey(), value);
				}
				
				return map; 
			}
			
		};

		chain.doFilter(wrappedRequest, response);
	}

}
