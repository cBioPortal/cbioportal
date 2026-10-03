package org.cbioportal.application.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.cbioportal.domain.wsi.WsiDeidentification;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiSlideSource;
import org.cbioportal.domain.wsi.WsiThumbnail;
import org.cbioportal.domain.wsi.WsiTileMetadata;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public class WsiAccessTokenControllerTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";
  private static final String SLIDE_KEY = "2351e12d49557627b24fe71e17ec5c64";
  private static final String IMAGE_ID = "syn-img-0001";
  private static final String SOURCE = "s3://bucket/syn-img-0001.svs";
  private static final String THUMBNAIL = "s3://bucket/thumbs/syn-img-0001.jpg";

  private final WsiSlideAccessRepository wsiSlideAccessRepository =
      mock(WsiSlideAccessRepository.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void returnsSlideKeyBoundAccessWithoutIdentifyingFields() throws Exception {
    WsiAccessTokenController controller = createAuthenticatedController();
    when(wsiSlideAccessRepository.getSlideSource("study-1", "patient-1", SLIDE_KEY))
        .thenReturn(source());

    ResponseEntity<?> response = controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY);

    assertEquals(200, response.getStatusCode().value());
    WsiSlideAccess body = (WsiSlideAccess) response.getBody();
    assertNotNull(body);
    assertEquals(SLIDE_KEY, body.slideKey());
    assertEquals("Bearer", body.tokenType());
    assertEquals(300, body.expiresIn());

    // The serialized browser response carries no image id, object URI or barcode.
    String json = objectMapper.writeValueAsString(body);
    JsonNode tree = objectMapper.readTree(json);
    List<String> keys = new ArrayList<>();
    collectKeys(tree, keys);
    for (String forbidden : List.of("imageId", "image_id", "sourceUrl", "barcode")) {
      assertFalse("response exposes " + forbidden, keys.contains(forbidden));
    }
    assertEquals(3, tree.get("thumbnail").size());
    assertFalse(json.contains(IMAGE_ID));
    assertFalse(json.contains("s3://"));
    assertFalse(json.contains("file://"));
    assertFalse(WsiDeidentification.containsAccession(json));
  }

  @Test
  public void issuesAVersionThreeTokenWithOnlyEncryptedSourceClaims() throws Exception {
    WsiAccessTokenController controller = createAuthenticatedController();
    when(wsiSlideAccessRepository.getSlideSource("study-1", "patient-1", SLIDE_KEY))
        .thenReturn(source());

    WsiSlideAccess body =
        (WsiSlideAccess) controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY).getBody();
    String[] token = body.accessToken().split("\\.");
    String payload = new String(Base64.getUrlDecoder().decode(token[1]), StandardCharsets.UTF_8);
    JsonNode claims = objectMapper.readTree(payload);

    assertEquals("study-1", claims.get("study_id").asText());
    assertEquals(SLIDE_KEY, claims.get("slide_key").asText());
    assertEquals("wsi:read", claims.get("scope").asText());
    assertEquals(3, claims.get("wsi_auth_version").asInt());
    assertEquals(128, claims.get("thumbnail_width").asInt());
    assertEquals(64, claims.get("thumbnail_height").asInt());
    for (String removed :
        List.of(
            "image_id",
            "tile_source",
            "thumbnail_source",
            "tile_source_sha256",
            "thumbnail_source_sha256")) {
      assertFalse("token carries plaintext " + removed, claims.has(removed));
    }
    assertFalse(payload.contains(IMAGE_ID));
    assertFalse(payload.contains("s3://"));
    assertFalse(payload.contains("file://"));

    assertEquals(
        "{\"image_id\":\""
            + IMAGE_ID
            + "\",\"tile_source\":\""
            + SOURCE
            + "\",\"thumbnail_source\":\""
            + THUMBNAIL
            + "\"}",
        WsiClaimEncryptionTest.decrypt(SECRET, SLIDE_KEY, claims.get("enc").asText()));
  }

  @Test
  public void rejectsAnythingButAnOpaqueSlideKey() {
    WsiAccessTokenController controller = createAuthenticatedController();

    for (String invalid :
        new String[] {
          IMAGE_ID,
          "3020726",
          SLIDE_KEY.toUpperCase(),
          SLIDE_KEY + "0",
          SLIDE_KEY.substring(1),
          "",
          null
        }) {
      assertEquals(
          "slide key should be rejected: " + invalid,
          400,
          controller.issueSlideAccess("study-1", "patient-1", invalid).getStatusCode().value());
    }
    verifyNoInteractions(wsiSlideAccessRepository);
  }

  @Test
  public void rejectsABlankPatient() {
    WsiAccessTokenController controller = createAuthenticatedController();

    assertEquals(
        400, controller.issueSlideAccess("study-1", " ", SLIDE_KEY).getStatusCode().value());
    verifyNoInteractions(wsiSlideAccessRepository);
  }

  @Test
  public void requiresLoginUnlessTheLocalBypassIsEnabled() {
    WsiAccessTokenController controller = createAuthenticatedController();
    SecurityContextHolder.clearContext();

    assertEquals(
        401,
        controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY).getStatusCode().value());
    verifyNoInteractions(wsiSlideAccessRepository);
  }

  @Test
  public void returnsNotFoundForAnUnknownSlideKey() {
    WsiAccessTokenController controller = createAuthenticatedController();

    assertEquals(
        404,
        controller
            .issueSlideAccess("study-1", "patient-1", "0".repeat(32))
            .getStatusCode()
            .value());
    verify(wsiSlideAccessRepository).getSlideSource(any(), any(), any());
  }

  @Test
  public void neverSerializesOrPrintsTheServerSideSource() throws Exception {
    WsiSlideSource source = source();
    String json = objectMapper.writeValueAsString(source);

    assertFalse(json.contains(IMAGE_ID));
    assertFalse(json.contains("s3://"));
    assertFalse(source.toString().contains(IMAGE_ID));
    assertFalse(source.toString().contains("s3://"));
    assertTrue(source.toString().contains(SLIDE_KEY));
  }

  private static WsiSlideSource source() {
    WsiTileMetadata metadata =
        new WsiTileMetadata(
            new WsiTileMetadata.Dimensions(2048, 1024),
            1,
            List.of(new WsiTileMetadata.Dimensions(2048, 1024)),
            List.of(1.0),
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
    return new WsiSlideSource(
        SLIDE_KEY, IMAGE_ID, SOURCE, THUMBNAIL, metadata, new WsiThumbnail(128, 64, "image/jpeg"));
  }

  private static void collectKeys(JsonNode node, List<String> keys) {
    node.fieldNames().forEachRemaining(keys::add);
    node.elements().forEachRemaining(child -> collectKeys(child, keys));
  }

  private WsiAccessTokenController createAuthenticatedController() {
    WsiAccessTokenController controller = new WsiAccessTokenController();
    ReflectionTestUtils.setField(controller, "accessTokenSecret", SECRET);
    ReflectionTestUtils.setField(controller, "accessTokenAudience", "cbioportal-wsi");
    ReflectionTestUtils.setField(controller, "accessTokenTtlSeconds", 300);
    ReflectionTestUtils.setField(controller, "wsiSlideAccessRepository", wsiSlideAccessRepository);
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("user", "password", "ROLE_USER");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return controller;
  }
}
