package org.cbioportal.application.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiSlideSource;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
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

/**
 * Issues short-lived capabilities for the WSI tile server.
 *
 * <p>Slides are listed through the generic resource APIs (resources {@code WSI_SAMPLE} and {@code
 * WSI_PATIENT}). This endpoint is the one step that needs the portal: it checks that the caller may
 * read the study and signs a capability for one of its slides.
 */
@RestController
@RequestMapping("/api/wsi")
public class WsiAccessTokenController {

  /** Tile capability format: slide_key + sealed source claim. */
  static final int WSI_AUTH_VERSION = 4;

  /** Opaque per-slide key: 12 lowercase hex characters computed upstream. */
  private static final Pattern SLIDE_KEY = Pattern.compile("[0-9a-f]{12}");

  @Value("${wsi.access-token-secret:}")
  private String accessTokenSecret;

  @Value("${wsi.access-token-audience:cbioportal-wsi}")
  private String accessTokenAudience;

  @Value("${wsi.access-token-ttl-seconds:300}")
  private int accessTokenTtlSeconds;

  /** Local development stacks may opt into issuing capabilities without portal authentication. */
  @Value("${wsi.local-auth-bypass:false}")
  private boolean localAuthBypass;

  private final WsiSlideAccessRepository wsiSlideAccessRepository;

  public WsiAccessTokenController(WsiSlideAccessRepository wsiSlideAccessRepository) {
    this.wsiSlideAccessRepository = wsiSlideAccessRepository;
  }

  /**
   * Returns a tile capability for one slide of the patient, addressed by its slide key. The slide's
   * sealed source is copied verbatim into the capability's {@code enc} claim; only the tile server
   * can open it.
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
      return privateResponse(HttpStatus.UNAUTHORIZED).build();
    }
    if (anonymous) {
      authentication = localDevelopmentAuthentication();
    }
    if (studyId == null
        || studyId.isBlank()
        || patientId == null
        || patientId.isBlank()
        || slideKey == null
        || !SLIDE_KEY.matcher(slideKey).matches()) {
      return privateResponse(HttpStatus.BAD_REQUEST).build();
    }
    if (accessTokenSecret == null
        || accessTokenSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
      return privateResponse(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
    WsiSlideSource source = wsiSlideAccessRepository.getSlideSource(studyId, patientId, slideKey);
    if (source == null) {
      return privateResponse(HttpStatus.NOT_FOUND).build();
    }
    int ttl = Math.max(60, Math.min(accessTokenTtlSeconds, 300));
    Instant issuedAt = Instant.now();
    String token =
        issueSlideToken(authentication, studyId, source, issuedAt, issuedAt.plusSeconds(ttl));
    return privateResponse(HttpStatus.OK).body(new WsiSlideAccess(slideKey, token, "Bearer", ttl));
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
        .claim("thumbnail_width", source.thumbnailWidth())
        .claim("thumbnail_height", source.thumbnailHeight())
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

  /** A response that no shared cache may store and that varies with the caller's credentials. */
  private static ResponseEntity.BodyBuilder privateResponse(HttpStatus status) {
    return ResponseEntity.status(status)
        .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
        .header(HttpHeaders.VARY, "Authorization, Cookie");
  }

  private static Authentication localDevelopmentAuthentication() {
    return new UsernamePasswordAuthenticationToken("local-development", null, List.of());
  }
}
