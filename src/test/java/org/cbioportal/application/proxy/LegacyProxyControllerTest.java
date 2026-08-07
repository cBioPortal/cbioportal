package org.cbioportal.application.proxy;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for {@link LegacyProxyController} verifying OncoKB guard and unknown-path behavior.
 *
 * <p>These tests cover error paths that are triggered before any external HTTP call is made, so no
 * network connection or WireMock setup is required.
 */
public class LegacyProxyControllerTest {

  private MockMvc mockMvc;
  private LegacyProxyController controller;

  @Before
  public void setUp() {
    controller = new LegacyProxyController();
    controller.setBitlyURL("http://test-bitly.com");
    controller.setSessionServiceURL("http://test-session.com");
  }

  /**
   * When the OncoKB service is disabled (show.oncokb=false), requests to any path starting with
   * "oncokb" should be rejected with HTTP 403.
   */
  @Test
  public void oncokbPath_whenServiceDisabled_returns403() throws Exception {
    controller.setEnableOncokb(false);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/proxy/oncokb"))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
  }

  /**
   * Requests to an unknown/unmapped path result in an empty URL being passed to RestTemplate.
   * RestTemplate throws when given an empty/relative URI, which is caught and returned as HTTP 503.
   */
  @Test
  public void unknownPath_returns503() throws Exception {
    controller.setEnableOncokb(true);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/proxy/unknownService"))
        .andExpect(MockMvcResultMatchers.status().isServiceUnavailable());
  }

  /**
   * The oncokb path check is case-insensitive. A path like "ONCOKB" should also be blocked when the
   * service is disabled.
   */
  @Test
  public void oncokbPath_caseInsensitive_whenServiceDisabled_returns403() throws Exception {
    controller.setEnableOncokb(false);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/proxy/ONCOKB"))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
  }
}
