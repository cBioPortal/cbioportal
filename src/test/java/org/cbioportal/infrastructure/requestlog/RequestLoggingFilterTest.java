package org.cbioportal.infrastructure.requestlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerMapping;

class RequestLoggingFilterTest {

  private RequestLogService service;
  private RequestLoggingFilter filter;

  /** A chain that reads the body the way a controller would, so the cached body is populated. */
  private final FilterChain bodyReadingChain =
      (request, response) -> {
        StreamUtils.copyToByteArray(request.getInputStream());
        ((HttpServletResponse) response).setStatus(200);
      };

  @BeforeEach
  void setUp() {
    service = mock(RequestLogService.class);
    filter = new RequestLoggingFilter(service, new RequestLoggingProperties());
  }

  private MockHttpServletRequest postRequest(String body) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/studies/acc_tcga/x");
    request.setServerName("cbioportal.org");
    request.setQueryString("projection=SUMMARY");
    request.setContentType("application/json");
    request.setContent(body.getBytes());
    request.setAttribute(
        HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/studies/{studyId}/x");
    return request;
  }

  private LoggedRequest runAndCapture(HttpServletRequest request)
      throws ServletException, IOException {
    filter.doFilter(request, new MockHttpServletResponse(), bodyReadingChain);
    ArgumentCaptor<LoggedRequest> captor = ArgumentCaptor.forClass(LoggedRequest.class);
    verify(service).save(captor.capture());
    return captor.getValue();
  }

  @Test
  void capturesMethodPathBodyAndEndpoint() throws Exception {
    LoggedRequest logged = runAndCapture(postRequest("{\"ids\":[1,2,3]}"));

    assertEquals("POST", logged.getMethod());
    assertEquals("/api/studies/acc_tcga/x", logged.getPath());
    assertEquals("/api/studies/{studyId}/x", logged.getEndpoint());
    assertEquals("projection=SUMMARY", logged.getQueryString());
    assertEquals("{\"ids\":[1,2,3]}", logged.getBody());
    assertEquals(200, logged.getResponseStatus());
    assertTrue(logged.getDurationMs() >= 0, "durationMs must be non-negative");
    assertFalse(logged.isBodyTruncated());
    assertTrue(logged.getUrl().endsWith("/api/studies/acc_tcga/x?projection=SUMMARY"));
  }

  @Test
  void identicalRequestsProduceSameIdDifferentBodiesDiffer() throws Exception {
    String idA1 = runAndCapture(postRequest("{\"ids\":[1]}")).getId();
    // Fresh mock so the second verify() sees exactly one save.
    service = mock(RequestLogService.class);
    filter = new RequestLoggingFilter(service, new RequestLoggingProperties());
    String idA2 = runAndCapture(postRequest("{\"ids\":[1]}")).getId();

    service = mock(RequestLogService.class);
    filter = new RequestLoggingFilter(service, new RequestLoggingProperties());
    String idB = runAndCapture(postRequest("{\"ids\":[2]}")).getId();

    assertEquals(idA1, idA2, "same method+path+query+body must hash to the same id");
    assertFalse(idA1.equals(idB), "different bodies must hash to different ids");
  }

  @Test
  void durationMs_doesNotAffectDeduplicationId() throws Exception {
    // Drive two deterministic, distinct elapsed values so the test is meaningful:
    // if durationMs were included in the SHA-256 hash the IDs would differ; they must not.
    // A single iterator supplies all four clock ticks across both filter instances:
    //   ticks 1+2 → first request:  (5_000_000 - 0) / 1_000_000  =  5 ms
    //   ticks 3+4 → second request: (200_000_000 - 0) / 1_000_000 = 200 ms
    Iterator<Long> ticks = List.of(0L, 5_000_000L, 0L, 200_000_000L).iterator();
    LongSupplier clock = ticks::next;

    filter = new RequestLoggingFilter(service, new RequestLoggingProperties(), clock);
    LoggedRequest first = runAndCapture(postRequest("{\"ids\":[1]}"));

    service = mock(RequestLogService.class);
    filter = new RequestLoggingFilter(service, new RequestLoggingProperties(), clock);
    LoggedRequest second = runAndCapture(postRequest("{\"ids\":[1]}"));

    // Precondition: durations must actually differ — guards against both being 0 (false positive)
    assertNotEquals(
        first.getDurationMs(),
        second.getDurationMs(),
        "test precondition: durations must differ to be a meaningful dedup check");
    // Despite different durations, the dedup id must be identical
    assertEquals(
        first.getId(),
        second.getId(),
        "id must not change between runs of the same logical request");
  }

  @Test
  void redactsSensitiveHeaders() throws Exception {
    MockHttpServletRequest request = postRequest("{\"ids\":[1]}");
    request.addHeader("Authorization", "Bearer secret-token");
    request.addHeader("X-Custom", "keep-me");

    LoggedRequest logged = runAndCapture(request);

    assertEquals("REDACTED", header(logged, "Authorization"));
    assertEquals("keep-me", header(logged, "X-Custom"));
    assertTrue(
        logged.getHeaders().stream().noneMatch(h -> h.value().contains("secret-token")),
        "redacted secret must not be stored");
  }

  @Test
  void redactsConfiguredQueryAndBodyParams() throws Exception {
    RequestLoggingProperties props = new RequestLoggingProperties();
    props.setRedactParams(List.of("token", "password"));
    filter = new RequestLoggingFilter(service, props);

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/studies/acc_tcga/x");
    request.setServerName("cbioportal.org");
    request.setQueryString("projection=SUMMARY&token=abc123");
    request.setContentType("application/json");
    request.setContent("{\"password\":\"hunter2\",\"ids\":[1]}".getBytes());

    LoggedRequest logged = runAndCapture(request);

    assertEquals("projection=SUMMARY&token=REDACTED", logged.getQueryString());
    assertTrue(logged.getUrl().endsWith("token=REDACTED"));
    assertFalse(logged.getBody().contains("hunter2"), "secret body value must be redacted");
    assertTrue(logged.getBody().contains("REDACTED"));
    assertTrue(logged.getBody().contains("\"ids\""), "non-secret fields must be preserved");
  }

  private static String header(LoggedRequest logged, String name) {
    return logged.getHeaders().stream()
        .filter(h -> h.name().equals(name))
        .map(HttpHeader::value)
        .findFirst()
        .orElse(null);
  }

  @Test
  void durationMs_isCapturedEvenWhenChainThrows() throws Exception {
    // durationMs is computed in the finally block so it must be recorded even when the filter
    // chain throws — ensuring latency data is never lost due to downstream errors.
    // Inject a deterministic clock (7 ms) so we can assert an exact value, not just >= 0.
    LongSupplier clock = List.of(0L, 7_000_000L).iterator()::next;
    filter = new RequestLoggingFilter(service, new RequestLoggingProperties(), clock);

    FilterChain throwingChain =
        (req, res) -> {
          throw new ServletException("simulated downstream failure");
        };

    MockHttpServletRequest request = postRequest("{\"ids\":[1]}");
    assertThrows(
        ServletException.class,
        () -> filter.doFilter(request, new MockHttpServletResponse(), throwingChain));

    ArgumentCaptor<LoggedRequest> captor = ArgumentCaptor.forClass(LoggedRequest.class);
    verify(service).save(captor.capture());
    assertEquals(
        7L,
        captor.getValue().getDurationMs(),
        "durationMs must equal injected elapsed time even when the chain throws");
  }

  @Test
  void skipsRequestsOutsideConfiguredPaths() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/images/logo.png");

    filter.doFilter(request, new MockHttpServletResponse(), bodyReadingChain);

    verify(service, never()).save(any());
  }
}
