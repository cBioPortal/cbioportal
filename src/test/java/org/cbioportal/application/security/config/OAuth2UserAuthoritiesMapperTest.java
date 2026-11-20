package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.test.util.ReflectionTestUtils;

/** Role mapping of OIDC logins under {@code authenticate=oauth2}. */
class OAuth2UserAuthoritiesMapperTest {

  private GrantedAuthoritiesMapper mapper;

  @BeforeEach
  void setUp() {
    OAuth2SecurityConfig config = new OAuth2SecurityConfig();
    ReflectionTestUtils.setField(config, "jwtRolesPath", "resource_access::cbioportal::roles");
    mapper = config.userAuthoritiesMapper();
  }

  @Test
  void rolesFromIdTokenAndUserInfoAreMerged() {
    OidcUserAuthority authority =
        new OidcUserAuthority(
            idToken(Map.of("resource_access", roles("TEAM_A"))),
            new OidcUserInfo(Map.of("sub", "user", "resource_access", roles("TEAM_B"))));

    assertEquals(Set.of("ROLE_TEAM_A", "ROLE_TEAM_B"), map(authority));
  }

  @Test
  void rolesFromIdTokenAloneAreUsed() {
    OidcUserAuthority authority =
        new OidcUserAuthority(idToken(Map.of("resource_access", roles("TEAM_A"))));

    assertEquals(Set.of("ROLE_TEAM_A"), map(authority));
  }

  @Test
  void loginWithoutRoleClaimsHasNoRoles() {
    // Google and Microsoft accounts carry no cbioportal roles claim
    OidcUserAuthority authority = new OidcUserAuthority(idToken(Map.of("email", "a@b.org")));

    assertEquals(Set.of(), map(authority));
  }

  @Test
  void rolesFromOAuth2UserAttributesAreUsed() {
    OAuth2UserAuthority authority =
        new OAuth2UserAuthority(Map.of("sub", "user", "resource_access", roles("TEAM_A")));

    assertEquals(Set.of("ROLE_TEAM_A"), map(authority));
  }

  @Test
  void simpleAuthoritiesArePassedThrough() {
    assertEquals(Set.of("SCOPE_openid"), map(new SimpleGrantedAuthority("SCOPE_openid")));
  }

  private Set<String> map(GrantedAuthority authority) {
    Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(List.of(authority));
    return AuthorityUtils.authorityListToSet(mapped);
  }

  private static Map<String, Object> roles(String... roles) {
    return Map.of("cbioportal", Map.of("roles", List.of(roles)));
  }

  private static OidcIdToken idToken(Map<String, Object> claims) {
    return OidcIdToken.withTokenValue("id-token")
        .subject("user")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .claims(c -> c.putAll(claims))
        .build();
  }
}
