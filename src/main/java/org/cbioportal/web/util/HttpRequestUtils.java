package org.cbioportal.web.util;

import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestUtils {

    public JSONObject getPostData(HttpServletRequest request) {
        // To support posted query data (when data would exceed URL length),
        // write all post params to json on page where it can be consumed.
        JSONObject paramsJson = new JSONObject();
        if (request.getMethod().equals("POST")) {
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                paramsJson.put(parameterName, request.getParameter(parameterName));
            }
        }
        return paramsJson;
    }
    
    public String getBaseUrl(HttpServletRequest request) {
        String currentUrl = request.getRequestURL().toString();
        String contextPath = request.getContextPath();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + contextPath;
        return baseURL.replaceAll("https?://", "");
    }
    
}
