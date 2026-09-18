package org.cbioportal.application.security.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicInteger;
import org.cbioportal.domain.ratelimit.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

  @Test
  void returnsTooManyRequestsAndRetryAfterWhenClientExceedsLimit() throws Exception {
    RateLimitProperties properties = new RateLimitProperties();
    properties.setRequestsPerMinute(60);
    properties.setBurstCapacity(1);
    RateLimitFilter filter =
        new RateLimitFilter(
            new RateLimitService(
                properties.getRequestsPerMinute(), properties.getBurstCapacity(), 100));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");
    request.setRemoteAddr("198.51.100.10");
    AtomicInteger chainCalls = new AtomicInteger();
    FilterChain chain = (ignoredRequest, ignoredResponse) -> chainCalls.incrementAndGet();

    filter.doFilter(request, new MockHttpServletResponse(), chain);
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, chain);

    assertEquals(1, chainCalls.get());
    assertEquals(429, response.getStatus());
    assertEquals("1", response.getHeader("Retry-After"));
  }
}
