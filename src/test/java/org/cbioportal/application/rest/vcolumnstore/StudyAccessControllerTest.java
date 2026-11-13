package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;
import org.cbioportal.application.rest.availability.StudyAvailabilityConfig;
import org.cbioportal.application.rest.availability.UnavailableStudyIdentifiers;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.application.security.config.ApiSecurityConfig;
import org.cbioportal.application.security.config.MethodSecurityConfig;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests {@link StudyAccessController} on a portal that requires authentication, with the real API
 * filter chain ({@link ApiSecurityConfig}) and the real {@link CancerStudyPermissionEvaluator} from
 * {@link MethodSecurityConfig}. Only its study permission map ({@link CacheMapUtil}) is stubbed.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ImportAutoConfiguration(AopAutoConfiguration.class)
@TestPropertySource(
    properties = {
      "authenticate=oauth2",
      "dat.method=none",
      "filter_groups_by_appname=false",
      "study_availability.enabled=true"
    })
@ContextConfiguration(
    classes = {
      StudyAccessController.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      StudyAvailabilityConfig.class,
      TestConfig.class,
      StudyAccessControllerTest.WebSecurityTestConfig.class
    })
public class StudyAccessControllerTest {

  /** Readable by users in GROUP_A. */
  private static final String ALLOWED_STUDY_ID = "allowed_study";

  /** Readable by users in GROUP_B only. */
  private static final String RESTRICTED_STUDY_ID = "restricted_study";

  /** In the database but not yet in the permission map, as right after an import. */
  private static final String UNMAPPED_STUDY_ID = "new_study";

  /** In the permission map but no longer in the database, as right after a deletion. */
  private static final String DELETED_STUDY_ID = "deleted_study";

  private static final String UNKNOWN_STUDY_ID = "no_such_study";
  private static final String URL = "/api/studies/{studyId}/access";

  @EnableWebSecurity
  static class WebSecurityTestConfig {}

  @MockitoBean private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;
  @MockitoBean private UnavailableStudyIdentifiers unavailableStudyIdentifiers;

  @Autowired private WebApplicationContext context;
  @Autowired private CacheMapUtil cacheMapUtil;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    // Built here rather than autowired so the filter chain sees @WithMockUser users.
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    // cacheMapUtil is a plain @Bean mock from TestConfig, which Spring does not reset.
    Mockito.reset(cacheMapUtil);
    Mockito.when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(
            Map.of(
                ALLOWED_STUDY_ID, study(ALLOWED_STUDY_ID, "GROUP_A"),
                RESTRICTED_STUDY_ID, study(RESTRICTED_STUDY_ID, "GROUP_B"),
                DELETED_STUDY_ID, study(DELETED_STUDY_ID, "GROUP_A")));
    for (String studyId : new String[] {ALLOWED_STUDY_ID, RESTRICTED_STUDY_ID, UNMAPPED_STUDY_ID}) {
      Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(studyId))
          .thenReturn(Optional.of(studyId));
    }
    Mockito.when(unavailableStudyIdentifiers.get()).thenReturn(Map.of());
  }

  private static CancerStudy study(String studyId, String groups) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(studyId);
    study.setGroups(groups);
    return study;
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_A")
  public void allowedStudy_getsNoContentWithUserAndStudyHeaders() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, ALLOWED_STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().string(StudyAccessController.USER_HEADER, "reader"))
        .andExpect(header().string(StudyAccessController.STUDY_HEADER, ALLOWED_STUDY_ID))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string(""));
  }

  @Test
  public void unauthenticatedRequest_isUnauthorized() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, ALLOWED_STUDY_ID))
        .andExpect(status().isUnauthorized())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER));

    Mockito.verifyNoInteractions(getCancerStudyMetadataUseCase);
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_A")
  public void restrictedStudy_isForbiddenWithoutBody() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, RESTRICTED_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string(""));

    Mockito.verifyNoInteractions(getCancerStudyMetadataUseCase);
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_A")
  public void studyMissingFromPermissionMap_isForbidden() throws Exception {
    // The evaluator alone would allow this id: it only checks studies that are in its map.
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, UNMAPPED_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(content().string(""));
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_A")
  public void unknownStudy_isForbiddenNotNotFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, UNKNOWN_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(content().string(""));
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_A")
  public void studyMissingFromDatabase_isForbiddenEvenIfPermitted() throws Exception {
    // The permission map can be stale after a study is deleted.
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, DELETED_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(content().string(""));
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_A")
  public void studyBeingImported_isForbiddenNotLocked() throws Exception {
    Mockito.when(unavailableStudyIdentifiers.get())
        .thenReturn(Map.of(ALLOWED_STUDY_ID, ALLOWED_STUDY_ID));

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, ALLOWED_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(content().string(""));
  }

  @Test
  public void authenticatedUserWithBlankName_isForbidden() throws Exception {
    TestingAuthenticationToken blankUser = new TestingAuthenticationToken(" ", null, "GROUP_A");

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, ALLOWED_STUDY_ID).with(authentication(blankUser)))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(content().string(""));
  }
}
