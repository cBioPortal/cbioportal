package org.cbioportal.legacy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FrontendPropertiesServiceImplTest {

  @Mock private Environment env;

  @Test
  public void parseUrlShouldAppendTrailingSlashWhenMissing() {
    assertEquals(
        "https://frontend.cbioportal.org/",
        FrontendPropertiesServiceImpl.parseUrl("https://frontend.cbioportal.org"));
  }

  @Test
  public void parseUrlShouldTrimWhitespaceAndPreserveTrailingSlash() {
    assertEquals(
        "https://frontend.cbioportal.org/",
        FrontendPropertiesServiceImpl.parseUrl("  https://frontend.cbioportal.org/  "));
  }

  @Test
  public void parseUrlShouldReturnEmptyStringForNullOrEmpty() {
    assertEquals("", FrontendPropertiesServiceImpl.parseUrl(null));
    assertEquals("", FrontendPropertiesServiceImpl.parseUrl(""));
  }

  @Test
  public void getFrontendUrlShouldNormalizePropertyValueWhenNoRuntimeOverride() {
    FrontendPropertiesServiceImpl service = new FrontendPropertiesServiceImpl();
    ReflectionTestUtils.setField(service, "env", env);
    when(env.getProperty("frontend.url.runtime", "")).thenReturn("");

    assertEquals(
        "https://frontend.cbioportal.org/",
        service.getFrontendUrl("https://frontend.cbioportal.org"));
  }

  @Test
  public void getFrontendUrlShouldUseRuntimeOverrideAndNormalizeIt() throws IOException {
    Path runtimeFile = Files.createTempFile("frontend-url-runtime", ".txt");
    try {
      Files.writeString(runtimeFile, "https://runtime.cbioportal.org\n");

      FrontendPropertiesServiceImpl service = new FrontendPropertiesServiceImpl();
      ReflectionTestUtils.setField(service, "env", env);
      when(env.getProperty("frontend.url.runtime", "")).thenReturn(runtimeFile.toString());

      assertEquals(
          "https://runtime.cbioportal.org/",
          service.getFrontendUrl("https://frontend.cbioportal.org"));
    } finally {
      Files.deleteIfExists(runtimeFile);
    }
  }

  @Test
  public void getSkinHideDownloadControlsValueShouldMapBooleanValuesToFrontendValues() {
    assertEquals("hide", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue("true"));
    assertEquals("hide", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue(" TRUE "));
    assertEquals("show", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue("false"));
    assertEquals("show", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue(" FALSE "));
  }

  @Test
  public void getSkinHideDownloadControlsValueShouldPreserveFrontendEnumValues() {
    assertEquals("show", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue("show"));
    assertEquals("data", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue("data"));
    assertEquals("hide", FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue("hide"));
  }

  @Test
  public void getSkinHideDownloadControlsValueShouldPreserveNull() {
    assertNull(FrontendPropertiesServiceImpl.getSkinHideDownloadControlsValue(null));
  }

  @Test
  public void annotationFeatureFlagsShouldUseCanonicalPropertyNames() {
    MockEnvironment environment =
        new MockEnvironment()
            .withProperty("feature.annotation.hotspot", "true")
            .withProperty("feature.annotation.oncokb", "true")
            .withProperty("feature.annotation.civic", "false")
            .withProperty("feature.annotation.genomenexus", "false");

    FrontendPropertiesServiceImpl service = initializeService(environment);

    assertEquals(
        "true",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_hotspot));
    assertEquals(
        "true",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_oncokb));
    assertEquals(
        "false",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_civic));
    assertEquals(
        "false",
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.show_genomenexus));
  }

  @Test
  public void annotationFeatureFlagsShouldFallBackToLegacyPropertyNames() {
    MockEnvironment environment =
        new MockEnvironment()
            .withProperty("show.hotspot", "true")
            .withProperty("show.oncokb", "true")
            .withProperty("show.civic", "false")
            .withProperty("show.genomenexus", "false");

    FrontendPropertiesServiceImpl service = initializeService(environment);

    assertEquals(
        "true",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_hotspot));
    assertEquals(
        "true",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_oncokb));
    assertEquals(
        "false",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_civic));
    assertEquals(
        "false",
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.show_genomenexus));
  }

  @Test
  public void canonicalAnnotationFeatureFlagShouldTakePrecedenceOverLegacyProperty() {
    MockEnvironment environment =
        new MockEnvironment()
            .withProperty("feature.annotation.oncokb", "false")
            .withProperty("show.oncokb", "true");

    FrontendPropertiesServiceImpl service = initializeService(environment);

    assertEquals(
        "false",
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_oncokb));
  }

  @Test
  public void annotationFeatureFlagsShouldRemainUnsetByDefault() {
    FrontendPropertiesServiceImpl service = initializeService(new MockEnvironment());

    assertNull(
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_hotspot));
    assertNull(
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_oncokb));
    assertNull(
        service.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.show_civic));
    assertNull(
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.show_genomenexus));
  }

  private FrontendPropertiesServiceImpl initializeService(Environment environment) {
    FrontendPropertiesServiceImpl service = new FrontendPropertiesServiceImpl();
    ReflectionTestUtils.setField(service, "env", environment);
    service.init();
    return service;
  }
}
