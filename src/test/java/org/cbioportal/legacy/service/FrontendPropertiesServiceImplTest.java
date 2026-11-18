package org.cbioportal.legacy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
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

  /** A service whose environment sets only the given property, leaving every other default. */
  private FrontendPropertiesServiceImpl serviceWith(String name, String value) {
    lenient()
        .when(env.getProperty(anyString(), (String) any()))
        .thenAnswer(invocation -> invocation.getArgument(1));
    if (name != null) {
      lenient().when(env.getProperty(name, "false")).thenReturn(value);
    }
    FrontendPropertiesServiceImpl service = new FrontendPropertiesServiceImpl();
    ReflectionTestUtils.setField(service, "env", env);
    service.init();
    return service;
  }

  @Test
  public void oqlWizardIsOffByDefault() {
    assertEquals(
        "false", serviceWith(null, null).getFrontendProperties().get("oql_wizard_enabled"));
  }

  @Test
  public void oqlWizardEnabledReachesTheFrontend() {
    assertEquals(
        "true",
        serviceWith("oql_wizard.enabled", "true")
            .getFrontendProperties()
            .get("oql_wizard_enabled"));
  }
}
