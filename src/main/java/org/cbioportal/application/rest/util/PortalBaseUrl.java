package org.cbioportal.application.rest.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * The portal's own base URL, which resource URL templates substitute for {@code ${base_url}}.
 *
 * <p>When {@code resource.url_template.base_url} is set, that value is used for every request.
 * Otherwise the base URL is taken from the current request.
 */
@Component
public class PortalBaseUrl {

  private final String configured;

  public PortalBaseUrl(@Value("${resource.url_template.base_url:}") String configured) {
    this.configured = configured == null ? "" : stripTrailingSlashes(configured.trim());
  }

  /**
   * The base URL for the current request: scheme, host and context path, with no trailing slash,
   * e.g. {@code https://portal.example.org/cbioportal}.
   *
   * <p>With {@code resource.url_template.base_url} set, it is that value. Otherwise it is read from
   * the request, which reflects {@code X-Forwarded-*} / {@code Forwarded} headers only when Spring
   * applies them: with {@code server.forward-headers-strategy} set to {@code NATIVE} or {@code
   * FRAMEWORK}, or, with the property unset, when Spring Boot detects a cloud platform such as
   * Kubernetes (it then uses {@code NATIVE}). Tomcat's {@code NATIVE} handling trusts the headers
   * only from proxies matching {@code server.tomcat.remoteip.internal-proxies}. Behind a reverse
   * proxy whose headers are not applied, the base URL is the address the proxy used to reach the
   * portal. Must be called on a request thread unless the base URL is configured.
   */
  public String current() {
    if (!configured.isEmpty()) {
      return configured;
    }
    return stripTrailingSlashes(
        ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
  }

  private static String stripTrailingSlashes(String url) {
    int end = url.length();
    while (end > 0 && url.charAt(end - 1) == '/') {
      end--;
    }
    return url.substring(0, end);
  }
}
