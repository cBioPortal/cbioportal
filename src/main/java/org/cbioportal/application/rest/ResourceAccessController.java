package org.cbioportal.application.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.cbioportal.domain.resource.ResourceAccess;
import org.cbioportal.domain.resource.ResourceServing;
import org.cbioportal.domain.resource.repository.ResourceServingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Issues short-lived tile capabilities for resource rows that carry serving data.
 *
 * <p>Any resource whose rows have a {@code metadata.serving} object (a key, an upstream-sealed
 * source and a thumbnail size) can be opened through the tile server; whole-slide images
 * (WSI_SAMPLE, WSI_PATIENT) are one such resource. Whoever may read the study may open its rows.
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceAccessController {

  /** Tile capability format: the key in {@code slide_key} plus the sealed source in {@code enc}. */
  static final int TILE_AUTH_VERSION = 4;

  @Value("${wsi.access-token-secret:}")
  private String accessTokenSecret;

  @Value("${wsi.access-token-audience:cbioportal-wsi}")
  private String accessTokenAudience;

  @Value("${wsi.access-token-ttl-seconds:300}")
  private int accessTokenTtlSeconds;

  private final ResourceServingRepository resourceServingRepository;

  public ResourceAccessController(ResourceServingRepository resourceServingRepository) {
    this.resourceServingRepository = resourceServingRepository;
  }

  /**
   * Returns a tile capability for the study patient's {@code resourceId} row whose serving key is
   * {@code key}. The patient keeps the lookup to that patient's rows. The row's sealed source is
   * copied verbatim into the capability's {@code enc} claim.
   */
  @GetMapping("/{studyId}/{resourceId}/access")
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<ResourceAccess> issueAccess(
      @PathVariable String studyId,
      @PathVariable String resourceId,
      @RequestParam String key,
      @RequestParam String patientId) {
    ResourceServing serving =
        resourceServingRepository.getServing(studyId, resourceId, key, patientId);
    if (serving == null) {
      return ResponseEntity.notFound().build();
    }
    Instant issuedAt = Instant.now();
    String token =
        issueToken(studyId, serving, issuedAt, issuedAt.plusSeconds(accessTokenTtlSeconds));
    return ResponseEntity.ok(
        new ResourceAccess(resourceId, key, token, "Bearer", accessTokenTtlSeconds));
  }

  private String issueToken(
      String studyId, ResourceServing serving, Instant issuedAt, Instant expiresAt) {
    return Jwts.builder()
        .setHeaderParam("typ", "JWT")
        .setSubject(subject())
        .setAudience(accessTokenAudience)
        .claim("scope", "wsi:read")
        .claim("study_id", studyId)
        .claim("resource_id", serving.resourceId())
        // The tile server binds the sealed source to this claim.
        .claim("slide_key", serving.key())
        .claim("thumbnail_width", serving.thumbnailWidth())
        .claim("thumbnail_height", serving.thumbnailHeight())
        .claim("wsi_auth_version", TILE_AUTH_VERSION)
        .claim("enc", serving.sealedSource())
        .setIssuedAt(Date.from(issuedAt))
        .setExpiration(Date.from(expiresAt))
        .signWith(SignatureAlgorithm.HS256, accessTokenSecret.getBytes(StandardCharsets.UTF_8))
        .compact();
  }

  /** The caller's name, or {@code anonymousUser} when the portal has no login or nobody is in. */
  private static String subject() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null || authentication.getName() == null
        ? "anonymousUser"
        : authentication.getName();
  }
}
