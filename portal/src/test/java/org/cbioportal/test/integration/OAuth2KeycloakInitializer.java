package org.cbioportal.test.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class OAuth2KeycloakInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(OAuth2KeycloakInitializer.class);

    public void initializeImpl(ConfigurableApplicationContext configurableApplicationContext,
                               KeycloakContainer keycloakContainer) {

        try {

            String keycloakUrlForCBioportal = keycloakContainer.getAuthServerUrl();
            String keycloakUrlForBrowser = "http://keycloak:8080/auth";

            TestPropertyValues values = TestPropertyValues.of(
            
                // These urls are from the perspective of cBioPortal application (port on host system).
                String.format(
                    "spring.security.oauth2.client.provider.cbio-idp.issuer=%s/realms/cbio",
                    keycloakUrlForCBioportal),
                String.format(
                    "spring.security.oauth2.client.provider.cbio-idp.token-uri=%s/realms/cbio/protocol/openid-connect/token",
                    keycloakUrlForCBioportal),
                String.format(
                    "spring.security.oauth2.client.provider.cbio-idp.user-info-uri=%s/realms/cbio/protocol/openid-connect/userinfo",
                    keycloakUrlForCBioportal),
                String.format(
                    "spring.security.oauth2.client.provider.cbio-idp.jwk-set-uri=%s/realms/cbio/protocol/openid-connect/certs",
                    keycloakUrlForCBioportal),
                String.format(
                    "dat.oauth2.accessTokenUri=%s/realms/cbio/protocol/openid-connect/token",
                    keycloakUrlForCBioportal),
                String.format("dat.oauth2.jwkUrl=%s/realms/cbio/protocol/openid-connect/certs",
                    keycloakUrlForCBioportal),

            // This url is from the perspective of the browser.
                String.format(
                    "spring.security.oauth2.client.provider.cbio-idp.authorization-uri=%s/realms/cbio/protocol/openid-connect/auth",
                    keycloakUrlForBrowser),
                String.format(
                    "spring.security.oauth2.client.provider.cbio-idp.logout-uri=%s/realms/cbio/protocol/openid-connect/logout",
                    keycloakUrlForBrowser),
                String.format(
                    "dat.oauth2.userAuthorizationUri=%s/realms/cbio/protocol/openid-connect/auth",
                    keycloakUrlForBrowser),
                String.format("dat.oauth2.issuer=%s/realms/cbio", keycloakUrlForBrowser)

            );
            values.applyTo(configurableApplicationContext);

        } catch (Exception e) {
            log.error("Error initializing keycloak container" + e.getMessage());
        }
    }

}
