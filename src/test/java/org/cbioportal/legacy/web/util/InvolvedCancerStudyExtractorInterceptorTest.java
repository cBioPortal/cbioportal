package org.cbioportal.legacy.web.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.DelegatingServletInputStream;

@RunWith(MockitoJUnitRunner.class)
public class InvolvedCancerStudyExtractorInterceptorTest {

  @InjectMocks private InvolvedCancerStudyExtractorInterceptor interceptor;

  @Mock private CacheMapUtil cacheMapUtil;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  private StringWriter responseWriter;

  @Before
  public void setUp() throws IOException {
    responseWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    when(response.isCommitted()).thenReturn(false);
    when(cacheMapUtil.hasCacheEnabled()).thenReturn(false);
  }

  private ServletInputStream createInputStream(String content) {
    ByteArrayInputStream byteStream =
        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    return new DelegatingServletInputStream(byteStream);
  }

  @Test
  public void testMalformedJsonReturnsBadRequest() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/patients/fetch");
    when(request.getInputStream()).thenReturn(createInputStream("{'invalid json'}"));

    boolean result = interceptor.preHandle(request, response, null);

    assertFalse("preHandle should return false for malformed JSON", result);
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response).setContentType("application/json");
    assertTrue(
        "Response should contain error message",
        responseWriter.toString().contains("Invalid request body"));
  }

  @Test
  public void testEmptyBodyReturnsBadRequest() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/samples/fetch");
    when(request.getInputStream()).thenReturn(createInputStream(""));

    boolean result = interceptor.preHandle(request, response, null);

    assertFalse("preHandle should return false for empty body", result);
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void testValidJsonPassesThrough() throws Exception {
    String validJson =
        "{\"sampleIdentifiers\":[{\"sampleId\":\"sample1\",\"studyId\":\"study1\"}]}";
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/samples/fetch");
    when(request.getInputStream()).thenReturn(createInputStream(validJson));

    boolean result = interceptor.preHandle(request, response, null);

    assertTrue("preHandle should return true for valid JSON", result);
    verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void testNonPostRequestPassesThrough() throws Exception {
    when(request.getMethod()).thenReturn("GET");

    boolean result = interceptor.preHandle(request, response, null);

    assertTrue("preHandle should return true for GET requests", result);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  public void testMessageTruncationRemovesJacksonDetails() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/patients/fetch");
    when(request.getInputStream()).thenReturn(createInputStream("{bad}"));

    interceptor.preHandle(request, response, null);

    String responseBody = responseWriter.toString();
    assertFalse(
        "Response should not contain Jackson source details", responseBody.contains("at [Source:"));
  }

  @Test
  public void testJsonEscapingForSpecialCharacters() throws Exception {
    // This tests that ObjectMapper properly escapes special characters
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/patients/fetch");
    // JSON with unmatched quote that would cause parsing error with special chars in message
    when(request.getInputStream()).thenReturn(createInputStream("{\"field\": \"value with \\n}"));

    interceptor.preHandle(request, response, null);

    String responseBody = responseWriter.toString();
    // Verify it's valid JSON (ObjectMapper would produce valid JSON)
    assertTrue("Response should be valid JSON", responseBody.startsWith("{\"message\":"));
    assertTrue("Response should end properly", responseBody.endsWith("}"));
  }

  @Test
  public void testCommittedResponseDoesNotWrite() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/patients/fetch");
    when(request.getInputStream()).thenReturn(createInputStream("invalid"));
    when(response.isCommitted()).thenReturn(true);

    interceptor.preHandle(request, response, null);

    verify(response, never()).setStatus(anyInt());
    verify(response, never()).getWriter();
  }

  @Test
  public void testNullExceptionMessageHandledGracefully() throws Exception {
    // Test that null message from Exception.getMessage() doesn't cause NPE
    when(request.getMethod()).thenReturn("POST");
    when(request.getPathInfo()).thenReturn("/api/patients/fetch");
    // Throwing an exception that returns null for getMessage()
    when(request.getInputStream()).thenThrow(new NullPointerException());

    boolean result = interceptor.preHandle(request, response, null);

    assertFalse("preHandle should return false for exception", result);
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    String responseBody = responseWriter.toString();
    assertTrue(
        "Response should contain fallback message for null exception message",
        responseBody.contains("Unknown error") || responseBody.contains("Invalid request body"));
  }
}
