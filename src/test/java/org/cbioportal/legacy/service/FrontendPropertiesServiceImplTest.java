package org.cbioportal.legacy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

public class FrontendPropertiesServiceImplTest {

  private FrontendPropertiesServiceImpl service;
  private Environment env;

  @BeforeEach
  public void setUp() {
    service = new FrontendPropertiesServiceImpl();
    env = mock(Environment.class);
    when(env.getProperty(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
        .thenAnswer(
            invocation -> invocation.getArgument(1)); // Return default value passed to getProperty
    when(env.getProperty(org.mockito.ArgumentMatchers.anyString())).thenReturn("");

    ReflectionTestUtils.setField(service, "env", env);
  }

  @Test
  public void testSkinHideDownloadControls_False() {
    // User sets "false" (meaning: "don't hide controls" -> "show")
    when(env.getProperty("skin.hide_download_controls", "show")).thenReturn("false");

    service.init();

    String value =
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.skin_hide_download_controls);
    // Expect "show" but currently it returns "false"
    assertEquals("show", value, "Property 'false' should be mapped to 'show'");
  }

  @Test
  public void testSkinHideDownloadControls_True() {
    // User sets "true" (meaning: "hide controls" -> "hide")
    when(env.getProperty("skin.hide_download_controls", "show")).thenReturn("true");

    service.init();

    String value =
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.skin_hide_download_controls);
    // Expect "hide" but currently it returns "true"
    assertEquals("hide", value, "Property 'true' should be mapped to 'hide'");
  }

  @Test
  public void testSkinHideDownloadControls_Data() {
    // User sets "data" (hide data only)
    when(env.getProperty("skin.hide_download_controls", "show")).thenReturn("data");

    service.init();

    String value =
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.skin_hide_download_controls);
    assertEquals("data", value);
  }

  @Test
  public void testSkinHideDownloadControls_Show() {
    // User sets "show" (explicitly show)
    when(env.getProperty("skin.hide_download_controls", "show")).thenReturn("show");

    service.init();

    String value =
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.skin_hide_download_controls);
    assertEquals("show", value);
  }

  @Test
  public void testSkinHideDownloadControls_Hide() {
    // User sets "hide" (explicitly hide)
    when(env.getProperty("skin.hide_download_controls", "show")).thenReturn("hide");

    service.init();

    String value =
        service.getFrontendProperty(
            FrontendPropertiesServiceImpl.FrontendProperty.skin_hide_download_controls);
    assertEquals("hide", value);
  }
}
