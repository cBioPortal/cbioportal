package org.cbioportal.application.proxy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for {@link ProxyController} verifying OncoKB proxy guard behavior.
 *
 * <p>These tests cover the error paths that are triggered before any external HTTP call is made, so
 * no network connection or WireMock setup is required.
 */
public class ProxyControllerTest {

  private static final String VALID_USER_AGREEMENT =
      "I/We do NOT use this obfuscated proxy to programmatically obtain private OncoKB data. "
          + "I/We know that I/we should get a valid data access token by registering at "
          + "https://www.oncokb.org/account/register.";

  private static final String ENCODED_ONCOKB_PATH =
      "/proxy/A8F74CD7851BDEE8DCD2E86AB4E2A711/someEncodedPath";

  private MockMvc mockMvc;
  private ProxyController controller;

  @Before
  public void setUp() {
    Monkifier monkifier = Mockito.mock(Monkifier.class);
    controller = new ProxyController(monkifier);
    // inject default @Value fields
    ReflectionTestUtils.setField(controller, "oncokbToken", "");
    ReflectionTestUtils.setField(
        controller, "oncokbApiUrl", "https://public.api.oncokb.org/api/v1");
    ReflectionTestUtils.setField(controller, "showOncokb", true);

    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  /**
   * When the OncoKB service is disabled (show.oncokb=false), requests to the dev proxy endpoint
   * should be rejected with HTTP 404.
   */
  @Test
  public void devOncokb_whenServiceDisabled_returns404() throws Exception {
    ReflectionTestUtils.setField(controller, "showOncokb", false);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/proxy/dev/oncokb/genes/BRAF"))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  /**
   * Requests to the encoded OncoKB proxy without the required X-Proxy-User-Agreement header should
   * be rejected with HTTP 400.
   */
  @Test
  public void encodedOncokb_whenUserAgreementHeaderMissing_returns400() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(ENCODED_ONCOKB_PATH))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  /**
   * Requests to the encoded OncoKB proxy with an incorrect X-Proxy-User-Agreement header value
   * should be rejected with HTTP 400.
   */
  @Test
  public void encodedOncokb_whenUserAgreementHeaderIsWrong_returns400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(ENCODED_ONCOKB_PATH)
                .header("X-Proxy-User-Agreement", "I agree"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }
}
