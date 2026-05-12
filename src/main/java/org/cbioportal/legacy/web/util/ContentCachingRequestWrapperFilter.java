package org.cbioportal.legacy.web.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Filter that wraps the request in a re-readable body wrapper to allow multiple reads of the
 * request body. For controllers that receive study view data filters, the first read is done by
 * {@link InvolvedCancerStudyExtractorInterceptor} security interceptor to extract involved study
 * ids to apply authorization, and the second read will be done by the actual controller.
 *
 * <p>Example of use: For controllers that receive study view data filters, the first read is
 * performed by the {@link InvolvedCancerStudyExtractorInterceptor} security interceptor. This
 * interceptor extracts the involved study IDs and set them as request attribute that is necessary
 * for the following authorization annotation on the controller level.
 * {@code @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>',
 * T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")} The second read is then executed by
 * the actual controller to process the data.
 *
 * @author ochoaa
 * @author Ruslan Forostianov
 */
@Component
public class ContentCachingRequestWrapperFilter implements Filter {
  private final Logger LOG = LoggerFactory.getLogger(ContentCachingRequestWrapperFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    CachedBodyRequestWrapper wrappedRequest =
        new CachedBodyRequestWrapper((HttpServletRequest) request);
    LOG.trace("Wrapping request for multiple reads of request body");
    filterChain.doFilter(wrappedRequest, response);
  }

  /**
   * Request wrapper that eagerly reads and caches the entire request body, then replays it on every
   * {@link #getInputStream()} call. This ensures that both the {@link
   * InvolvedCancerStudyExtractorInterceptor} (which consumes the body to extract study IDs) and the
   * downstream controller (which uses {@code @RequestBody}) can each read the body independently.
   */
  static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
      super(request);
      this.cachedBody = request.getInputStream().readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
      return new ServletInputStream() {
        @Override
        public boolean isFinished() {
          return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
          throw new UnsupportedOperationException("Async reading is not supported");
        }

        @Override
        public int read() throws IOException {
          return byteArrayInputStream.read();
        }
      };
    }

    @Override
    public BufferedReader getReader() throws IOException {
      String encoding = getCharacterEncoding();
      Charset charset = (encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
      return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
  }
}
