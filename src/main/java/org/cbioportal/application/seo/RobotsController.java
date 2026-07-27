package org.cbioportal.application.seo;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves {@code /robots.txt}. Available only when the {@link SitemapFeature} is enabled (public
 * portal with the {@code sitemaps} flag); otherwise the endpoint returns 404, which matches the
 * behavior before this feature existed and lets crawlers treat the portal as unrestricted on
 * instances that do not opt in.
 */
@Hidden
@RestController
public class RobotsController {

  @Autowired private SitemapFeature sitemapFeature;

  // Comma-separated crawler User-Agent names to block entirely; empty by default so the open-source
  // policy stays generic. A deployment can set e.g. --robots.disallow_user_agents=PetalBot to block
  // a specific abusive bot.
  @Value("${robots.disallow_user_agents:}")
  private String disallowUserAgents;

  @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> robots(HttpServletRequest request) {
    if (!sitemapFeature.isEnabled()) {
      return ResponseEntity.notFound().build();
    }

    String baseUrl = SeoRequestUtil.resolveBaseUrl(request);

    StringBuilder body = new StringBuilder();

    // Deployment-specific bots to block, each as its own group, before the shared policy below.
    for (String userAgent : disallowUserAgents.split(",")) {
      String agent = userAgent.trim();
      if (!agent.isEmpty()) {
        body.append("User-agent: ").append(agent).append("\n").append("Disallow: /\n\n");
      }
    }

    // The goal is SEO: study and patient pages are a client-rendered React SPA whose content is
    // populated by /api/ XHRs, so /api/ must stay crawlable or a JS-executing indexer (Googlebot)
    // would render empty pages. Only /proxy/ is disallowed — it forwards to external services
    // (OncoKB, Genome Nexus) and carries no indexable content. Crawl-delay throttles crawlers, and
    // the sitemap advertises the study/patient URLs worth indexing.
    body.append("User-agent: *\n")
        .append("Allow: /\n")
        .append("Disallow: /proxy/\n")
        .append("Crawl-delay: 5\n")
        .append("\n")
        .append("Sitemap: ")
        .append(baseUrl)
        .append("/sitemap_index.xml\n");

    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.TEXT_PLAIN)
        .body(body.toString());
  }
}
