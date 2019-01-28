package org.cbioportal.web.util;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


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
        ResettableHttpServletRequestWrapper wrappedRequest = new ResettableHttpServletRequestWrapper((HttpServletRequest) aRequest);
        aRequest = wrappedRequest;
        aChain.doFilter(aRequest, aResponse);
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
