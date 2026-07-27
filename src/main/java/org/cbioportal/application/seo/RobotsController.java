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
 * Serves {@code /robots.txt}. Available only when the {@link SitemapFeature} is enabled (the {@code
 * sitemaps} flag); otherwise the endpoint returns 404, which matches the behavior before this
 * feature existed and lets crawlers treat the portal as unrestricted on instances that do not opt
 * in.
 *
 * <p>The policy is tunable per deployment without code changes via {@code robots.disallow_paths},
 * {@code robots.crawl_delay}, and {@code robots.disallow_user_agents}.
 */
@Hidden
@RestController
public class RobotsController {

  @Autowired private SitemapFeature sitemapFeature;

  // Path prefixes disallowed for all crawlers (comma-separated). Defaults to /proxy/ (external
  // OncoKB/Genome Nexus annotation, the heaviest fan-out and no indexable content). /api/ is
  // deliberately left crawlable: study and patient pages are a client-rendered SPA populated by
  // /api/ XHRs, so blocking /api/ would make a JS-executing indexer render empty pages.
  @Value("${robots.disallow_paths:/proxy/}")
  private String disallowPaths;

  // Crawl-delay (seconds) emitted for all crawlers; blank to omit the directive.
  @Value("${robots.crawl_delay:5}")
  private String crawlDelay;

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

    body.append("User-agent: *\n").append("Allow: /\n");
    for (String path : disallowPaths.split(",")) {
      String p = path.trim();
      if (!p.isEmpty()) {
        body.append("Disallow: ").append(p).append("\n");
      }
    }
    if (!crawlDelay.isBlank()) {
      body.append("Crawl-delay: ").append(crawlDelay.trim()).append("\n");
    }
    body.append("\n").append("Sitemap: ").append(baseUrl).append("/sitemap_index.xml\n");

    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.TEXT_PLAIN)
        .body(body.toString());
  }
}
