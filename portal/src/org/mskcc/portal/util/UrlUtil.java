package org.mskcc.portal.util;

import org.mskcc.portal.servlet.QueryBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Enumeration;

/**
 * URL Utility Class.
 */
public class UrlUtil {

    /**
     * Gets Current URL.
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
}
