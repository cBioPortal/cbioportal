package org.cbioportal.application.security.util;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
class ClaimRoleExtractorUtilTest {

  @Test
  void shouldExtractRolesFromSerializedClaims() {
    String claims =
        """
            {
                "roles": ["role1", "role2"]
            }
            """;
    String jwtRolesPath = "roles";
    var result = ClaimRoleExtractorUtil.extractClientRoles(claims, jwtRolesPath);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains("role1"));
    assertTrue(result.contains("role2"));
  }

  @Test
  void shouldExtractRolesFromJsonNodeClaims() {
    ObjectMapper objectMapper = new ObjectMapper();
    String roleString =
        """
                ["role1","role2","role3"]
            """;
    ObjectNode root = objectMapper.createObjectNode();
    root.put("roles", roleString);
    String jwtRolesPath = "roles";

    var result = ClaimRoleExtractorUtil.extractClientRoles(root, jwtRolesPath);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains("role1"));
    assertTrue(result.contains("role2"));
    assertTrue(result.contains("role3"));
  }

  @Test
  void shouldExtractRolesFromString() {
    ObjectMapper objectMapper = new ObjectMapper();
    String groupString =
        """
                ["GROUP_A","GROUP_B","GROUP_C"]
            """;
    ObjectNode root = objectMapper.createObjectNode();
    root.put("groups", groupString);
    String jwtRolesPath = "groups";

    var result = ClaimRoleExtractorUtil.extractClientRoles(root, jwtRolesPath);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains("GROUP_A"));
    assertTrue(result.contains("GROUP_B"));
    assertTrue(result.contains("GROUP_C"));
  }

  @Test
  void shouldExtractRolesFormIdToken() {

    Map<String, Object> claims = new HashMap<>();
    claims.put("aud", "my-app-client-id");
    claims.put("exp", Instant.now());
    claims.put("iat", Instant.now());
    claims.put("groups", List.of("GROUP_X", "GROUP_Y"));

    String jwtRolesPath = "groups";
    var result = ClaimRoleExtractorUtil.extractClientRoles(claims, jwtRolesPath);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains("GROUP_X"));
    assertTrue(result.contains("GROUP_Y"));
  }

  @Test
  void shouldExtractNestedKeycloakRealmRoles() {
    String claims =
        """
            {"realm_access": {"roles": ["portal:PUBLIC", "portal:TEAM_A"]}}
            """;
    var result = ClaimRoleExtractorUtil.extractClientRoles(claims, "realm_access::roles");
    assertEquals(2, result.size());
    assertTrue(result.contains("portal:PUBLIC"));
    assertTrue(result.contains("portal:TEAM_A"));
  }

  @Test
  void shouldReturnNoRolesWhenPathIsMissing() {
    String claims =
        """
            {"realm_access": {"other": ["role1"]}}
            """;
    assertTrue(ClaimRoleExtractorUtil.extractClientRoles(claims, "realm_access::roles").isEmpty());
    assertTrue(
        ClaimRoleExtractorUtil.extractClientRoles(claims, "resource_access::cbioportal::roles")
            .isEmpty());
    assertTrue(ClaimRoleExtractorUtil.extractClientRoles(Map.of("sub", "user"), "roles").isEmpty());
  }

  @Test
  void shouldReturnNoRolesForMalformedClaims() {
    assertTrue(ClaimRoleExtractorUtil.extractClientRoles("not json", "roles").isEmpty());
    assertTrue(
        ClaimRoleExtractorUtil.extractClientRoles("{\"roles\": \"not json\"}", "roles").isEmpty());
  }
}
