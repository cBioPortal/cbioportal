package org.cbioportal.legacy.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.exception.InvalidVirtualStudyDataException;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.validation.VirtualStudyValidationMessages;
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
@ContextConfiguration(classes = {PublicVirtualStudiesController.class, TestConfig.class})
@TestPropertySource(properties = "session.endpoint.publisher-api-key=test-publisher-key")
public class PublicVirtualStudiesControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockBean private VirtualStudyService virtualStudyService;

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  public void publishVirtualStudyShouldRejectInvalidRequestBody() throws Exception {
    VirtualStudyData virtualStudyData = createStaticVirtualStudyData();
    virtualStudyData.setName(" ");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/public_virtual_studies/virtual-study")
                .with(csrf())
                .header("X-PUBLISHER-API-KEY", "test-publisher-key")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(virtualStudyData)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message")
                .value("name " + VirtualStudyValidationMessages.NAME_REQUIRED));

    verify(virtualStudyService, never()).publishVirtualStudy(anyString(), any(), any(), any());
  }

  @Test
  @WithMockUser
  public void publishVirtualStudyShouldRejectMissingDynamicFilter() throws Exception {
    VirtualStudyData virtualStudyData = createDynamicVirtualStudyData();
    virtualStudyData.setStudyViewFilter(null);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/public_virtual_studies/virtual-study")
                .with(csrf())
                .header("X-PUBLISHER-API-KEY", "test-publisher-key")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(virtualStudyData)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message")
                .value(
                    "studyViewFilter " + VirtualStudyValidationMessages.DYNAMIC_FILTER_REQUIRED));

    verify(virtualStudyService, never()).publishVirtualStudy(anyString(), any(), any(), any());
  }

  @Test
  @WithMockUser
  public void publishVirtualStudyShouldReturnBadRequestForServiceValidationErrors()
      throws Exception {
    VirtualStudyData virtualStudyData = createDynamicVirtualStudyData();
    doThrow(new InvalidVirtualStudyDataException(VirtualStudyValidationMessages.NO_FILTER_RESULTS))
        .when(virtualStudyService)
        .publishVirtualStudy(anyString(), any(), any(), any());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/public_virtual_studies/virtual-study")
                .with(csrf())
                .header("X-PUBLISHER-API-KEY", "test-publisher-key")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(virtualStudyData)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message")
                .value(VirtualStudyValidationMessages.NO_FILTER_RESULTS));
  }

  private VirtualStudyData createStaticVirtualStudyData() {
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setName("Static virtual study");
    virtualStudyData.setDynamic(false);

    VirtualStudySamples virtualStudySamples = new VirtualStudySamples();
    virtualStudySamples.setId("study_1");
    virtualStudySamples.setSamples(Set.of("sample_1"));
    virtualStudyData.setStudies(Set.of(virtualStudySamples));

    return virtualStudyData;
  }

  private VirtualStudyData createDynamicVirtualStudyData() {
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setName("Dynamic virtual study");
    virtualStudyData.setDynamic(true);

    VirtualStudySamples virtualStudySamples = new VirtualStudySamples();
    virtualStudySamples.setId("study_1");
    virtualStudyData.setStudies(Set.of(virtualStudySamples));

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of("study_1"));
    virtualStudyData.setStudyViewFilter(studyViewFilter);

    return virtualStudyData;
  }
}
