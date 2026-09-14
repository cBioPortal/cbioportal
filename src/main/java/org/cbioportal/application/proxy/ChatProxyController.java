package org.cbioportal.application.proxy;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncWebRequest;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Reverse proxy to the chat-sidebar-server, which serves both the chat API and the sidebar's static
 * bundle.
 */
@RestController
@ConditionalOnProperty(name = "chat.sidebar.url")
public class ChatProxyController {

  private static final Logger log = LoggerFactory.getLogger(ChatProxyController.class);

  /**
   * Deliberately excludes Cookie and Authorization: the portal session must not reach the
   * unauthenticated upstream. Accept-Encoding is dropped too, so upstream responses are always
   * uncompressed and there is no Content-Encoding to reconcile. Note that the JDK's HttpClient
   * rejects connection/content-length/expect/host/upgrade outright, so an allow-list is required
   * rather than merely preferred.
   */
  private static final Set<String> FORWARDED_REQUEST_HEADERS = Set.of("accept", "content-type");

  /**
   * Content-Type carries the per-asset type Express already computed for the bundle, and
   * text/event-stream for the chat stream.
   */
  private static final Set<String> FORWARDED_RESPONSE_HEADERS =
      Set.of(
          "content-type",
          "cache-control",
          "etag",
          "last-modified",
          "expires",
          "x-vercel-ai-ui-message-stream",
          "x-accel-buffering");

  private static final int BUFFER_SIZE = 4096;

  private final String upstreamBaseUrl;
  private final long requestTimeoutMs;
  private final long streamTimeoutMs;
  private final HttpClient httpClient;

  public ChatProxyController(
      @Value("${chat.sidebar.url}") String upstreamBaseUrl,
      @Value("${chat.sidebar.connect_timeout_ms:5000}") long connectTimeoutMs,
      @Value("${chat.sidebar.request_timeout_ms:10000}") long requestTimeoutMs,
      @Value("${chat.sidebar.stream_timeout_ms:600000}") long streamTimeoutMs) {
    this.upstreamBaseUrl = upstreamBaseUrl.replaceFirst("/$", "");
    this.requestTimeoutMs = requestTimeoutMs;
    this.streamTimeoutMs = streamTimeoutMs;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(connectTimeoutMs))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
  }

  @PreAuthorize("@chatAccess.isAllowed(authentication)")
  @RequestMapping("/api/chat/**")
  public ResponseEntity<StreamingResponseBody> chatApi(
      HttpServletRequest request, @RequestBody(required = false) byte[] body)
      throws InterruptedException {
    // Calls that invoke the model arrive as POSTs and can run for minutes; GETs are cheap
    // metadata.
    long timeoutMs =
        "POST".equalsIgnoreCase(request.getMethod()) ? streamTimeoutMs : requestTimeoutMs;
    return proxy(request, body, timeoutMs);
  }

  /**
   * The bundle is gated on being logged in rather than on the allowlist, so a user without access
   * loads the sidebar and is told why instead of getting a blank frame.
   */
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
      value = "/chat-sidebar/**",
      method = {RequestMethod.GET, RequestMethod.HEAD})
  public ResponseEntity<StreamingResponseBody> chatSidebarAssets(HttpServletRequest request)
      throws InterruptedException {
    return proxy(request, null, requestTimeoutMs);
  }

  private ResponseEntity<StreamingResponseBody> proxy(
      HttpServletRequest servletRequest, byte[] body, long timeoutMs) throws InterruptedException {

    // Async processing hasn't started yet -- StreamingResponseBody starts it only after this
    // method returns -- so the timeout can still be set, and setting it here affects this request
    // alone rather than the global default that the study export feature configures.
    AsyncWebRequest asyncWebRequest =
        WebAsyncUtils.getAsyncManager(servletRequest).getAsyncWebRequest();
    if (asyncWebRequest != null) {
      asyncWebRequest.setTimeout(timeoutMs);
    }

    String path = servletRequest.getRequestURI();
    String contextPath = servletRequest.getContextPath();
    if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
      path = path.substring(contextPath.length());
    }
    if (path.contains("..")) {
      return ResponseEntity.badRequest().build();
    }
    String query = servletRequest.getQueryString();

    URI upstreamUri;
    try {
      // Concatenated onto a base that already fixes the scheme and authority. Resolving the
      // client-supplied remainder instead would let a leading "//host" segment retarget the
      // request at an arbitrary internal host.
      upstreamUri = new URI(upstreamBaseUrl + path + (query == null ? "" : "?" + query));
    } catch (URISyntaxException e) {
      return ResponseEntity.badRequest().build();
    }

    HttpRequest.Builder builder =
        HttpRequest.newBuilder(upstreamUri).timeout(Duration.ofMillis(timeoutMs));
    Collections.list(servletRequest.getHeaderNames()).stream()
        .filter(name -> FORWARDED_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT)))
        .forEach(name -> builder.header(name, servletRequest.getHeader(name)));
    if (servletRequest.getUserPrincipal() != null) {
      builder.header("X-Chat-User", servletRequest.getUserPrincipal().getName());
    }
    builder.method(
        servletRequest.getMethod(),
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofByteArray(body));

    HttpResponse<InputStream> upstream;
    try {
      // Returns once the status line and headers have arrived, leaving the body to be read
      // lazily. That ordering is required: StreamingResponseBody commits the status and headers
      // when this method returns, so they have to be known by then.
      upstream = httpClient.send(builder.build(), BodyHandlers.ofInputStream());
    } catch (ConnectException | HttpTimeoutException e) {
      log.warn("chat-sidebar-server unreachable: {}", e.toString());
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    } catch (IOException e) {
      log.warn("chat-sidebar-server request failed: {}", e.toString());
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    HttpHeaders headers = new HttpHeaders();
    upstream
        .headers()
        .map()
        .forEach(
            (name, values) -> {
              if (FORWARDED_RESPONSE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                headers.addAll(name, values);
              }
            });

    return ResponseEntity.status(upstream.statusCode())
        .headers(headers)
        .body(out -> streamBody(upstream, out));
  }

  private void streamBody(HttpResponse<InputStream> upstream, OutputStream out) {
    try (InputStream in = upstream.body()) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
        out.flush();
      }
    } catch (IOException e) {
      log.debug("chat proxy stream closed before completion: {}", e.toString());
    }
  }
}
