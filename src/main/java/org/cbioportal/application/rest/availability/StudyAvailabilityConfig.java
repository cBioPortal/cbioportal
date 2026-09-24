package org.cbioportal.application.rest.availability;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
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
 */
@Configuration
public class StudyAvailabilityConfig {

  /**
   * Must match the {@code @ControllerAdvice} packages of {@code GlobalExceptionHandler}, otherwise
   * {@link StudyUnavailableException} would not be turned into a 423.
   */
  private static final List<String> CONTROLLER_PACKAGES =
      List.of("org.cbioportal.legacy.web", "org.cbioportal.application.rest.vcolumnstore");

  /**
   * @param unavailableStudyIdentifiers resolved lazily, because advisors are created before regular
   *     beans such as the mapper are ready
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static Advisor studyAvailabilityAdvisor(
      ObjectProvider<UnavailableStudyIdentifiers> unavailableStudyIdentifiers) {
    StudyAvailabilityInterceptor interceptor =
        new StudyAvailabilityInterceptor(() -> unavailableStudyIdentifiers.getObject().get());
    DefaultPointcutAdvisor advisor =
        new DefaultPointcutAdvisor(new RestEndpointPointcut(), interceptor);
    advisor.setOrder(Ordered.LOWEST_PRECEDENCE);
    return advisor;
  }

  /**
   * Matches {@code @RequestMapping} methods of {@code @RestController}s in the covered packages.
   */
  private static class RestEndpointPointcut extends StaticMethodMatcherPointcut {

    RestEndpointPointcut() {
      setClassFilter(
          clazz ->
              AnnotatedElementUtils.hasAnnotation(clazz, RestController.class)
                  && CONTROLLER_PACKAGES.stream()
                      .anyMatch(pkg -> clazz.getPackageName().startsWith(pkg)));
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
      return AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class);
    }
  }
}
