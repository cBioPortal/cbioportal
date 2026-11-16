package org.cbioportal.application.seo;

import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves {@code /llms.txt}: guidance for AI agents and other automated clients, such as where to
 * download data in bulk instead of rendering pages or looping over the web API.
 *
 * <p>The content is deployment-specific, so nothing is built in. Set {@code llms_txt.location} to a
 * Spring resource location (e.g. {@code file:/cbioportal/llms.txt} or {@code classpath:llms.txt});
 * when it is unset the endpoint returns 404. The file is re-read at most once per {@link
 * #CACHE_TTL}, so an updated file is picked up without a restart.
 */
@Hidden
@RestController
public class LlmsTxtController {

  private static final Logger LOG = LoggerFactory.getLogger(LlmsTxtController.class);

  static final Duration CACHE_TTL = Duration.ofMinutes(5);

  private static final MediaType TEXT_MARKDOWN =
      new MediaType("text", "markdown", StandardCharsets.UTF_8);

  @Autowired private ResourceLoader resourceLoader;

  @Value("${llms_txt.location:}")
  private String location;

  private String cachedBody;
  private Instant lastReadAttempt = Instant.MIN;

  @GetMapping(value = "/llms.txt")
  public ResponseEntity<String> llmsTxt() {
    if (!isConfigured()) {
      return ResponseEntity.notFound().build();
    }
    String body = getBody();
    if (body == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().contentType(TEXT_MARKDOWN).body(body);
  }

  boolean isConfigured() {
    return location != null && !location.isBlank();
  }

  private synchronized String getBody() {
    Instant now = Instant.now();
    // Failed reads count as attempts too, so an unreadable file is retried at most once per TTL.
    if (now.isBefore(lastReadAttempt.plus(CACHE_TTL))) {
      return cachedBody;
    }
    lastReadAttempt = now;
    Resource resource = resourceLoader.getResource(location.trim());
    try (InputStream in = resource.getInputStream()) {
      cachedBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      // Keep serving the last good copy if the file is briefly unreadable.
      LOG.warn("Could not read llms_txt.location {}: {}", location, e.getMessage());
    }
    return cachedBody;
  }
}
