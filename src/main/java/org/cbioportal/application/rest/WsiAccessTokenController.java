package org.cbioportal.application.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.cbioportal.domain.wsi.WsiDeidentification;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiSlideSource;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Issues short-lived capabilities for the same-origin WSI tile service. */
@RestController
@RequestMapping("/api/wsi")
public class WsiAccessTokenController {

  /** Tile capability format: slide_key + sealed source claim (contract wsi-serving-v6). */
  static final int WSI_AUTH_VERSION = 4;

  @Value("${wsi.access-token-secret:}")
  private String accessTokenSecret;

  @Value("${wsi.access-token-audience:cbioportal-wsi}")
  private String accessTokenAudience;

  @Value("${wsi.access-token-ttl-seconds:300}")
  private int accessTokenTtlSeconds;

  /** Local development stacks may opt into issuing capabilities without portal authentication. */
  @Value("${wsi.local-auth-bypass:false}")
  private boolean localAuthBypass;

  @Autowired(required = false)
  private WsiSlideAccessRepository wsiSlideAccessRepository;

  /**
   * Returns the browser-facing pixel access bundle for one materialized slide, addressed by its
   * opaque slide key.
   *
   * <p>The slide key is the public, stable name of a slide: unlike the resource-data row ID it
   * survives a reimport, and it identifies nothing outside the portal. It is a query parameter so
   * the resource-data URL shape is unchanged.
   *
   * <p>The slide's source and thumbnail locations are sealed upstream, with a key cBioPortal does
   * not hold and the slide key as associated data, into the stored {@code sealed_source}. The
   * capability carries it verbatim as its {@code enc} claim, and only the tile server can open it.
   * It never appears in the response outside the signed capability.
   */
  @GetMapping("/v2/resources/{studyId}/{patientId}/access")
  @PreAuthorize(
      "!isAuthenticated() or hasPermission(#studyId, 'CancerStudyId', "
          + "T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<?> issueSlideAccess(
      @PathVariable String studyId,
      @PathVariable String patientId,
      @RequestParam(required = false) String slideKey) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean anonymous = isAnonymous(authentication);
    if (anonymous && !localAuthBypass) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (anonymous) {
      authentication = localDevelopmentAuthentication();
    }
    if (studyId == null
        || studyId.isBlank()
        || patientId == null
        || patientId.isBlank()
        || !WsiDeidentification.isSlideKey(slideKey)) {
      return ResponseEntity.badRequest().build();
    }
    if (wsiSlideAccessRepository == null) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
    if (accessTokenSecret == null
        || accessTokenSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    // The repository guarantees the returned source is bound to slideKey.
    WsiSlideSource source = wsiSlideAccessRepository.getSlideSource(studyId, patientId, slideKey);
    if (source == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    int ttl = Math.max(60, Math.min(accessTokenTtlSeconds, 300));
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(ttl);
    String token = issueSlideToken(authentication, studyId, source, issuedAt, expiresAt);
    WsiSlideAccess response =
        new WsiSlideAccess(
            source.slideKey(), source.tileMetadata(), source.thumbnail(), token, "Bearer", ttl);
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
        .header(HttpHeaders.VARY, "Authorization, Cookie")
        .body(response);
  }

  private String issueSlideToken(
      Authentication authentication,
      String studyId,
      WsiSlideSource source,
      Instant issuedAt,
      Instant expiresAt) {
    return Jwts.builder()
        .setHeaderParam("typ", "JWT")
        .setSubject(authentication.getName())
        .setAudience(accessTokenAudience)
        .claim("scope", "wsi:read")
        .claim("study_id", studyId)
        .claim("slide_key", source.slideKey())
        .claim("thumbnail_width", source.thumbnail().width())
        .claim("thumbnail_height", source.thumbnail().height())
        .claim("wsi_auth_version", WSI_AUTH_VERSION)
        .claim("enc", source.sealedSource())
        .setIssuedAt(Date.from(issuedAt))
        .setExpiration(Date.from(expiresAt))
        .signWith(SignatureAlgorithm.HS256, accessTokenSecret.getBytes(StandardCharsets.UTF_8))
        .compact();
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
