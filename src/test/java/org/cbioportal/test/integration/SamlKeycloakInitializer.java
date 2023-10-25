package org.cbioportal.test.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class SamlKeycloakInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(SamlKeycloakInitializer.class);

    public void initializeImpl(ConfigurableApplicationContext configurableApplicationContext,
                               KeycloakContainer keycloakContainer) {

        try {

            String keycloakUrlForCBioportal = keycloakContainer.getAuthServerUrl();
            String keycloakUrlForBrowser = "http://keycloak:8080/auth";

            TestPropertyValues values = TestPropertyValues.of(
                // Should match the id in the generated idp metadata xml (samlIdpMetadata)
                String.format("spring.security.saml2.relyingparty.registration.cbio-idp.identityprovider.entity-id=%s/realms/cbio",
                    keycloakUrlForBrowser),

                // These urls are from the perspective of cBioPortal application (port on host system)
                String.format(
                    "spring.security.saml2.relyingparty.registration.cbio-idp.identityprovider.metadata-uri=%s/realms/cbio/protocol/saml/descriptor",
                    keycloakUrlForCBioportal),
                String.format(
                    "dat.oauth2.accessTokenUri=%s/realms/cbio/protocol/openid-connect/token",
                    keycloakUrlForCBioportal),
                String.format("dat.oauth2.jwkUrl=%s/realms/cbio/protocol/openid-connect/certs",
                    keycloakUrlForCBioportal),

                // This url is from the perspective of the browser
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
