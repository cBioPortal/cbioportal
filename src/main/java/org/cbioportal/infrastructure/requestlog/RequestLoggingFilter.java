package org.cbioportal.infrastructure.requestlog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Captures every matching HTTP request and hands it to {@link RequestLogService} to be stored in
 * MongoDB. The request body is read through a {@link ContentCachingRequestWrapper} so that
 * capturing it does not interfere with the controller's own read of the body.
 *
 * <p>Capturing happens <em>after</em> the request has been handled, so the body has been fully read
 * (and therefore cached) and the matched endpoint pattern and response status are available.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);

  private final RequestLogService requestLogService;
  private final RequestLoggingProperties properties;
  private final Set<String> redactHeaders;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public RequestLoggingFilter(
      RequestLogService requestLogService, RequestLoggingProperties properties) {
    this.requestLogService = requestLogService;
    this.properties = properties;
    this.redactHeaders =
        properties.getRedactHeaders().stream()
            .map(header -> header.toLowerCase(java.util.Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return properties.getPathPatterns().stream()
        .noneMatch(pattern -> pathMatcher.match(pattern, path));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    ContentCachingRequestWrapper wrapped =
        request instanceof ContentCachingRequestWrapper ccrw
            ? ccrw
            : new ContentCachingRequestWrapper(request, properties.getMaxBodyBytes());
    try {
      filterChain.doFilter(wrapped, response);
    } finally {
      try {
        requestLogService.save(capture(wrapped, response));
      } catch (RuntimeException ex) {
        // Capturing must never break the response.
        LOG.debug("Failed to capture request for logging", ex);
      }
    }
  }

  private LoggedRequest capture(
      ContentCachingRequestWrapper request, HttpServletResponse response) {
    String method = request.getMethod();
    String path = request.getRequestURI();
    String queryString = request.getQueryString();
    String url = reconstructUrl(request);
    Map<String, String> headers = extractHeaders(request);

    Charset charset =
        request.getCharacterEncoding() != null
            ? Charset.forName(request.getCharacterEncoding())
            : StandardCharsets.UTF_8;
    byte[] bodyBytes = request.getContentAsByteArray();
    String body = new String(bodyBytes, charset);
    boolean truncated =
        bodyBytes.length >= properties.getMaxBodyBytes()
            && request.getContentLengthLong() > bodyBytes.length;

    String endpoint = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

    LoggedRequest logged = new LoggedRequest();
    logged.setId(computeId(method, path, queryString, body));
    logged.setMethod(method);
    logged.setPath(path);
    logged.setEndpoint(endpoint != null ? endpoint : path);
    logged.setQueryString(queryString);
    logged.setUrl(url);
    logged.setHeaders(headers);
    logged.setContentType(request.getContentType());
    logged.setBody(body);
    logged.setBodyTruncated(truncated);
    logged.setResponseStatus(response.getStatus());
    Instant now = Instant.now();
    logged.setFirstSeen(now);
    logged.setLastSeen(now);
    logged.setCount(1);
    return logged;
  }

  private Map<String, String> extractHeaders(HttpServletRequest request) {
    Map<String, String> headers = new LinkedHashMap<>();
    Enumeration<String> names = request.getHeaderNames();
    if (names == null) {
      return headers;
    }
    for (String name : Collections.list(names)) {
      String value =
          redactHeaders.contains(name.toLowerCase(java.util.Locale.ROOT))
              ? "REDACTED"
              : String.join(", ", Collections.list(request.getHeaders(name)));
      headers.put(name, value);
    }
    return headers;
  }

  private String reconstructUrl(HttpServletRequest request) {
    StringBuffer url = request.getRequestURL();
    if (request.getQueryString() != null) {
      url.append('?').append(request.getQueryString());
    }
    return url.toString();
  }

  /**
   * Deterministic id so the same logical request always maps to the same document. Hashing keeps
   * the id short and bounded even for very large bodies.
   */
  private String computeId(String method, String path, String queryString, String body) {
    String canonical =
        method + '\n' + path + '\n' + (queryString == null ? "" : queryString) + '\n' + body;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      // SHA-256 is always available on a standard JVM.
      throw new IllegalStateException(ex);
    }
  }
}
