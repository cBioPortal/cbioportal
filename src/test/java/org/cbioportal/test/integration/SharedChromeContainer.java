package org.cbioportal.test.integration;

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;

public class SharedChromeContainer extends BrowserWebDriverContainer {

    public final static String DOWNLOAD_FOLDER = "/tmp/browser_downloads";

    private static BrowserWebDriverContainer<?> container;

    private SharedChromeContainer() {
        super();
    }

    // For test development a VNC viewer can be connected to the selenium container.
    // Make sure to connect to exposed port on host system connected to internal 
    // port 5900 of the browser container. The password is 'secret'.

    public static BrowserWebDriverContainer getInstance() {
        if (container == null) {

            ChromeOptions options = new ChromeOptions();
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("download.default_directory", DOWNLOAD_FOLDER);
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("download.prompt_for_download", "false");
            prefs.put("download.directory_upgrade", "true");
            options.setExperimentalOption("prefs", prefs);

            container = new BrowserWebDriverContainer<>()
                // activate this to record movies of the tests (greate for debugging)
                .withRecordingMode(RECORD_ALL, new File("/home/pnp300"))
                .withNetwork(SharedKeycloakContainer.keycloakNetwork)
                .withAccessToHost(true)
                .withCapabilities(options);
        }
        return container;
    }

}
