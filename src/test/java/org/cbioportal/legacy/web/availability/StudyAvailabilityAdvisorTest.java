package org.cbioportal.legacy.web.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.cbioportal.application.rest.availability.StudyAvailabilityConfig;
import org.cbioportal.application.rest.availability.StudyUnavailableException;
import org.cbioportal.application.rest.availability.UnavailableStudyIdentifiers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verifies the advisor is wired the way the portal runs it: applied to REST controllers with no
 * method security at all (no-auth portals), and ordered after @PreAuthorize when it is present.
 * Lives under org.cbioportal.legacy.web because the advisor only targets controller packages.
 */
class StudyAvailabilityAdvisorTest {

  @RestController
  public static class TestController {
    // Inert without method security; present only to satisfy EndpointAuthorizationArchTest.
    @PreAuthorize("permitAll()")
    @GetMapping("/studies/{studyId}")
    public String getStudy(@PathVariable String studyId) {
      return "ok";
    }

    @PreAuthorize("hasRole('READER')")
    @GetMapping("/secured/studies/{studyId}")
    public String getSecuredStudy(@PathVariable String studyId) {
      return "ok";
    }
  }

  @Configuration
  static class Beans {
    @Bean
    UnavailableStudyIdentifiers unavailableStudyIdentifiers() {
      UnavailableStudyIdentifiers identifiers = mock(UnavailableStudyIdentifiers.class);
      when(identifiers.get()).thenReturn(Map.of("study1", "study1"));
      return identifiers;
    }

    @Bean
    TestController testController() {
      return new TestController();
    }
  }

  @Configuration
  @EnableMethodSecurity
  static class MethodSecurity {}

  private final ApplicationContextRunner disabledRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class))
          .withUserConfiguration(StudyAvailabilityConfig.class, Beans.class);

  private final ApplicationContextRunner runner =
      disabledRunner.withPropertyValues(UnavailableStudyIdentifiers.ENABLED_PROPERTY + "=true");

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void notAppliedUnlessEnabled() {
    disabledRunner.run(
        context -> {
          assertThat(context).doesNotHaveBean(Advisor.class);
          assertEquals("ok", context.getBean(TestController.class).getStudy("study1"));
        });
    disabledRunner
        .withPropertyValues(UnavailableStudyIdentifiers.ENABLED_PROPERTY + "=false")
        .run(context -> assertThat(context).doesNotHaveBean(Advisor.class));
  }

  @Test
  void appliesWithoutMethodSecurity() {
    runner.run(
        context -> {
          TestController controller = context.getBean(TestController.class);
          assertThrows(StudyUnavailableException.class, () -> controller.getStudy("study1"));
          assertEquals("ok", controller.getStudy("study2"));
        });
  }

  @Test
  void runsAfterPreAuthorize() {
    runner
        .withUserConfiguration(MethodSecurity.class)
        .run(
            context -> {
              TestController controller = context.getBean(TestController.class);

              SecurityContextHolder.getContext()
                  .setAuthentication(new TestingAuthenticationToken("user", "pw"));
              assertThrows(
                  AccessDeniedException.class,
                  () -> controller.getSecuredStudy("study1"),
                  "a user without access must get 403, never learn the study is unavailable");

              SecurityContextHolder.getContext()
                  .setAuthentication(new TestingAuthenticationToken("user", "pw", "ROLE_READER"));
              assertThrows(
                  StudyUnavailableException.class, () -> controller.getSecuredStudy("study1"));
            });
  }
}
