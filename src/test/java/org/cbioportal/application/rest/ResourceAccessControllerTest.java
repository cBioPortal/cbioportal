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
import org.cbioportal.domain.resource.ResourceAccess;
import org.cbioportal.domain.resource.ResourceServing;
import org.cbioportal.domain.resource.repository.ResourceServingRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public class ResourceAccessControllerTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";
  private static final String KEY = "2351e12d4955";
  // A well-formed seal (contract test key); opaque to cBioPortal.
  private static final String SEALED_SOURCE =
      "AQEBAQEBAQEBAQEBMc_tL9dUso8ZSDg1JZIFeeclGKwdmpgGohT4JR793-tUE3oED7qLjtQhP_usT08BCC6kpTQl6az5"
          + "wJ6AMvgEt1zCZgqnjk2gcXeEODZFCRVg80fRaEk2FQaFx_eROJgD6ajaxtdJ1OJw4aKN06MJD0Hc6zBDxi77WsoS";

  private final ResourceServingRepository repository = mock(ResourceServingRepository.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void returnsOnlyTheCapability() throws Exception {
    ResourceAccessController controller = controller();
    login("user");
    when(repository.getServing("study-1", "WSI_SAMPLE", KEY, "patient-1")).thenReturn(serving());

    ResponseEntity<?> response = controller.issueAccess("study-1", "WSI_SAMPLE", KEY, "patient-1");

    assertEquals(200, response.getStatusCode().value());
    assertEquals("private, no-store", response.getHeaders().getCacheControl());
    ResourceAccess body = (ResourceAccess) response.getBody();
    assertNotNull(body);
    assertEquals("WSI_SAMPLE", body.resourceId());
    assertEquals(KEY, body.key());
    assertEquals("Bearer", body.tokenType());
    assertEquals(300, body.expiresIn());
    List<String> keys = new ArrayList<>();
    objectMapper
        .readTree(objectMapper.writeValueAsString(body))
        .fieldNames()
        .forEachRemaining(keys::add);
    assertEquals(List.of("resourceId", "key", "accessToken", "tokenType", "expiresIn"), keys);
  }

  @Test
  public void issuesATokenTheTileServerAccepts() throws Exception {
    ResourceAccessController controller = controller();
    login("user");
    when(repository.getServing("study-1", "WSI_SAMPLE", KEY, "patient-1")).thenReturn(serving());

    ResourceAccess body =
        (ResourceAccess)
            controller.issueAccess("study-1", "WSI_SAMPLE", KEY, "patient-1").getBody();
    String[] token = body.accessToken().split("\\.");
    JsonNode header =
        objectMapper.readTree(
            new String(Base64.getUrlDecoder().decode(token[0]), StandardCharsets.UTF_8));
    JsonNode claims =
        objectMapper.readTree(
            new String(Base64.getUrlDecoder().decode(token[1]), StandardCharsets.UTF_8));

    assertEquals("HS256", header.get("alg").asText());
    assertEquals("JWT", header.get("typ").asText());
    assertEquals("user", claims.get("sub").asText());
    assertEquals("cbioportal-wsi", claims.get("aud").asText());
    assertEquals("wsi:read", claims.get("scope").asText());
    assertEquals("study-1", claims.get("study_id").asText());
    assertEquals("WSI_SAMPLE", claims.get("resource_id").asText());
    assertEquals(KEY, claims.get("slide_key").asText());
    assertEquals(4, claims.get("wsi_auth_version").asInt());
    assertEquals(128, claims.get("thumbnail_width").asInt());
    assertEquals(64, claims.get("thumbnail_height").asInt());
    assertEquals(SEALED_SOURCE, claims.get("enc").asText());
    assertEquals(300, claims.get("exp").asLong() - claims.get("iat").asLong());
    for (String removed : List.of("image_id", "tile_source", "thumbnail_source")) {
      assertFalse("token carries " + removed, claims.has(removed));
    }
  }

  @Test
  public void signsTheTokenWithTheAccessTokenSecret() throws Exception {
    ResourceAccessController controller = controller();
    login("user");
    when(repository.getServing("study-1", "WSI_SAMPLE", KEY, "patient-1")).thenReturn(serving());

    ResourceAccess body =
        (ResourceAccess)
            controller.issueAccess("study-1", "WSI_SAMPLE", KEY, "patient-1").getBody();
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
  public void issuesToAnAnonymousReaderOfTheStudy() throws Exception {
    // Study read permission is checked by @PreAuthorize; with no login the subject is anonymous.
    ResourceAccessController controller = controller();
    when(repository.getServing("study-1", "WSI_SAMPLE", KEY, "patient-1")).thenReturn(serving());

    ResponseEntity<?> response = controller.issueAccess("study-1", "WSI_SAMPLE", KEY, "patient-1");

    assertEquals(200, response.getStatusCode().value());
    String payload =
        new String(
            Base64.getUrlDecoder()
                .decode(((ResourceAccess) response.getBody()).accessToken().split("\\.")[1]),
            StandardCharsets.UTF_8);
    assertEquals("anonymousUser", objectMapper.readTree(payload).get("sub").asText());
  }

  @Test
  public void refusesToIssueWithoutAStrongSecret() {
    ResourceAccessController controller = controller();
    ReflectionTestUtils.setField(controller, "accessTokenSecret", "short");

    assertEquals(
        503,
        controller.issueAccess("study-1", "WSI_SAMPLE", KEY, "patient-1").getStatusCode().value());
    verifyNoInteractions(repository);
  }

  @Test
  public void rejectsMalformedRequestsBeforeLookingUp() {
    ResourceAccessController controller = controller();

    for (String key : new String[] {"", "a key", "a/b", "a.b", "..", "x".repeat(129), null}) {
      assertEquals(
          "key should be rejected: " + key,
          400,
          controller
              .issueAccess("study-1", "WSI_SAMPLE", key, "patient-1")
              .getStatusCode()
              .value());
    }
    for (String resourceId : new String[] {"", "WSI-SAMPLE", "../x"}) {
      assertEquals(
          "resource id should be rejected: " + resourceId,
          400,
          controller.issueAccess("study-1", resourceId, KEY, "patient-1").getStatusCode().value());
    }
    // The patient keeps the lookup to that patient's rows, so it is required.
    for (String patientId : new String[] {null, "", " "}) {
      assertEquals(
          "patient should be rejected: " + patientId,
          400,
          controller.issueAccess("study-1", "WSI_SAMPLE", KEY, patientId).getStatusCode().value());
    }
    verifyNoInteractions(repository);
  }

  @Test
  public void returnsNotFoundForARowWithoutServingData() {
    ResourceAccessController controller = controller();

    assertEquals(
        404,
        controller
            .issueAccess("study-1", "WSI_SAMPLE", "0".repeat(12), "patient-1")
            .getStatusCode()
            .value());
    verify(repository).getServing(any(), any(), any(), any());
  }

  private static ResourceServing serving() {
    return new ResourceServing("WSI_SAMPLE", KEY, SEALED_SOURCE, 128, 64);
  }

  private static void login(String name) {
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken(name, "password", "ROLE_USER");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private ResourceAccessController controller() {
    ResourceAccessController controller = new ResourceAccessController(repository);
    ReflectionTestUtils.setField(controller, "accessTokenSecret", SECRET);
    ReflectionTestUtils.setField(controller, "accessTokenAudience", "cbioportal-wsi");
    ReflectionTestUtils.setField(controller, "accessTokenTtlSeconds", 300);
    return controller;
  }
}
