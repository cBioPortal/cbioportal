package org.mskcc.cbio.portal.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidParameterException;

@Component("urlParamFilter")
public class UrlParamFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to init (Sonar asks you to comment if you leave a method empty)
    }
    
    @Override
    public final void doFilter(
        final ServletRequest servletRequest,
        final ServletResponse servletResponse,
        final FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (request.getParameter("configUrl") != null && !request.getParameter("configUrl").isEmpty()) {
            throw new InvalidParameterException();
        } else {
            chain.doFilter(request, response);
        }
    }
    
    @Override
    public final void destroy() {
        // Nothing to destroy (Sonar asks you to comment if you leave a method empty)
    }
}