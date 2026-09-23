package org.cbioportal.application;

import java.util.List;
import org.cbioportal.legacy.web.ExecuterTimeInterceptor;
import org.cbioportal.legacy.web.util.InvolvedCancerStudyExtractorInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683
@Configuration
// @EnableAspectJAutoProxy // TODO no idea what this does; is this logging aspect still useful?
public class WebAppConfig implements WebMvcConfigurer {

  private static final String SINGLE_PAGE_APP_ROOT = "forward:/";

  @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
  private String swaggerRedirectUrl;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/**").addResourceLocations("classpath:/webapp/images/");
    registry.addResourceHandler("/reactapp/**").addResourceLocations("classpath:/reactapp/");
    registry.addResourceHandler("/js/**").addResourceLocations("classpath:/js/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/api", swaggerRedirectUrl);
    registry.addRedirectViewController("/installations", "https://installationmap.netlify.app/");
    registry.addRedirectViewController(
        "/tutorials", "https://docs.cbioportal.org/user-guide/overview/#tutorial-slides");
    registry.addRedirectViewController("/oql", "https://docs.cbioportal.org/user-guide/oql/");
    registry.addRedirectViewController("/donate", "https://docs.cbioportal.org/donate/");

    List<String> endpoints =
        List.of(
            "/results/*",
            "/results**",
            "/results/comparison/*",
            "/results/pathways/*",
            "/patient/*",
            "/patient**",
            "/study/*",
            "/study",
            "/mutation_mapper/*",
            "/mutation_mapper",
            "/index.do/*",
            "/case.do/*",
            "/loading/*",
            "/comparison",
            "/comparison/*",
            "/restore",
            "/index.do**",
            "/oncoprinter**",
            "/encodedRedirect",
            "/datasets**",
            "/ln**",
            "/webAPI**",
            "/news**",
            "/visualize**",
            "/oncotree2genes**");

    endpoints.forEach(route -> registry.addViewController(route).setViewName(SINGLE_PAGE_APP_ROOT));
  }

  @Bean
  public HandlerInterceptor involvedCancerStudyExtractorInterceptor() {
    return new InvolvedCancerStudyExtractorInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(involvedCancerStudyExtractorInterceptor());
    registry
        .addInterceptor(new WebRequestHandlerInterceptorAdapter(new ExecuterTimeInterceptor()))
        .addPathPatterns("/**");
  }

  // NOTE (Spring Boot 4 migration): this used to override configurePathMatch(...) to call
  // PathMatchConfigurer.setUseTrailingSlashMatch(true), restoring the pre-Spring-6 default of
  // tolerating a trailing slash on any mapped path. That method (and its PathPatternParser
  // equivalent) no longer exist anywhere in Spring 7 -- there is no remaining configuration hook
  // to control this. NEEDS RUNTIME VERIFICATION: confirm whether trailing-slash requests still
  // resolve as expected before merging (this app selects the legacy AntPathMatcher strategy via
  // spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER, which may have its own default
  // trailing-slash tolerance independent of this removed override).
}
