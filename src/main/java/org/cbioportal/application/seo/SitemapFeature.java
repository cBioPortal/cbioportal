package org.cbioportal.application.seo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Availability of the SEO feature (robots.txt + sitemaps), toggled by the {@code sitemaps} program
 * argument.
 *
 * <p>It is meant for public portals and is safe to leave off elsewhere: the sitemap index and
 * robots.txt are built from an anonymous study listing, so they advertise only public studies, and
 * per-study patient enumeration is authorization-checked. Enabling it therefore never exposes
 * non-public data even on a portal that also serves access-controlled studies.
 */
@Component
class SitemapFeature {

  private final boolean enabled;

  SitemapFeature(@Value("${sitemaps:false}") boolean sitemapsEnabled) {
    this.enabled = sitemapsEnabled;
  }

  boolean isEnabled() {
    return enabled;
  }
}
