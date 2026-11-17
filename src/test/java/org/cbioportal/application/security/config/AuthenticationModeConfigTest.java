package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthenticationModeConfigTest {

  @ParameterizedTest
  @ValueSource(strings = {"false", "oauth2", "optional_oauth2", "saml", "saml_plus_basic"})
  void supportedModesAreAccepted(String mode) {
    assertDoesNotThrow(() -> new AuthenticationModeConfig(mode));
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "noauthsessionservice", "OAUTH2", "Saml", "", " saml"})
  void otherValuesStopStartup(String mode) {
    assertThrows(IllegalStateException.class, () -> new AuthenticationModeConfig(mode));
  }
}
