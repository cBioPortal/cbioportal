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
