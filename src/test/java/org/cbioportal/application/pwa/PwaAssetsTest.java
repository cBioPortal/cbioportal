package org.cbioportal.application.pwa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.Test;

/**
 * Guards the files that make cBioPortal installable as a Progressive Web App: the manifest must be
 * valid, every icon it declares must exist with the declared size, and the page template must link
 * the manifest and register the service worker.
 */
public class PwaAssetsTest {

  private static InputStream resource(String path) {
    InputStream in = PwaAssetsTest.class.getResourceAsStream(path);
    assertNotNull("Missing classpath resource " + path, in);
    return in;
  }

  @Test
  public void manifestIsInstallableAndItsIconsExist() throws Exception {
    JsonNode manifest = new ObjectMapper().readTree(resource("/webapp/pwa/manifest.json"));

    assertEquals("standalone", manifest.get("display").asText());
    assertTrue(manifest.hasNonNull("name"));
    assertTrue(manifest.hasNonNull("start_url"));

    boolean has192 = false;
    boolean has512 = false;
    for (JsonNode icon : manifest.get("icons")) {
      String sizes = icon.get("sizes").asText();
      // Icon paths are relative to the manifest, which is served from the application root.
      BufferedImage image = ImageIO.read(resource("/webapp/" + icon.get("src").asText()));
      assertEquals(sizes, image.getWidth() + "x" + image.getHeight());
      has192 |= sizes.equals("192x192");
      has512 |= sizes.equals("512x512");
    }
    assertTrue("Chrome requires a 192x192 icon", has192);
    assertTrue("Chrome requires a 512x512 icon", has512);
  }

  @Test
  public void serviceWorkerExists() throws Exception {
    try (InputStream in = resource("/webapp/pwa/service-worker.js")) {
      assertTrue(
          new String(in.readAllBytes(), StandardCharsets.UTF_8).contains("addEventListener"));
    }
  }

  @Test
  public void indexPageLinksManifestAndRegistersServiceWorker() throws Exception {
    try (InputStream in = resource("/templates/index.html")) {
      String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(html.contains("rel=\"manifest\""));
      assertTrue(html.contains("serviceWorker"));
    }
  }
}
