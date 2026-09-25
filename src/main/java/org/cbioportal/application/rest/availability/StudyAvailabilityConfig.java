package org.cbioportal.application.rest.availability;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Applies {@link StudyAvailabilityInterceptor} to every request-mapped method of the REST
 * controllers, on every portal regardless of authentication.
 *
 * <p>The advisor is ordered innermost, i.e. after {@code @PreAuthorize}: a user without access to a
 * study still gets 403, so availability is never revealed for studies they cannot read.
 *
 * <p>Only registered when {@value #ENABLED_PROPERTY} is {@code true}. The study list marks
 * unavailable studies {@code readPermission: false} either way.
 */
@Configuration
public class StudyAvailabilityConfig {

  /** Opts into returning 423 for unavailable studies; off by default. */
  public static final String ENABLED_PROPERTY = "study_availability.enabled";

  /**
   * Packages (including subpackages) whose REST controllers take study-scoped input. Controllers
   * outside the {@code GlobalExceptionHandler} packages still answer 423, via the response status
   * declared on {@link StudyUnavailableException}.
   */
  private static final List<String> CONTROLLER_PACKAGES =
      List.of(
          "org.cbioportal.legacy.web",
          "org.cbioportal.application.rest.vcolumnstore",
          "org.cbioportal.application.file.export",
          "org.cbioportal.application.seo");

  /**
   * @param unavailableStudyIdentifiers resolved lazily, because advisors are created before regular
   *     beans such as the mapper are ready
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  @ConditionalOnProperty(name = ENABLED_PROPERTY, havingValue = "true")
  static Advisor studyAvailabilityAdvisor(
      ObjectProvider<UnavailableStudyIdentifiers> unavailableStudyIdentifiers) {
    StudyAvailabilityInterceptor interceptor =
        new StudyAvailabilityInterceptor(() -> unavailableStudyIdentifiers.getObject().get());
    DefaultPointcutAdvisor advisor =
        new DefaultPointcutAdvisor(new RestEndpointPointcut(), interceptor);
    advisor.setOrder(Ordered.LOWEST_PRECEDENCE);
    return advisor;
  }

  static boolean isInPackage(String packageName, String pkg) {
    return packageName.equals(pkg) || packageName.startsWith(pkg + ".");
  }

  /**
   * Matches {@code @RequestMapping} methods of {@code @RestController}s in the covered packages.
   */
  static class RestEndpointPointcut extends StaticMethodMatcherPointcut {

    RestEndpointPointcut() {
      setClassFilter(
          clazz ->
              AnnotatedElementUtils.hasAnnotation(clazz, RestController.class)
                  && CONTROLLER_PACKAGES.stream()
                      .anyMatch(pkg -> isInPackage(clazz.getPackageName(), pkg)));
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
      return AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class);
    }
  }
}
