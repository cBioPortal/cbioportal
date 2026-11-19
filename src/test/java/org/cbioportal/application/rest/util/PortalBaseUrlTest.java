package org.cbioportal.application.rest.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class PortalBaseUrlTest {

  @After
  public void clearRequest() {
    RequestContextHolder.resetRequestAttributes();
  }

  private static void request(String scheme, String host, int port, String contextPath) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme(scheme);
    request.setServerName(host);
    request.setServerPort(port);
    request.setContextPath(contextPath);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  public void unsetReadsTheRequest() {
    request("https", "portal.example.org", 443, "/cbioportal");

    assertThat(new PortalBaseUrl("").current()).isEqualTo("https://portal.example.org/cbioportal");
  }

  @Test
  public void unsetReadsTheRequestWithoutAContextPath() {
    request("http", "localhost", 8080, "");

    assertThat(new PortalBaseUrl(null).current()).isEqualTo("http://localhost:8080");
  }

  @Test
  public void configuredValueWinsOverTheRequest() {
    request("http", "internal-host", 8080, "/cbioportal");

    assertThat(new PortalBaseUrl(" https://cbioportal.example.org/ ").current())
        .isEqualTo("https://cbioportal.example.org");
  }

  @Test
  public void configuredValueNeedsNoRequest() {
    assertThat(new PortalBaseUrl("https://cbioportal.example.org/portal").current())
        .isEqualTo("https://cbioportal.example.org/portal");
  }
}
