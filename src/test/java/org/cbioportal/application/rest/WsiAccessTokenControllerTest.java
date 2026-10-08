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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
  // A genuine seal for SLIDE_KEY under the contract test key; opaque to cBioPortal.
  private static final String SEALED_SOURCE =
      "AQEBAQEBAQEBAQEBMc_tL9dUso8ZSDg1JZIFeeclGKwdmpgGohT4JR793-tUE3oED7qLjtQhP_usT08BCC6kpTQl6az5"
          + "wJ6AMvgEt1zCZgqnjk2gcXeEODZFCRVg80fRaEk2FQaFx_eROJgD6ajaxtdJ1OJw4aKN06MJD0Hc6zBDxi77WsoS";

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

    // The serialized browser response carries no image id, object URI or barcode, and the sealed
    // source appears only inside the signed capability.
    String json = objectMapper.writeValueAsString(body);
    JsonNode tree = objectMapper.readTree(json);
    List<String> keys = new ArrayList<>();
    collectKeys(tree, keys);
    for (String forbidden :
        List.of(
            "imageId",
            "image_id",
            "sourceUrl",
            "barcode",
            "sealedSource",
            "sealed_source",
            "enc")) {
      assertFalse("response exposes " + forbidden, keys.contains(forbidden));
    }
    assertEquals(3, tree.get("thumbnail").size());
    String withoutToken = json.replace(body.accessToken(), "");
    assertFalse(withoutToken.contains(SEALED_SOURCE));
    assertFalse(json.contains("s3://"));
    assertFalse(json.contains("file://"));
  }

  @Test
  public void issuesAVersionFourTokenThatForwardsTheSealedSource() throws Exception {
    WsiAccessTokenController controller = createAuthenticatedController();
    when(wsiSlideAccessRepository.getSlideSource("study-1", "patient-1", SLIDE_KEY))
        .thenReturn(source());

    WsiSlideAccess body =
        (WsiSlideAccess) controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY).getBody();
    String[] token = body.accessToken().split("\\.");
    JsonNode header =
        objectMapper.readTree(
            new String(Base64.getUrlDecoder().decode(token[0]), StandardCharsets.UTF_8));
    String payload = new String(Base64.getUrlDecoder().decode(token[1]), StandardCharsets.UTF_8);
    JsonNode claims = objectMapper.readTree(payload);

    assertEquals("HS256", header.get("alg").asText());
    assertEquals("user", claims.get("sub").asText());
    assertEquals("cbioportal-wsi", claims.get("aud").asText());
    assertEquals("study-1", claims.get("study_id").asText());
    assertEquals(SLIDE_KEY, claims.get("slide_key").asText());
    assertEquals("wsi:read", claims.get("scope").asText());
    assertEquals(4, claims.get("wsi_auth_version").asInt());
    assertEquals(128, claims.get("thumbnail_width").asInt());
    assertEquals(64, claims.get("thumbnail_height").asInt());
    assertEquals(300, claims.get("exp").asLong() - claims.get("iat").asLong());
    assertEquals(SEALED_SOURCE, claims.get("enc").asText());
    for (String removed :
        List.of(
            "image_id",
            "tile_source",
            "thumbnail_source",
            "tile_source_sha256",
            "thumbnail_source_sha256",
            "sealed_source")) {
      assertFalse("token carries " + removed, claims.has(removed));
    }
    assertFalse(payload.contains("s3://"));
    assertFalse(payload.contains("file://"));
  }

  @Test
  public void signsTheTokenWithTheAccessTokenSecret() throws Exception {
    WsiAccessTokenController controller = createAuthenticatedController();
    when(wsiSlideAccessRepository.getSlideSource("study-1", "patient-1", SLIDE_KEY))
        .thenReturn(source());

    WsiSlideAccess body =
        (WsiSlideAccess) controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY).getBody();
    String[] token = body.accessToken().split("\\.");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String expected =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                mac.doFinal((token[0] + "." + token[1]).getBytes(StandardCharsets.US_ASCII)));

    assertEquals(expected, token[2]);
  }

  @Test
  public void refusesToIssueWithoutAStrongSecret() {
    WsiAccessTokenController controller = createAuthenticatedController();
    ReflectionTestUtils.setField(controller, "accessTokenSecret", "short");

    assertEquals(
        503,
        controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY).getStatusCode().value());
    verifyNoInteractions(wsiSlideAccessRepository);
  }

  @Test
  public void rejectsAnythingButAnOpaqueSlideKey() {
    WsiAccessTokenController controller = createAuthenticatedController();

    for (String invalid :
        new String[] {
          "syn-img-0001",
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
  public void neverSerializesOrPrintsTheSealedSource() throws Exception {
    WsiSlideSource source = source();
    String json = objectMapper.writeValueAsString(source);

    assertFalse(json.contains(SEALED_SOURCE));
    assertFalse(json.contains("sealedSource"));
    assertFalse(source.toString().contains(SEALED_SOURCE));
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
        SLIDE_KEY, SEALED_SOURCE, metadata, new WsiThumbnail(128, 64, "image/jpeg"));
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
