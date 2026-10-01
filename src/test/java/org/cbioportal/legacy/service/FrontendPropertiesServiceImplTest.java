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
  public void studyAvailabilityEnabledShouldDefaultToFalse() {
    FrontendPropertiesServiceImpl service = initService(new MockEnvironment());

    assertEquals(
        "false",
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.study_availability_enabled));
  }

  @Test
  public void studyAvailabilityEnabledShouldExposeConfiguredValue() {
    FrontendPropertiesServiceImpl service =
        initService(new MockEnvironment().withProperty("study_availability.enabled", "true"));

    assertEquals("true", service.getFrontendProperties().get("study_availability_enabled"));
  }

  private FrontendPropertiesServiceImpl initService(Environment environment) {
    FrontendPropertiesServiceImpl service = new FrontendPropertiesServiceImpl();
    ReflectionTestUtils.setField(service, "env", environment);
    service.init();
    return service;
  }
}
