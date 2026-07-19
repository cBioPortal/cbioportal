package org.cbioportal.application.seo;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves {@code /robots.txt}. Enabled by the {@code sitemaps} program argument (the same flag that
 * enables {@link SitemapController}); when disabled the endpoint returns 404, which matches the
 * behavior before this feature existed and lets crawlers treat the portal as unrestricted on
 * instances that do not opt in.
 */
@Hidden
@RestController
public class RobotsController {

  @Value("${sitemaps:false}")
  private boolean sitemapsEnabled;

  @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> robots(HttpServletRequest request) {
    if (!sitemapsEnabled) {
      return ResponseEntity.notFound().build();
    }

    String baseUrl = SeoRequestUtil.resolveBaseUrl(request);

    // PetalBot (Huawei Cloud) disguises itself with a browser User-Agent and fans out across every
    // per-patient page, driving expensive API traffic. It honors robots.txt, so disallow it
    // outright.
    //
    // For all other crawlers the goal is SEO: study and patient pages are a client-rendered React
    // SPA whose content is populated by /api/ XHRs, so /api/ must stay crawlable or a JS-executing
    // indexer (Googlebot) would render empty pages. Only /proxy/ is disallowed — it forwards to
    // external services (OncoKB, Genome Nexus) and carries no indexable content. Crawl-delay slows
    // the polite crawlers, and the sitemap advertises the study/patient URLs worth indexing.
    String body =
        "User-agent: PetalBot\n"
            + "Disallow: /\n"
            + "\n"
            + "User-agent: *\n"
            + "Allow: /\n"
            + "Disallow: /proxy/\n"
            + "Crawl-delay: 5\n"
            + "\n"
            + "Sitemap: "
            + baseUrl
            + "/sitemap_index.xml\n";

    return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(body);
  }
}
