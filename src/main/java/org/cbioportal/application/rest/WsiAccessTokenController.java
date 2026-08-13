package org.cbioportal.application.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Issues short-lived capabilities for the same-origin WSI tile service. */
@RestController
@RequestMapping("/api/wsi")
public class WsiAccessTokenController {

  @Value("${wsi.access-token-secret:}")
  private String accessTokenSecret;

  @Value("${wsi.access-token-audience:cbioportal-wsi}")
  private String accessTokenAudience;

  @Value("${wsi.access-token-ttl-seconds:300}")
  private int accessTokenTtlSeconds;

  /** Local development stacks may opt into issuing capabilities without portal authentication. */
  @Value("${wsi.local-auth-bypass:false}")
  private boolean localAuthBypass;

  // The CancerStudyPermissionEvaluator bean does not exist on portals w/o user-authentication.
  @Autowired(required = false)
  private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  @Autowired(required = false)
  private WsiSlideAccessRepository wsiSlideAccessRepository;

  /**
   * Returns the browser-facing pixel access bundle for one materialized slide.
   *
   * <p>The URL is deliberately returned by cBioPortal, rather than resolved by the tile server.
   * The capability is bound to the exact source and thumbnail URLs so a valid token cannot be
   * replayed against another object.
   */
  @GetMapping("/v2/slides/{studyId}/{imageId}/access")
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', "
          + "T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<?> issueSlideAccess(
      @PathVariable String studyId, @PathVariable String imageId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean anonymous = isAnonymous(authentication);
    if (anonymous && !localAuthBypass) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (anonymous) {
      authentication = localDevelopmentAuthentication();
    }
    if (studyId == null || studyId.isBlank() || imageId == null || imageId.isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    if (wsiSlideAccessRepository == null) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
    if (!anonymous
        && (cancerStudyPermissionEvaluator == null
            || !cancerStudyPermissionEvaluator.hasPermission(
                authentication, studyId, "CancerStudyId", AccessLevel.READ))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (accessTokenSecret == null
        || accessTokenSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    WsiSlideAccess access = wsiSlideAccessRepository.getSlideAccess(studyId, imageId);
    if (access == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    int ttl = Math.max(60, Math.min(accessTokenTtlSeconds, 300));
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(ttl);
    String token = issueSlideToken(authentication, studyId, imageId, access, issuedAt, expiresAt);
    WsiSlideAccess response =
        new WsiSlideAccess(
            access.imageId(),
            access.sourceUrl(),
            access.tileMetadata(),
            access.thumbnail(),
            token,
            "Bearer",
            ttl);
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
        .header(HttpHeaders.VARY, "Authorization, Cookie")
        .body(response);
  }

  private String issueSlideToken(
      Authentication authentication,
      String studyId,
      String imageId,
      WsiSlideAccess access,
      Instant issuedAt,
      Instant expiresAt) {
    return Jwts.builder()
        .setHeaderParam("typ", "JWT")
        .setSubject(authentication.getName())
        .setAudience(accessTokenAudience)
        .claim("scope", "wsi:read")
        .claim("study_id", studyId)
        .claim("image_id", imageId)
        .claim("tile_source_sha256", sha256(access.sourceUrl()))
        .claim("thumbnail_source_sha256", sha256(access.thumbnail().sourceUrl()))
        .claim("thumbnail_width", access.thumbnail().width())
        .claim("thumbnail_height", access.thumbnail().height())
        .claim("wsi_auth_version", 2)
        .setIssuedAt(Date.from(issuedAt))
        .setExpiration(Date.from(expiresAt))
        .signWith(SignatureAlgorithm.HS256, accessTokenSecret.getBytes(StandardCharsets.UTF_8))
        .compact();
  }

  private static String sha256(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder(digest.length * 2);
      for (byte item : digest) {
        result.append(String.format("%02x", item));
      }
      return result.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private static boolean isAnonymous(Authentication authentication) {
    return authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken;
  }

  private static Authentication localDevelopmentAuthentication() {
    return new UsernamePasswordAuthenticationToken("local-development", null, List.of());
  }
}
