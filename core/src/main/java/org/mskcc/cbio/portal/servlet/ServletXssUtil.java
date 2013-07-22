/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.servlet;

import org.owasp.validator.html.Policy;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.owasp.validator.html.CleanResults;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * Servlet Cross Site Scripting Util Class.
 */
public class ServletXssUtil {
    private static ServletXssUtil servletUtil;
    private Policy policy;
    private AntiSamy as;

    /**
     * Private Constructor.
     * 
     * @throws PolicyException Policy Error.
     */
    private ServletXssUtil() throws PolicyException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("/antisamy.xml");
        policy = Policy.getInstance(inputStream);
        as = new AntiSamy();
    }

    /**
     * Gets Global Singleton.
     * @return Global Singleton of Servlet Util.
     * @throws PolicyException Policy Exception.
     */
    public static ServletXssUtil getInstance() throws PolicyException {
        if (servletUtil == null) {
            servletUtil = new ServletXssUtil();
        }
        return servletUtil;
    }

    /**
     * Gets Clean XSS User Input.
     *
     * @param httpServletRequest    Http Servlet Request.
     * @param parameter             Parameter.
     * @return                      Clean Input, minus XSS Scripting Attack Problems.
     * @throws ScanException        Scan Error.
     * @throws PolicyException      Policy Error.
     */
    public String getCleanInput (HttpServletRequest httpServletRequest, String parameter) {
        String dirtyInput = httpServletRequest.getParameter(parameter);
        try {
            if (dirtyInput != null) {
                CleanResults cr = as.scan(dirtyInput, policy);
                return cr.getCleanHTML();
            } else {
                return null;
            }
        } catch (ScanException e) {
            return null;
        } catch (PolicyException e) {
            return null;
        }
    }

    /**
     * Gets Clean XSS User Input.
     *
     * @param dirty Dirty User Input.
     * @return Clean User Input.
     */
    public String getCleanInput (String dirty) {
        try {
            CleanResults cr = as.scan(dirty, policy);
            return cr.getCleanHTML();
        } catch (ScanException e) {
            return null;
        } catch (PolicyException e) {
            return null;
        }
    }
}
