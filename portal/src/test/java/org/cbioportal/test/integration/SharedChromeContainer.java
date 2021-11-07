package org.cbioportal.test.integration;

import org.openqa.selenium.chrome.ChromeOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;

public class SharedChromeContainer extends BrowserWebDriverContainer {

    private static BrowserWebDriverContainer<?> container;

    private SharedChromeContainer() {
        super();
    }

    public static BrowserWebDriverContainer getInstance() {
        if (container == null) {
            container = new BrowserWebDriverContainer<>()
                // activate this to record movies of the tests (greate for debugging)
                // .withRecordingMode(RECORD_ALL, new File("/home/pnp300"))
                .withNetwork(SharedKeycloakContainer.keycloakNetwork)
                .withAccessToHost(true)
                .withCapabilities(new ChromeOptions());
        }
        return container;
    }

}
