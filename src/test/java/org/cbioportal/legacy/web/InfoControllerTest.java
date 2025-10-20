package org.cbioportal.legacy.web;

import static org.mockito.Mockito.when;

import org.cbioportal.legacy.model.InfoDb;
import org.cbioportal.legacy.service.InfoService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {InfoController.class, TestConfig.class})
@TestPropertySource(
    properties = {
      "portal.version=test_portal_version",
      "db.version=test_db_version",
      "derived_table.version=test_derived_table_version"
    })
public class InfoControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private InfoService infoService;

  @Test
  @WithMockUser
  public void getInfo() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/info").accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.portalVersion").value("test_portal_version"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.dbVersion").value("test_db_version"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.derivedTableVersion")
                .value("test_derived_table_version"));
  }

  @Test
  @WithMockUser
  public void getInfo_usesDbThenOverrides() throws Exception {
    InfoDb db = new InfoDb();
    db.setDbSchemaVersion("db_from_db");
    db.setDerivedTableSchemaVersion("derived_from_db");
    db.setGenesetVersion("geneset_from_db");
    db.setGeneTableVersion("gene_table_from_db");
    when(infoService.getInfoFromDb()).thenReturn(db);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/info").accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        // property overrides should win for db and derived
        .andExpect(MockMvcResultMatchers.jsonPath("$.dbVersion").value("test_db_version"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.derivedTableVersion")
                .value("test_derived_table_version"))
        // geneTableVersion and genesetVersion should be present from DB
        .andExpect(MockMvcResultMatchers.jsonPath("$.geneTableVersion").value("gene_table_from_db"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.genesetVersion").value("geneset_from_db"));
  }
}
