package org.cbioportal.application.security.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

public class Saml2SecurityConfigTest {

  private Saml2SecurityConfig saml2SecurityConfig;

  @Before
  public void setUp() {
    saml2SecurityConfig = new Saml2SecurityConfig();
    ReflectionTestUtils.setField(saml2SecurityConfig, "roleAttributeName", "Role");
    ReflectionTestUtils.setField(saml2SecurityConfig, "emailAttributeName", "mail");
  }

  @Test
  public void testMapAuthoritiesWithRolesAndEmail() {
    Saml2Authentication authentication = mock(Saml2Authentication.class);
    Saml2AuthenticatedPrincipal principal = mock(Saml2AuthenticatedPrincipal.class);

    when(authentication.getPrincipal()).thenReturn(principal);
    when(authentication.getSaml2Response()).thenReturn("dummy-response");
    when(principal.getName()).thenReturn("user@example.com");
    when(principal.getAttribute("Role")).thenReturn(Arrays.asList("ADMIN", "USER"));
    when(principal.getAttribute("mail")).thenReturn(List.of("user@example.com"));

    Saml2Authentication result = saml2SecurityConfig.mapAuthorities(authentication);

    Set<String> authorities =
        result.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertEquals(3, authorities.size());
    assertTrue(authorities.contains("ROLE_ADMIN"));
    assertTrue(authorities.contains("ROLE_USER"));
    assertTrue(authorities.contains("user@example.com"));
  }

  @Test
  public void testMapAuthoritiesWithRolesAndDefaultEmail() {
    // Test fallback to principal.getName() when email attribute is missing
    Saml2Authentication authentication = mock(Saml2Authentication.class);
    Saml2AuthenticatedPrincipal principal = mock(Saml2AuthenticatedPrincipal.class);

    when(authentication.getPrincipal()).thenReturn(principal);
    when(authentication.getSaml2Response()).thenReturn("dummy-response");
    when(principal.getName()).thenReturn("user_name");
    when(principal.getAttribute("Role")).thenReturn(Arrays.asList("USER"));
    when(principal.getAttribute("mail")).thenReturn(null);

    Saml2Authentication result = saml2SecurityConfig.mapAuthorities(authentication);

    Set<String> authorities =
        result.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertEquals(2, authorities.size());
    assertTrue(authorities.contains("ROLE_USER"));
    assertTrue(authorities.contains("user_name"));
  }
}
