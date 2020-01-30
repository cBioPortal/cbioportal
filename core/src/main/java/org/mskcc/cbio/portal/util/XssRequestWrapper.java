/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.servlet.ServletXssUtil;
import org.owasp.validator.html.PolicyException;

/**
 * Wrapper designed for all requests.
 *
 * <P>This class uses the Apache Commons
 * <a href='http://commons.apache.org/fileupload/'>File Upload tool</a>.
 * The generous Apache License will very likely allow you to use it in your
 * applications as well.
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {
    private static Log LOG = LogFactory.getLog(XssRequestWrapper.class);
    protected ServletXssUtil xssUtil;

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            xssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            // TODO log?
            xssUtil = null;
        }
    }

    public String getParameter(String name) {
        String parameter = super.getParameter(name);

        LOG.debug(
            "XssRequestWrapper.getParameter(" + name + "): '" + parameter + "'"
        );
        String clean = xssUtil.getCleanerInput(parameter);
        LOG.debug(
            "XssRequestWrapper.getParameter(" +
            name +
            "): cleaned = '" +
            clean +
            "'"
        );

        return clean;
    }

    public String getRawParameter(String name) {
        String raw = super.getParameter(name);
        LOG.debug(
            "XssRequestWrapper.getRawParameter(" +
            name +
            "): cleaned = '" +
            raw +
            "'"
        );
        return raw;
    }
    // TODO also overwrite getParameterValues & getAttribute method?
}
