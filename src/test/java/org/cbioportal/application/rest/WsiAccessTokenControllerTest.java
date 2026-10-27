package org.cbioportal.application.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public class WsiAccessTokenControllerTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";
  private static final String SLIDE_KEY = "2351e12d4955";
  // A well-formed seal (contract test key); opaque to cBioPortal.
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
  public void returnsOnlyTheCapability() throws Exception {
    WsiAccessTokenController controller = createAuthenticatedController();
    when(wsiSlideAccessRepository.getSlideSource("study-1", "patient-1", SLIDE_KEY))
        .thenReturn(source());

    ResponseEntity<?> response = controller.issueSlideAccess("study-1", "patient-1", SLIDE_KEY);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("private, no-store", response.getHeaders().getCacheControl());
    WsiSlideAccess body = (WsiSlideAccess) response.getBody();
    assertNotNull(body);
    assertEquals(SLIDE_KEY, body.slideKey());
    assertEquals("Bearer", body.tokenType());
    assertEquals(300, body.expiresIn());
    List<String> keys = new ArrayList<>();
    objectMapper
        .readTree(objectMapper.writeValueAsString(body))
        .fieldNames()
        .forEachRemaining(keys::add);
    assertEquals(List.of("slideKey", "accessToken", "tokenType", "expiresIn"), keys);
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
          // The retired 32-character form.
          SLIDE_KEY + "a1b2c3d4e5f6a7b8c9d0",
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
            .issueSlideAccess("study-1", "patient-1", "0".repeat(12))
            .getStatusCode()
            .value());
    verify(wsiSlideAccessRepository).getSlideSource(any(), any(), any());
  }

  private static WsiSlideSource source() {
    return new WsiSlideSource(SLIDE_KEY, SEALED_SOURCE, 128, 64);
  }

  private WsiAccessTokenController createAuthenticatedController() {
    WsiAccessTokenController controller = new WsiAccessTokenController(wsiSlideAccessRepository);
    ReflectionTestUtils.setField(controller, "accessTokenSecret", SECRET);
    ReflectionTestUtils.setField(controller, "accessTokenAudience", "cbioportal-wsi");
    ReflectionTestUtils.setField(controller, "accessTokenTtlSeconds", 300);
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("user", "password", "ROLE_USER");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return controller;
  }
}
