package org.cbioportal.application.seo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class SeoRequestUtilTest {

  private static final String PROTO_HEADER = "X-Forwarded-Proto";
  private static final String HOST_HEADER = "X-Forwarded-Host";

  @Test
  public void resolveBaseUrlWithoutForwardedHeadersUsesRequestSchemeAndServerName() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");

    assertEquals("http://localhost", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlWithoutForwardedHeadersDoesNotIncludePort() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("https");
    request.setServerName("internal.example.org");
    request.setServerPort(8443);

    assertEquals("https://internal.example.org", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlPrefersForwardedProtoAndHost() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(PROTO_HEADER, "https");
    request.addHeader(HOST_HEADER, "www.cbioportal.org");

    assertEquals("https://www.cbioportal.org", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlFallsBackToRequestHostWhenOnlyForwardedProtoIsPresent() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(PROTO_HEADER, "https");

    assertEquals("https://internal-host", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlFallsBackToRequestSchemeWhenOnlyForwardedHostIsPresent() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(HOST_HEADER, "www.cbioportal.org");

    assertEquals("http://www.cbioportal.org", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlIgnoresEmptyForwardedHeaders() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(PROTO_HEADER, "");
    request.addHeader(HOST_HEADER, "");

    assertEquals("http://internal-host", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlIgnoresBlankForwardedHeaders() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(PROTO_HEADER, "   ");
    request.addHeader(HOST_HEADER, "   ");

    assertEquals("http://internal-host", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlUsesFirstValueOfCommaSeparatedForwardedChain() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(PROTO_HEADER, "https,http");
    request.addHeader(HOST_HEADER, "www.cbioportal.org,proxy.internal");

    assertEquals("https://www.cbioportal.org", SeoRequestUtil.resolveBaseUrl(request));
  }

  @Test
  public void resolveBaseUrlTrimsWhitespaceAroundForwardedChainValues() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("internal-host");
    request.addHeader(PROTO_HEADER, "  https , http");
    request.addHeader(HOST_HEADER, "  www.cbioportal.org , proxy.internal");

    assertEquals("https://www.cbioportal.org", SeoRequestUtil.resolveBaseUrl(request));
  }
}
