package org.cbioportal.web.util;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;


/**
 *
 * @author ochoaa
 * 
 */
@Component
public class ResettableHttpServletRequestFilter implements Filter {
    private Logger LOG = LoggerFactory.getLogger(ResettableHttpServletRequestFilter.class);

    @Override
    public void init(FilterConfig aChain) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest
            && ((HttpServletRequest)request).getRequestURI() != null
            && ((HttpServletRequest)request).getRequestURI().startsWith("/export/")) {
            filterChain.doFilter(request, response); // Skip content caching for export requests as we want to stream the response
        } else {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
