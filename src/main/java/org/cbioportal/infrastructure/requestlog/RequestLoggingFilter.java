package org.cbioportal.infrastructure.requestlog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
 * ClickHouse. The request body is read through a {@link ContentCachingRequestWrapper} so that
 * capturing it does not interfere with the controller's own read of the body.
 *
 * <p>Capturing happens <em>after</em> the request has been handled, so the body has been fully read
 * (and therefore cached) and the matched endpoint pattern and response status are available.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String REDACTED = "REDACTED";

  private final RequestLogService requestLogService;
  private final RequestLoggingProperties properties;
  private final Set<String> redactHeaders;
  private final Set<String> redactParams;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public RequestLoggingFilter(
      RequestLogService requestLogService, RequestLoggingProperties properties) {
    this.requestLogService = requestLogService;
    this.properties = properties;
    this.redactHeaders = toLowerCaseSet(properties.getRedactHeaders());
    this.redactParams = toLowerCaseSet(properties.getRedactParams());
  }

  private static Set<String> toLowerCaseSet(List<String> values) {
    return values.stream()
        .map(value -> value.toLowerCase(Locale.ROOT))
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
    String contentType = request.getContentType();

    Charset charset =
        Optional.ofNullable(request.getCharacterEncoding())
            .map(Charset::forName)
            .orElse(StandardCharsets.UTF_8);
    byte[] bodyBytes = request.getContentAsByteArray();
    // The cache filled to the cap. The body was truncated unless the declared content length proves
    // it fit exactly; an unknown length (-1, e.g. chunked transfer) is treated as possibly
    // truncated.
    long contentLength = request.getContentLengthLong();
    boolean truncated =
        bodyBytes.length >= properties.getMaxBodyBytes()
            && (contentLength < 0 || contentLength > bodyBytes.length);

    // Redact before hashing/storing so secrets never reach the database and so requests that differ
    // only in a secret value still deduplicate to the same document.
    String queryString = redactQuery(request.getQueryString());
    String body = redactBody(new String(bodyBytes, charset), contentType);

    String endpoint = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

    LoggedRequest logged = new LoggedRequest();
    logged.setId(computeId(method, path, queryString, body));
    logged.setMethod(method);
    logged.setPath(path);
    logged.setEndpoint(endpoint != null ? endpoint : path);
    logged.setQueryString(queryString);
    logged.setServerName(request.getServerName());
    logged.setUrl(reconstructUrl(request, queryString));
    logged.setHeaders(extractHeaders(request));
    logged.setContentType(contentType);
    logged.setBody(body);
    logged.setBodyTruncated(truncated);
    logged.setResponseStatus(response.getStatus());
    logged.setSeen(Instant.now());
    return logged;
  }

  private List<HttpHeader> extractHeaders(HttpServletRequest request) {
    List<HttpHeader> headers = new ArrayList<>();
    Enumeration<String> names = request.getHeaderNames();
    if (names == null) {
      return headers;
    }
    for (String name : Collections.list(names)) {
      String value =
          redactHeaders.contains(name.toLowerCase(Locale.ROOT))
              ? REDACTED
              : String.join(", ", Collections.list(request.getHeaders(name)));
      headers.add(new HttpHeader(name, value));
    }
    return headers;
  }

  private String reconstructUrl(HttpServletRequest request, String queryString) {
    StringBuffer url = request.getRequestURL();
    if (queryString != null && !queryString.isEmpty()) {
      url.append('?').append(queryString);
    }
    return url.toString();
  }

  /** Replace the values of configured parameter names in a {@code k=v&k2=v2} string. */
  private String redactQuery(String query) {
    if (query == null || query.isEmpty() || redactParams.isEmpty()) {
      return query;
    }
    StringBuilder out = new StringBuilder();
    String[] pairs = query.split("&");
    for (int i = 0; i < pairs.length; i++) {
      if (i > 0) {
        out.append('&');
      }
      String pair = pairs[i];
      int eq = pair.indexOf('=');
      String key = eq >= 0 ? pair.substring(0, eq) : pair;
      if (eq >= 0 && redactParams.contains(key.toLowerCase(Locale.ROOT))) {
        out.append(key).append('=').append(REDACTED);
      } else {
        out.append(pair);
      }
    }
    return out.toString();
  }

  private String redactBody(String body, String contentType) {
    if (body == null || body.isEmpty() || redactParams.isEmpty()) {
      return body;
    }
    String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    if (type.contains("application/x-www-form-urlencoded")) {
      return redactQuery(body);
    }
    if (type.contains("json")) {
      return redactJson(body);
    }
    return body;
  }

  private String redactJson(String body) {
    try {
      JsonNode root = OBJECT_MAPPER.readTree(body);
      redactJsonNode(root);
      return OBJECT_MAPPER.writeValueAsString(root);
    } catch (IOException ex) {
      // Not parseable as JSON; leave it untouched rather than risk mangling it.
      return body;
    }
  }

  private void redactJsonNode(JsonNode node) {
    if (node instanceof ObjectNode object) {
      List<String> fieldNames = new ArrayList<>();
      object.fieldNames().forEachRemaining(fieldNames::add);
      for (String field : fieldNames) {
        if (redactParams.contains(field.toLowerCase(Locale.ROOT))) {
          object.put(field, REDACTED);
        } else {
          redactJsonNode(object.get(field));
        }
      }
    } else if (node instanceof ArrayNode array) {
      array.forEach(this::redactJsonNode);
    }
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
