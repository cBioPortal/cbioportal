package org.cbioportal.application.rest.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** The portal's own base URL, as the current request reached it. */
public final class PortalBaseUrl {

  private PortalBaseUrl() {}

  /**
   * Scheme, host and context path of the current request, with no trailing slash, e.g. {@code
   * https://portal.example.org/cbioportal}.
   *
   * <p>Behind a reverse proxy this is the client-facing URL only once forwarded headers are
   * honoured ({@code server.forward-headers-strategy}); otherwise it is the URL the proxy used to
   * reach the portal. Must be called on a request thread.
   */
  public static String current() {
    String url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }
}
