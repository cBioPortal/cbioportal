package org.cbioportal.application.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  private final CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  public WsiAccessTokenController(CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator) {
    this.cancerStudyPermissionEvaluator = cancerStudyPermissionEvaluator;
  }

  @GetMapping("/access-token")
  public ResponseEntity<?> issueAccessToken(@RequestParam(required = false) String studyId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (studyId == null || studyId.isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    if (!cancerStudyPermissionEvaluator.hasPermission(
        authentication, studyId, "CancerStudyId", AccessLevel.READ)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (accessTokenSecret == null || accessTokenSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    int ttl = Math.max(60, Math.min(accessTokenTtlSeconds, 900));
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(ttl);
    String token =
        Jwts.builder()
            .setSubject(authentication.getName())
            .setAudience(accessTokenAudience)
            .claim("scope", "wsi:read")
            .claim("study_id", studyId)
            .setIssuedAt(Date.from(issuedAt))
            .setExpiration(Date.from(expiresAt))
            .signWith(SignatureAlgorithm.HS256, accessTokenSecret.getBytes(StandardCharsets.UTF_8))
            .compact();

    return ResponseEntity.ok(
        Map.of("access_token", token, "token_type", "Bearer", "expires_in", ttl));
  }
}
