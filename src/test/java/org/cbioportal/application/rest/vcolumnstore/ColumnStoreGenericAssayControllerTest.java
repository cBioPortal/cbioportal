package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.cbioportal.domain.generic_assay.usecase.GetGenericAssayMetaUseCase;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.GenericAssayMetaFilter;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ColumnStoreGenericAssayController.class, TestConfig.class})
public class ColumnStoreGenericAssayControllerTest {

  private static final String PROF_ID = "test_prof_id";
  private static final String ENTITY_TYPE = "test_type";
  public static final String GENERIC_ASSAY_STABLE_ID_1 = "genericAssayStableId1";
  public static final String GENERIC_ASSAY_STABLE_ID_2 = "genericAssayStableId2";
  private static final String TEST_NAME = "name";
  private static final String TEST_NAME_VALUE = "test_name";
  private static final String TEST_DESCRIPTION = "description";
  private static final String TEST_DESCRIPTION_VALUE = "test_description";
  private static final HashMap<String, String> GENERIC_ENTITY_META_PROPERTIES =
      new HashMap<String, String>() {
        {
          put(TEST_NAME, TEST_NAME_VALUE);
          put(TEST_DESCRIPTION, TEST_DESCRIPTION_VALUE);
        }
      };

  @MockitoBean private GetGenericAssayMetaUseCase getGenericAssayMetaUseCase;

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void testGetGenericAssayMetaByMolecularProfileId() throws Exception {
    List<GenericAssayMeta> genericAssayMetaItems = createGenericAssayMetaItemsList();

    Mockito.when(getGenericAssayMetaUseCase.execute(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(genericAssayMetaItems);

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/column-store/generic-assay-meta/" + PROF_ID)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].entityType").value(ENTITY_TYPE))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(GENERIC_ASSAY_STABLE_ID_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].entityType").value(ENTITY_TYPE))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").value(GENERIC_ASSAY_STABLE_ID_2))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$[1].genericEntityMetaProperties", Matchers.hasKey(TEST_NAME)))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$[1].genericEntityMetaProperties", Matchers.hasValue(TEST_NAME_VALUE)));
  }

  @Test
  @WithMockUser
  public void testGetGenericAssayMetaByStableId() throws Exception {
    List<GenericAssayMeta> genericAssayMetaSingleItem = createGenericAssayMetaSingleItem();

    Mockito.when(getGenericAssayMetaUseCase.execute(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(genericAssayMetaSingleItem);

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/api/column-store/generic-assay-meta/generic-assay/"
                            + GENERIC_ASSAY_STABLE_ID_2)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].entityType").value(ENTITY_TYPE))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(GENERIC_ASSAY_STABLE_ID_2))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$[0].genericEntityMetaProperties", Matchers.hasKey(TEST_NAME)))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$[0].genericEntityMetaProperties", Matchers.hasValue(TEST_NAME_VALUE)));
  }

  @Test
  @WithMockUser
  public void testFetchGenericAssayMeta() throws Exception {
    List<GenericAssayMeta> genericAssayMetaItems = createGenericAssayMetaItemsList();
    List<String> genericAssayStableIds =
        Arrays.asList(GENERIC_ASSAY_STABLE_ID_1, GENERIC_ASSAY_STABLE_ID_2);
    GenericAssayMetaFilter genericAssayMetaFilter = new GenericAssayMetaFilter();
    genericAssayMetaFilter.setGenericAssayStableIds(genericAssayStableIds);

    Mockito.when(getGenericAssayMetaUseCase.execute(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(genericAssayMetaItems);

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/column-store/generic-assay-meta/fetch")
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(genericAssayMetaFilter)))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].entityType").value(ENTITY_TYPE))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(GENERIC_ASSAY_STABLE_ID_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].entityType").value(ENTITY_TYPE))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").value(GENERIC_ASSAY_STABLE_ID_2))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$[1].genericEntityMetaProperties", Matchers.hasKey(TEST_NAME)))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$[1].genericEntityMetaProperties", Matchers.hasValue(TEST_NAME_VALUE)));
  }

  private List<GenericAssayMeta> createGenericAssayMetaSingleItem() {
    List<GenericAssayMeta> items = new ArrayList<>();
    items.add(
        new GenericAssayMeta(
            ENTITY_TYPE, GENERIC_ASSAY_STABLE_ID_2, GENERIC_ENTITY_META_PROPERTIES));
    return items;
  }

  private List<GenericAssayMeta> createGenericAssayMetaItemsList() {
    List<GenericAssayMeta> items = new ArrayList<>();
    items.add(new GenericAssayMeta(ENTITY_TYPE, GENERIC_ASSAY_STABLE_ID_1));
    items.add(
        new GenericAssayMeta(
            ENTITY_TYPE, GENERIC_ASSAY_STABLE_ID_2, GENERIC_ENTITY_META_PROPERTIES));
    return items;
  }
}
