package org.cbioportal.test.integration.security;

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
            String samlIdpMetadata =
                keycloakContainer.execInContainer("curl",
                        String.format("http://localhost:8080/auth/realms/cbio/protocol/saml/descriptor",
                            keycloakContainer.getAuthServerUrl()))
                    .getStdout()
                    .replaceAll("http://localhost", "http://keycloak");
            String samlIdpMetadataPath = tempFile(samlIdpMetadata);
            TestPropertyValues values = TestPropertyValues.of(
                String.format("saml.idp.metadata.location=file:%s", samlIdpMetadataPath),
                String.format("dat.oauth2.accessTokenUri=%s/auth/realms/cbio/token",
                    "http://keycloak:8080"),
//                    keycloakContainer.getAuthServerUrl()),
                String.format("dat.oauth2.userAuthorizationUri=%s/auth/realms/cbio/auth",
                    "http://keycloak:8080"),
//                    keycloakContainer.getAuthServerUrl()),
                String.format("dat.oauth2.jwkUrl=%s/auth/realms/cbio/jwkUrl",
                    "http://keycloak:8080")
//                    keycloakContainer.getAuthServerUrl())
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
