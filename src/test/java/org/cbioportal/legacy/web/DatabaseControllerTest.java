package org.cbioportal.legacy.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.cbioportal.legacy.service.DatabaseSwitchService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(DatabaseController.class)
@ContextConfiguration(classes = {DatabaseController.class, TestConfig.class})
@TestPropertySource(
    properties = {"database.endpoint.enabled=true", "database.endpoint.api-key=correct-key"})
public class DatabaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DatabaseSwitchService databaseSwitchService;

  @Autowired private DatabaseController databaseController;

  @Test
  @WithMockUser
  public void switchDatabaseNoKeyProvided() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.put("/api/database").param("database", "v2").with(csrf()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
    verify(databaseSwitchService, never()).switchDatabase("v2");
  }

  @Test
  @WithMockUser
  public void switchDatabaseUnauthorized() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/database")
                .param("database", "v2")
                .with(csrf())
                .header("X-API-KEY", "incorrect-key"))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    verify(databaseSwitchService, never()).switchDatabase("v2");
  }

  @Test
  @WithMockUser
  public void switchDatabaseSuccess() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/database")
                .param("database", "cbioportal_v2")
                .with(csrf())
                .header("X-API-KEY", "correct-key"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    verify(databaseSwitchService, times(1)).switchDatabase("cbioportal_v2");
  }

  @Test
  @WithMockUser
  public void switchDatabaseRejectsInvalidName() throws Exception {
    doThrow(new IllegalArgumentException("Invalid database name"))
        .when(databaseSwitchService)
        .switchDatabase("bad name");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/database")
                .param("database", "bad name")
                .with(csrf())
                .header("X-API-KEY", "correct-key"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void switchDatabaseDisabled() throws Exception {
    ReflectionTestUtils.setField(databaseController, "databaseEndpointEnabled", false);
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/database")
                .param("database", "v2")
                .with(csrf())
                .header("X-API-KEY", "correct-key"))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
    verify(databaseSwitchService, never()).switchDatabase("v2");
    ReflectionTestUtils.setField(databaseController, "databaseEndpointEnabled", true);
  }

  @Test
  @WithMockUser
  public void getActiveDatabaseSuccess() throws Exception {
    when(databaseSwitchService.getActiveDatabase()).thenReturn("cbioportal_v2");
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/database")
                .with(csrf())
                .header("X-API-KEY", "correct-key"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("cbioportal_v2"));
  }
}
