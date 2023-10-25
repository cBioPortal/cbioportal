package org.cbioportal.test.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.containers.Network;

public class SharedKeycloakContainer extends KeycloakContainer {

    private static final String IMAGE_VERSION = "jboss/keycloak:15.0.2";
    private static String kcAdminName = "admin";
    private static String kcAdminPassword = "admin";
    public static final Network keycloakNetwork = Network.newNetwork();
    private static KeycloakContainer container;

    private SharedKeycloakContainer() {
        super(IMAGE_VERSION);
    }

    public static KeycloakContainer getInstance() {
        if (container == null) {
            container = new KeycloakContainer()
                .withRealmImportFile("security/keycloak-configuration-generated.json")
                .withAdminUsername(kcAdminName)
                .withAdminPassword(kcAdminPassword)
                .withNetwork(keycloakNetwork)
                .withNetworkAliases("keycloak")
                // Needed because cBioPortal and Chrome use differnt urls to Keycloak container
                // Causes mismatch of 'issuer' field in JWT.
                // See: https://stackoverflow.com/a/65848717/11651683
                .withEnv("KEYCLOAK_FRONTEND_URL", "http://keycloak:8080/auth");
        }
        return container;
    }

}
