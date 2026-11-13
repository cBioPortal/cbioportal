package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;
import org.cbioportal.application.rest.availability.StudyAvailabilityConfig;
import org.cbioportal.application.rest.availability.UnavailableStudyIdentifiers;
import org.cbioportal.application.security.config.NoSecurityConfig;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests {@link StudyAccessController} on a portal without authentication ({@code
 * authenticate=false}): no method security, every caller anonymous. Study availability is on, so
 * the 423 interceptor runs as it does in production.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ImportAutoConfiguration(AopAutoConfiguration.class)
@TestPropertySource(properties = {"authenticate=false", "study_availability.enabled=true"})
@ContextConfiguration(
    classes = {
      StudyAccessController.class,
      NoSecurityConfig.class,
      StudyAvailabilityConfig.class,
      TestConfig.class
    })
public class StudyAccessControllerNoAuthTest {

  private static final String STUDY_ID = "study_tcga_pub";
  private static final String UNKNOWN_STUDY_ID = "no_such_study";
  private static final String URL = "/api/studies/{studyId}/access";

  @MockitoBean private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;
  @MockitoBean private UnavailableStudyIdentifiers unavailableStudyIdentifiers;

  @Autowired private MockMvc mockMvc;

  @Before
  public void setUp() {
    Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(STUDY_ID))
        .thenReturn(Optional.of(STUDY_ID));
    Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(UNKNOWN_STUDY_ID))
        .thenReturn(Optional.empty());
    Mockito.when(unavailableStudyIdentifiers.get()).thenReturn(Map.of());
  }

  @Test
  public void anonymousCaller_getsNoContentWithoutUserHeader() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().string(StudyAccessController.STUDY_HEADER, STUDY_ID))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string(""));
  }

  @Test
  public void unknownStudy_isForbiddenNotNotFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, UNKNOWN_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string(""));
  }

  @Test
  public void studyBeingImported_isForbiddenNotLocked() throws Exception {
    Mockito.when(unavailableStudyIdentifiers.get()).thenReturn(Map.of(STUDY_ID, STUDY_ID));

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(content().string(""));
  }
}
