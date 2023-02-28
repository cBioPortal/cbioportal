package org.cbioportal.web.util;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;


/**
 *
 * @author ochoaa
 */
public class ResettableHttpServletRequestFilter implements Filter {
    private Logger LOG = LoggerFactory.getLogger(ResettableHttpServletRequestFilter.class);

    @Override
    public void init(FilterConfig aChain) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain) throws IOException, ServletException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) aRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) aResponse);
        aChain.doFilter(wrappedRequest, wrappedResponse);
        wrappedResponse.copyBodyToResponse();
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
