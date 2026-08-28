package org.cbioportal.application.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiThumbnail;
import org.cbioportal.domain.wsi.WsiTileMetadata;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;

public class WsiAccessTokenControllerTest {

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
        plainController, "wsiSlideAccessRepository", wsiSlideAccessRepository);

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
            "aperio",
            null,
            null,
            null,
            null,
            null);
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

  @Test
  public void exposesVersionedAndCompatibilitySlideAccessRoutes() throws Exception {
    GetMapping mapping =
        WsiAccessTokenController.class
            .getMethod("issueSlideAccess", String.class, String.class)
            .getAnnotation(GetMapping.class);

    assertNotNull(mapping);
    assertTrue(
        Arrays.asList(mapping.value()).contains("/slides/{studyId}/{imageId}/access"));
    assertTrue(
        Arrays.asList(mapping.value()).contains("/v2/slides/{studyId}/{imageId}/access"));
  }

  @Test
  public void returnsAnnotationScopeWhenRequested() {
    WsiAccessTokenController plainController = createAuthenticatedController();
    ReflectionTestUtils.setField(
        plainController, "accessTokenSecret", "0123456789abcdef0123456789abcdef");
    ReflectionTestUtils.setField(plainController, "accessTokenAudience", "cbioportal-wsi");
    ReflectionTestUtils.setField(plainController, "accessTokenTtlSeconds", 300);
    ResponseEntity<?> response = plainController.issueAccessToken("study-1", "annotations");

    assertEquals(200, response.getStatusCode().value());
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertNotNull(body);
    assertEquals("Bearer", body.get("token_type"));
    String token = (String) body.get("access_token");
    String payload = token.split("\\.")[1];
    String decodedPayload =
        new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
    assertTrue(decodedPayload.contains("\"scope\":\"annotations:read annotations:write\""));
    assertTrue(decodedPayload.contains("\"study_id\":\"study-1\""));
  }

  @Test
  public void rejectsNonAnnotationPurpose() {
    WsiAccessTokenController plainController = createAuthenticatedController();
    ReflectionTestUtils.setField(
        plainController, "accessTokenSecret", "0123456789abcdef0123456789abcdef");

    ResponseEntity<?> response = plainController.issueAccessToken("study-1", "wsi");

    assertEquals(400, response.getStatusCode().value());
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
