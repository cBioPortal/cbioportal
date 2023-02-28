package org.mskcc.cbio.portal.util;

import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("requestBodyGZipFilter")
public class RequestBodyGZipFilter implements Filter {
    /**
     * We need to limit the size of the gzipped request bodies to avoid denial of memory attacks.
     * Because gzip can achieve text compression rates of around 1000x, without a common sense limit for
     * request body size, it would be very easy to send a request body that, when inflated, consumes the
     * entirety of a server's memory.
     */
    @Value("${request_gzip_body_size_bytes:80000000}")
    private int maxInflatedRequestBodySize;
    
    @Value("${enable_request_body_gzip_compression:false}")
    private boolean enabled;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to init (Sonar asks you to comment if you leave a method empty)
    }

    /**
     * Analyzes servlet request for possible gzipped body.
     * When Content-Encoding header has "gzip" value and request method is POST, we read all the
     * gzipped stream and if it has any data unzip it. In case when gzip Content-Encoding header
     * specified but body is not actually in gzip format we will throw ZipException.
     *
     * @param servletRequest  servlet request
     * @param servletResponse servlet response
     * @param chain           filter chain
     * @throws IOException      throws when fails
     * @throws ServletException thrown when fails
     */
    @Override
    public final void doFilter(
        final ServletRequest servletRequest,
        final ServletResponse servletResponse,
        final FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        boolean isGzipped = request.getHeader(HttpHeaders.CONTENT_ENCODING) != null
            && request.getHeader(HttpHeaders.CONTENT_ENCODING).contains("gzip");
        boolean requestTypeSupported = "POST".equals(request.getMethod());

        if (isGzipped) {
            if (!enabled) {
                throw new IllegalStateException(
                    "Received gzipped request body, but enable_request_body_gzip_compression is not set or is false"
                );
            }
            if (!requestTypeSupported) {
                throw new IllegalStateException(
                    "Received request with a Content-Encoding: gzip header and a request method of: " +
                    request.getMethod()
                    + " Only POST requests are supported."
                );
            }

            request = new GzippedInputStreamRequestWrapper((HttpServletRequest) servletRequest, maxInflatedRequestBodySize);
        }

        chain.doFilter(request, response);
    }
    
    @Override
    public final void destroy() {
        // Nothing to destroy (Sonar asks you to comment if you leave a method empty)
    }
}