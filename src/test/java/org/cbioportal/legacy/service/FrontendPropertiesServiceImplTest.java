package org.cbioportal.legacy.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
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
  public void initShouldExposeGenericAssayDisplayTextToFrontendConfig() {
    FrontendPropertiesServiceImpl service = new FrontendPropertiesServiceImpl();
    ReflectionTestUtils.setField(service, "env", env);
    when(env.getProperty(anyString(), nullable(String.class)))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              String defaultValue = invocation.getArgument(1);
              if ("generic_assay_display_text".equals(key)) {
                return "LOH_HLA:HLA LOH,MUTATIONAL_SIGNATURE:Mutational Signature";
              }
              return defaultValue;
            });

    service.init();

    assertEquals(
        "LOH_HLA:HLA LOH,MUTATIONAL_SIGNATURE:Mutational Signature",
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.generic_assay_display_text));
  }
}
