package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.cbioportal.application.security.config.MethodSecurityConfig;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
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
 * authorization off: the API is open to anonymous callers and {@link MethodSecurityConfig} is not
 * loaded, so every existing study is readable by everyone. The filter chain stands in for {@code
 * OptionalOAuth2SecurityConfig}, which permits every request.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@TestPropertySource(
    properties = {"authenticate=optional_oauth2", "security.method_authorization_enabled=false"})
@ContextConfiguration(
    classes = {
      StudyAccessController.class,
      MethodSecurityConfig.class,
      TestConfig.class,
      StudyAccessControllerOptionalAuthTest.PermitAllSecurityConfig.class
    })
public class StudyAccessControllerOptionalAuthTest {

  private static final String STUDY_ID = "restricted_study";
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
    Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(STUDY_ID))
        .thenReturn(Optional.of(STUDY_ID));
  }

  @Test
  public void anonymousCaller_getsNoContentWithoutUserHeader() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().string(StudyAccessController.STUDY_HEADER, STUDY_ID));

    // Without method security there is no permission map to consult.
    Mockito.verifyNoInteractions(cacheMapUtil);
  }

  @Test
  @WithMockUser(username = "reader")
  public void loggedInCaller_getsNoContentWithUserHeader() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().string(StudyAccessController.USER_HEADER, "reader"))
        .andExpect(header().string(StudyAccessController.STUDY_HEADER, STUDY_ID));
  }
}
