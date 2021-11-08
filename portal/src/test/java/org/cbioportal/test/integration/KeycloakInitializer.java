package org.cbioportal.test.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class KeycloakInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Log log = LogFactory.getLog(KeycloakInitializer.class);

    public void initializeImpl(ConfigurableApplicationContext configurableApplicationContext,
                               KeycloakContainer keycloakContainer) {

        try {

            String keycloakUrlForCBioportal = keycloakContainer.getAuthServerUrl();
            String keycloakUrlForBrowser = String.format("http://host.testcontainers.internal:%d/auth",
                keycloakContainer.getHttpPort());

            String samlIdpMetadata =
                keycloakContainer.execInContainer("curl",
                        "http://localhost:8080/auth/realms/cbio/protocol/saml/descriptor")
                    .getStdout()
                    .replaceAll("http://localhost:8080/auth", keycloakUrlForBrowser);
            String samlIdpMetadataPath = tempFile(samlIdpMetadata);

            TestPropertyValues values = TestPropertyValues.of(
                String.format("saml.idp.metadata.location=file:%s", samlIdpMetadataPath),
                // Should match the id in the generated idp metadata xml (samlIdpMetadata)
                String.format("saml.idp.metadata.entityid=%s/realms/cbio",
                    keycloakUrlForBrowser),

                // These urls are from the perspective of cBioPortal application (port on host system)
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

    private static String tempFile(String samlIdpMetadata) throws IOException {
        String absolutePath =
            File.createTempFile("temp-idp-metadata", Long.toString(System.nanoTime()))
                .getAbsolutePath();
        BufferedWriter bw = new BufferedWriter(new FileWriter(absolutePath));
        bw.write(samlIdpMetadata);
        bw.close();
        return absolutePath;
    }
}
