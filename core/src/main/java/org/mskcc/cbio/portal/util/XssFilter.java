/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.portal.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Filter that wraps an underlying request. This is intended to filter
 * all input request
 *
 * <P>This filter should be configured only for those operations that use a
 * file upload request.
 */
public class XssFilter <W extends HttpServletRequestWrapper> implements Filter
{
	public void init(FilterConfig aConfig) throws ServletException
	{
		//do nothing
	}

	public void destroy() {
		//do nothing
	}

	public void doFilter(ServletRequest aRequest,
		ServletResponse aResponse,
		FilterChain aChain)
			throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) aRequest;
		XssRequestWrapper wrapper = new XssRequestWrapper(request);
		aChain.doFilter(wrapper, aResponse);
	}

}
