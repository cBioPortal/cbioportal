package org.cbioportal.infrastructure.requestlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.List;
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
  void skipsRequestsOutsideConfiguredPaths() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/images/logo.png");

    filter.doFilter(request, new MockHttpServletResponse(), bodyReadingChain);

    verify(service, never()).save(any());
  }
}
