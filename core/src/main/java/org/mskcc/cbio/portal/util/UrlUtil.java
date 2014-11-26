/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.servlet.QueryBuilder;
import org.mskcc.cbio.portal.servlet.ServletXssUtil;
import org.owasp.validator.html.PolicyException;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Enumeration;

/**
 * URL Utility Class.
 */
public class UrlUtil {

    /**
     * Gets Current URL.
     * 
     * @param request HttpServletRequest.
     * @return Current URL
     */
    public static String getCurrentUrl (HttpServletRequest request) {
        Enumeration paramEnum = request.getParameterNames();
        StringBuffer buf = new StringBuffer(request.getAttribute
                (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?");
        while (paramEnum.hasMoreElements()) {
            String paramName = (String) paramEnum.nextElement();
            String values[] = request.getParameterValues(paramName);
            if (values != null && values.length >0) {
                for (int i=0; i<values.length; i++) {
                    String currentValue = values[i];
                    if (paramName.equals(QueryBuilder.GENE_LIST)
                            || paramName.equals(QueryBuilder.CASE_IDS)
                        && currentValue != null) {
                        currentValue = URLEncoder.encode(currentValue);
                    }
                    buf.append (paramName + "=" + currentValue + "&");
                }
            }
        }
        return buf.toString();
    }
    
    /**
     * Gets current URL by replacing the case_ids parameter by
     * the case_ids_key parameter.
     * 
     * @param request	HttpServletRequest.
     * @return 			current URL with case_ids replaced by case_ids_key
     * @throws DaoException 
     */
    public static String getUrlWithCaseIdsKey(HttpServletRequest request)
    		throws DaoException
    {
        Enumeration paramEnum = request.getParameterNames();
        StringBuffer buf = new StringBuffer(request.getAttribute
                (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?");
	    ServletXssUtil xssUtil = null;

	    try {
		    xssUtil = ServletXssUtil.getInstance();
	    } catch (PolicyException e) {
		    //logger.error("Could not instantiate XSS Util:  " + e.toString());
	    }

        while (paramEnum.hasMoreElements())
        {
            String paramName = (String) paramEnum.nextElement();
            String values[] = request.getParameterValues(paramName);
            
            if (values != null && values.length >0)
            {
                for (int i=0; i<values.length; i++)
                {
                    String currentValue = values[i];

                    if (paramName.equals(QueryBuilder.GENE_LIST)
                        && currentValue != null)
                    {
                        currentValue = URLEncoder.encode(currentValue);
                    }
                    else if (paramName.equals(QueryBuilder.CASE_IDS)
                    		&& currentValue != null)
                    {
                    	paramName = QueryBuilder.CASE_IDS_KEY;
                    	
                    	// first try to get case_ids_key attribute from the request
                    	if (request.getAttribute(QueryBuilder.CASE_IDS_KEY) != null)
                    	{
                    		currentValue = (String)request.getAttribute(QueryBuilder.CASE_IDS_KEY);
                    	}
                    	// if no request attribute found, then use the utility function
                    	else
                    	{
                    		currentValue = PatientSetUtil.shortenPatientIds(currentValue);
                    	}
                    }

	                // TODO remove and test...
	                if (xssUtil != null)
	                {
		                currentValue = xssUtil.getCleanerInput(currentValue);
	                }

                    buf.append(paramName + "=" + currentValue + "&");
                }
            }
        }
        
        return buf.toString();
    }
}
