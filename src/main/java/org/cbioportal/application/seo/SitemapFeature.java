package org.cbioportal.application.seo;

import org.cbioportal.legacy.utils.security.PortalSecurityConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Availability of the SEO feature (robots.txt + sitemaps).
 *
 * <p>It is turned on by the {@code sitemaps} program argument, but only on <b>public</b> portals:
 * when portal authorization is enabled, different users may see different studies, so publishing a
 * single sitemap of study/patient URLs would either leak restricted URLs or be wrong. The feature
 * therefore stays off whenever authorization is enabled, and the endpoints return 404 there.
 * Because it only ever runs on a public portal, the endpoints expose exclusively already-public
 * URLs and need no per-request authorization.
 */
@Component
class SitemapFeature {

  private final boolean enabled;

  SitemapFeature(
      @Value("${sitemaps:false}") boolean sitemapsEnabled,
      @Value("${authenticate:false}") String authenticate) {
    this.enabled = sitemapsEnabled && !PortalSecurityConfig.userAuthorizationEnabled(authenticate);
  }

  boolean isEnabled() {
    return enabled;
  }
}
