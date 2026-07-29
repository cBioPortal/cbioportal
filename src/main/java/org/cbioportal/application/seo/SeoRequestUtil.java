package org.cbioportal.application.seo;

import jakarta.servlet.http.HttpServletRequest;

/** Helpers shared by the robots.txt and sitemap endpoints. */
final class SeoRequestUtil {

  private SeoRequestUtil() {}

  /**
   * Builds the public-facing origin (scheme + host) for absolute URLs in robots.txt and sitemaps.
   *
   * <p>The portal runs behind a TLS-terminating reverse proxy (Traefik), so the servlet request
   * sees plain HTTP against an internal host. Prefer the {@code X-Forwarded-Proto}/{@code
   * X-Forwarded-Host} headers the proxy sets, falling back to the request's own scheme and server
   * name for direct (e.g. local dev) access.
   */
  static String resolveBaseUrl(HttpServletRequest request) {
    String scheme = firstForwardedValue(request.getHeader("X-Forwarded-Proto"));
    if (scheme == null) {
      scheme = request.getScheme();
    }

    String host = firstForwardedValue(request.getHeader("X-Forwarded-Host"));
    if (host == null) {
      host = request.getServerName();
    }

    return scheme + "://" + host;
  }

  /**
   * A forwarded header may carry a comma-separated proxy chain; the client-facing value is first.
   */
  private static String firstForwardedValue(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      return null;
    }
    return headerValue.split(",")[0].trim();
  }
}
