package org.cbioportal.application.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiThumbnail;
import org.cbioportal.domain.wsi.WsiTileMetadata;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public class WsiAccessTokenControllerTest {

  private final CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator =
      mock(CancerStudyPermissionEvaluator.class);

  private final WsiSlideAccessRepository wsiSlideAccessRepository =
      mock(WsiSlideAccessRepository.class);

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void returnsSourceBoundSlideAccessWhenAuthorized() {
    WsiAccessTokenController plainController = createAuthenticatedController();
    ReflectionTestUtils.setField(
        plainController, "accessTokenSecret", "0123456789abcdef0123456789abcdef");
    ReflectionTestUtils.setField(plainController, "accessTokenAudience", "cbioportal-wsi");
    ReflectionTestUtils.setField(plainController, "accessTokenTtlSeconds", 300);
    ReflectionTestUtils.setField(
        plainController, "cancerStudyPermissionEvaluator", cancerStudyPermissionEvaluator);
    ReflectionTestUtils.setField(
        plainController, "wsiSlideAccessRepository", wsiSlideAccessRepository);
    when(cancerStudyPermissionEvaluator.hasPermission(
            any(Authentication.class),
            eq("study-1"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(true);

    WsiTileMetadata metadata =
        new WsiTileMetadata(
            new WsiTileMetadata.Dimensions(2048, 1024),
            3,
            java.util.List.of(new WsiTileMetadata.Dimensions(2048, 1024)),
            java.util.List.of(1.0),
            2,
            256,
            new WsiTileMetadata.Mpp(0.5, 0.5),
            20,
            "aperio");
    WsiSlideAccess sourceAccess =
        new WsiSlideAccess(
            "slide-1",
            "s3://bucket/slide-1.svs",
            metadata,
            new WsiThumbnail("s3://bucket/thumbs/slide-1.jpg", 128, 64, "image/jpeg"),
            null,
            null,
            0);
    when(wsiSlideAccessRepository.getSlideAccess("study-1", "slide-1"))
        .thenReturn(sourceAccess);

    ResponseEntity<?> response = plainController.issueSlideAccess("study-1", "slide-1");

    assertEquals(200, response.getStatusCode().value());
    WsiSlideAccess body = (WsiSlideAccess) response.getBody();
    assertNotNull(body);
    assertEquals("slide-1", body.imageId());
    assertEquals("s3://bucket/slide-1.svs", body.sourceUrl());
    assertEquals("Bearer", body.tokenType());
    assertEquals(300, body.expiresIn());
    String token = body.accessToken();
    assertTrue(token.length() > 20);
    String payload = token.split("\\.")[1];
    String decodedPayload =
        new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
    assertTrue(decodedPayload.contains("\"study_id\":\"study-1\""));
    assertTrue(decodedPayload.contains("\"image_id\":\"slide-1\""));
    assertTrue(decodedPayload.contains("\"scope\":\"wsi:read\""));
    assertTrue(decodedPayload.contains("\"wsi_auth_version\":2"));
  }

  private WsiAccessTokenController createAuthenticatedController() {
    WsiAccessTokenController plainController = new WsiAccessTokenController();
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("user", "password", "ROLE_USER");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return plainController;
  }
}
