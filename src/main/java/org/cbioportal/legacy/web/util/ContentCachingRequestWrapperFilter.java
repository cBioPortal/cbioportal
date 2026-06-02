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
import java.util.Locale;
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
    if (!(request instanceof HttpServletRequest httpRequest) || !shouldWrapRequest(httpRequest)) {
      filterChain.doFilter(request, response);
      return;
    }

    CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(httpRequest);
    LOG.trace("Wrapping JSON POST request for multiple reads of request body");
    filterChain.doFilter(wrappedRequest, response);
  }

  private boolean shouldWrapRequest(HttpServletRequest request) {
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return false;
    }

    String contentType = request.getContentType();
    if (contentType == null) {
      return false;
    }

    String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
    return normalizedContentType.startsWith("application/json")
        || normalizedContentType.contains("+json");
  }

  /**
   * Request wrapper that lazily reads and caches the request body on first access, then replays it on every
   * {@link #getInputStream()} call. This ensures that both the {@link
   * InvolvedCancerStudyExtractorInterceptor} (which consumes the body to extract study IDs) and the
   * downstream controller (which uses {@code @RequestBody}) can each read the body independently.
   */
  static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
    private byte[] cachedBody;

    CachedBodyRequestWrapper(HttpServletRequest request) {
      super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(getCachedBody());
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

    private synchronized byte[] getCachedBody() throws IOException {
      if (this.cachedBody == null) {
        this.cachedBody = super.getInputStream().readAllBytes();
      }
      return this.cachedBody;
    }

    @Override
    public BufferedReader getReader() throws IOException {
      String encoding = getCharacterEncoding();
      Charset charset = (encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
      return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
  }
}
