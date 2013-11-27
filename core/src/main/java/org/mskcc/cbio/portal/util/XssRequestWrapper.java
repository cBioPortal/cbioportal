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

import org.mskcc.cbio.portal.servlet.ServletXssUtil;
import org.owasp.validator.html.PolicyException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wrapper designed for all requests.
 *
 * <P>This class uses the Apache Commons
 * <a href='http://commons.apache.org/fileupload/'>File Upload tool</a>.
 * The generous Apache License will very likely allow you to use it in your
 * applications as well.
 */
public class XssRequestWrapper extends HttpServletRequestWrapper
{
	protected ServletXssUtil xssUtil;

	public XssRequestWrapper(HttpServletRequest request) {
		super(request);

		try
		{
			xssUtil = ServletXssUtil.getInstance();
		}
		catch (PolicyException e)
		{
			// TODO log?
			xssUtil = null;
		}
	}

	public String getParameter(String name)
	{
		String parameter = super.getParameter(name);

		String clean = xssUtil.getCleanerInput(parameter);

		return clean;
	}

	public String getRawParameter(String name)
	{
		return super.getParameter(name);
	}

	// TODO also overwrite getParameterValues & getAttribute method?
}
