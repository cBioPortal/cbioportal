package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;
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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests {@link StudyAccessController} with {@code authenticate=optional_oauth2} and method
 * authorization on, with the real permission evaluator. The API admits anonymous callers, so the
 * endpoint never answers 401: anonymous callers get 403, except for studies in {@code
 * always_show_study_group}. The filter chain stands in for {@code OptionalOAuth2SecurityConfig},
 * which permits every request.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@TestPropertySource(
    properties = {
      "authenticate=optional_oauth2",
      "security.method_authorization_enabled=true",
      "filter_groups_by_appname=false",
      "always_show_study_group=PUBLIC"
    })
@ContextConfiguration(
    classes = {
      StudyAccessController.class,
      MethodSecurityConfig.class,
      TestConfig.class,
      StudyAccessControllerOptionalAuthMethodSecurityTest.PermitAllSecurityConfig.class
    })
public class StudyAccessControllerOptionalAuthMethodSecurityTest {

  private static final String PUBLIC_STUDY_ID = "public_study";
  private static final String RESTRICTED_STUDY_ID = "restricted_study";
  private static final String URL = "/api/studies/{studyId}/access";

  @EnableWebSecurity
  static class PermitAllSecurityConfig {
    @Bean
    SecurityFilterChain permitAllFilterChain(HttpSecurity http) throws Exception {
      return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .csrf(AbstractHttpConfigurer::disable)
          .build();
    }
  }

  @MockitoBean private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

  @Autowired private WebApplicationContext context;
  @Autowired private CacheMapUtil cacheMapUtil;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Mockito.reset(cacheMapUtil);
    Mockito.when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(
            Map.of(
                PUBLIC_STUDY_ID, study(PUBLIC_STUDY_ID, "PUBLIC"),
                RESTRICTED_STUDY_ID, study(RESTRICTED_STUDY_ID, "GROUP_B")));
    for (String studyId : new String[] {PUBLIC_STUDY_ID, RESTRICTED_STUDY_ID}) {
      Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(studyId))
          .thenReturn(Optional.of(studyId));
    }
  }

  private static CancerStudy study(String studyId, String groups) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(studyId);
    study.setGroups(groups);
    return study;
  }

  @Test
  public void anonymousCaller_restrictedStudy_isForbiddenNotUnauthorized() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, RESTRICTED_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(content().string(""));
  }

  @Test
  public void anonymousCaller_publicStudy_getsNoContentWithoutUserHeader() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, PUBLIC_STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().string(StudyAccessController.STUDY_HEADER, PUBLIC_STUDY_ID));
  }

  @Test
  @WithMockUser(username = "reader", authorities = "GROUP_B")
  public void loggedInCallerWithAccess_getsNoContentWithUserHeader() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, RESTRICTED_STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().string(StudyAccessController.USER_HEADER, "reader"));
  }
}
