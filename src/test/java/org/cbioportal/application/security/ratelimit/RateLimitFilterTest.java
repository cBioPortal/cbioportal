package org.cbioportal.application.security.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

  private RateLimitProperties properties;
  private RateLimitFilter filter;

  @BeforeEach
  void setUp() {
    properties = new RateLimitProperties();
    properties.setEnabled(true);
    properties.setRequestsPerMinute(60);
    properties.setBurstCapacity(3);
    properties.setCleanupIntervalMinutes(60);
    filter = new RateLimitFilter(properties);
  }

  @Test
  void shouldPassRequestWhenDisabled() throws Exception {
    properties.setEnabled(false);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");

    // Send many requests — all should pass when disabled
    for (int i = 0; i < 10; i++) {
      MockHttpServletResponse response = new MockHttpServletResponse();
      MockFilterChain chain = new MockFilterChain();
      filter.doFilter(request, response, chain);
      assertEquals(200, response.getStatus());
    }
  }

  @Test
  void shouldAllowRequestsWithinLimit() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    for (int i = 0; i < 3; i++) {
      response = new MockHttpServletResponse();
      chain = new MockFilterChain();
      filter.doFilter(request, response, chain);
      assertEquals(200, response.getStatus(), "Request " + (i + 1) + " should pass");
    }
  }

  @Test
  void shouldReturn429WhenRateLimited() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");

    // Exhaust the burst capacity (3)
    for (int i = 0; i < 3; i++) {
      filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    // 4th request should be rate-limited
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(429, response.getStatus());
    assertEquals("application/json", response.getContentType());
    assertTrue(response.getContentAsString().contains("Rate limit exceeded"));
    assertTrue(response.getHeader("Retry-After") != null);
  }

  @Test
  void shouldTrackDifferentClientsSeparately() throws Exception {
    MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/studies");
    request1.setRemoteAddr("192.168.1.1");

    MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/studies");
    request2.setRemoteAddr("192.168.1.2");

    // Exhaust client 1's tokens
    for (int i = 0; i < 3; i++) {
      filter.doFilter(request1, new MockHttpServletResponse(), new MockFilterChain());
    }

    // Client 2 should still be allowed
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request2, response, new MockFilterChain());
    assertEquals(200, response.getStatus(), "Different client should not be rate-limited");
  }

  @Test
  void shouldUseXForwardedForHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("10.0.0.1");
    request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");

    String clientId = filter.resolveClientId(request);
    assertEquals("203.0.113.50", clientId, "Should use first IP from X-Forwarded-For");
  }

  @Test
  void shouldFallbackToRemoteAddrWhenNoForwardedHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("192.168.1.100");

    String clientId = filter.resolveClientId(request);
    assertEquals("192.168.1.100", clientId);
  }

  @Test
  void shouldCleanupStaleBuckets() throws Exception {
    properties.setCleanupIntervalMinutes(0); // immediate threshold for testing

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("10.0.0.1");
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertEquals(1, filter.getBucketCount(), "Should have 1 tracked client");

    // Wait a bit so the bucket becomes stale (cleanup interval = 0 minutes = immediate)
    Thread.sleep(10);
    filter.cleanupStaleBuckets();

    assertEquals(0, filter.getBucketCount(), "Stale bucket should be cleaned up");
  }
}
