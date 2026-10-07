package org.cbioportal.application.proxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ProxyControllerTest {
  private static final String PROXY_PATH = "/proxy/A8F74CD7851BDEE8DCD2E86AB4E2A711/";

  private final ProxyController proxyController = new ProxyController(new Monkifier());

  @Test
  public void getEncodedPathRemovesContextPathBeforeProxyPath() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("/cbioportal");
    request.setRequestURI("/cbioportal" + PROXY_PATH + "L2V2aWRlbmNl");

    assertEquals("L2V2aWRlbmNl", proxyController.getEncodedPath(request));
  }
}
