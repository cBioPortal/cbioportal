package org.cbioportal.web;

import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

public class ExecuterTimeInterceptor implements WebRequestInterceptor {
    
    @Override
    public void postHandle(WebRequest webRequest, ModelMap modelMap) {
        //unimplemented
    }

    @Override
    public void afterCompletion(WebRequest webRequest, Exception e) {
        //unimplemented
    }

    @Override
    public void preHandle(WebRequest webRequest) {
        
            long startTime = System.currentTimeMillis();
            webRequest.setAttribute("startTime", startTime, 0);
        
    }
    
}