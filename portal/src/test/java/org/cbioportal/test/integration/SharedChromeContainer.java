package org.cbioportal.test.integration;

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;


import java.io.File;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;

public class SharedChromeContainer extends BrowserWebDriverContainer {

    private static BrowserWebDriverContainer<?> container;

    private SharedChromeContainer() {
        super();
    }

    public static BrowserWebDriverContainer getInstance() {
        if (container == null) {
            container = new BrowserWebDriverContainer<>()
                .withRecordingMode(RECORD_ALL, new File("/home/pnp300"))
                .withNetwork(SharedKeycloakContainer.keycloakNetwork)
                .withAccessToHost(true)
                .withCapabilities(new ChromeOptions());
        }
        return container;
    }

}
