package org.cbioportal.legacy.web.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;


/**
 * Filter that wraps the request in a {@link ContentCachingRequestWrapper} to allow multiple reads of the request body.
 * For controllers that receive study view data filters, the first read is done by {@link InvolvedCancerStudyExtractorInterceptor}
 * security interceptor to extract involved study ids to apply autorization, and the second read will be done by the actual controller.
 * 
 * Example of use: For controllers that receive study view data filters,
 * the first read is performed by the {@link InvolvedCancerStudyExtractorInterceptor} security interceptor.
 * This interceptor extracts the involved study IDs and set them as request attribute that is necessary for the following authorization annotation on the controller level.
 * {@code @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")}
 * The second read is then executed by the actual controller to process the data.
 *
 * @author ochoaa
 * @author Ruslan Forostianov
 */
@Component
public class ContentCachingRequestWrapperFilter implements Filter {
    private final Logger LOG = LoggerFactory.getLogger(ContentCachingRequestWrapperFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        LOG.trace("Wrapping request for multiple reads of request body");
        filterChain.doFilter(wrappedRequest, response);
    }

}
