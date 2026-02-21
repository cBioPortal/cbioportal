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
    assertEquals(
        "{\"message\":\"Rate limit exceeded. Please try again later.\"}",
        response.getContentAsString());
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
  void shouldUseXForwardedForHeaderWhenTrusted() {
    properties.setTrustProxies(true);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("10.0.0.1");
    // Test both IPv4 and IPv6 in chain
    request.addHeader("X-Forwarded-For", "203.0.113.50, 2001:db8:85a3:8d3:1319:8a2e:370:7348");

    String clientId = filter.resolveClientId(request);
    assertEquals(
        "203.0.113.50", clientId, "Should use first valid IP from X-Forwarded-For when trusted");
  }

  @Test
  void shouldIgnoreXForwardedForHeaderWhenNotTrusted() {
    properties.setTrustProxies(false);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("10.0.0.1");
    request.addHeader("X-Forwarded-For", "203.0.113.50");

    String clientId = filter.resolveClientId(request);
    assertEquals("10.0.0.1", clientId, "Should ignore X-Forwarded-For when not trusted");
  }

  @Test
  void shouldFallbackToRemoteAddrWhenHeaderInvalidOrBlank() {
    properties.setTrustProxies(true);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("10.0.0.1");

    // Test blank
    request.addHeader("X-Forwarded-For", "  ");
    assertEquals("10.0.0.1", filter.resolveClientId(request));

    // Test malformed
    request.removeHeader("X-Forwarded-For");
    request.addHeader("X-Forwarded-For", "not-an-ip, 1.1.1.1");
    assertEquals("10.0.0.1", filter.resolveClientId(request));
  }

  @Test
  void shouldLimitTotalBuckets() throws Exception {
    properties.setMaxBuckets(2);

    // Client 1
    MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/a");
    request1.setRemoteAddr("1.1.1.1");
    filter.doFilter(request1, new MockHttpServletResponse(), new MockFilterChain());

    // Client 2
    MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/b");
    request2.setRemoteAddr("2.2.2.2");
    filter.doFilter(request2, new MockHttpServletResponse(), new MockFilterChain());

    assertEquals(2, filter.getBucketCount());

    // Client 3 - should be rejected if map is full
    MockHttpServletRequest request3 = new MockHttpServletRequest("GET", "/api/c");
    request3.setRemoteAddr("3.3.3.3");
    MockHttpServletResponse response3 = new MockHttpServletResponse();
    filter.doFilter(request3, response3, new MockFilterChain());

    assertEquals(429, response3.getStatus(), "Should reject new clients when maxBuckets reached");
    assertEquals(2, filter.getBucketCount(), "Should not have added 3rd client");
  }

  @Test
  void shouldCleanupStaleBuckets() throws Exception {
    properties.setCleanupIntervalMinutes(0); // threshold for immediate cleanup

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("10.0.0.1");
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertEquals(1, filter.getBucketCount(), "Should have 1 tracked client");

    // Wait a bit so the bucket becomes stale
    Thread.sleep(50);
    filter.cleanupStaleBuckets();

    assertEquals(0, filter.getBucketCount(), "Stale bucket should be cleaned up");
  }
}
