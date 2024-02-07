package org.cbioportal;

import org.cbioportal.web.util.InvolvedCancerStudyExtractorInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683
@Configuration
//@EnableAspectJAutoProxy // TODO no idea what this does; is this logging aspect still useful?
public class WebAppConfig implements WebMvcConfigurer {

	private static final String SINGLE_PAGE_APP_ROOT = "forward:/";

	@Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
	private String swaggerRedirectUrl;

	@Value("${skin.documentation.tutorial-url:https://docs.cbioportal.org/user-guide/overview/#tutorial-slides}")
	private String tutorialRedirecUrl;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/**").addResourceLocations("classpath:/webapp/images/");
		registry.addResourceHandler("/reactapp/**").addResourceLocations("classpath:/reactapp/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/js/");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/api", swaggerRedirectUrl);
		registry.addRedirectViewController("/tutorials", tutorialRedirecUrl);
		registry.addRedirectViewController("/installations", "https://installationmap.netlify.app/");

		// Redirects anything that doesn't start with /api or /proxy to the Javascript frontend
		registry.addViewController("/{path:^(?!api|proxy$).*$}").setViewName(SINGLE_PAGE_APP_ROOT);
	}

	@Bean
	public HandlerInterceptor involvedCancerStudyExtractorInterceptor() {
		return new InvolvedCancerStudyExtractorInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(involvedCancerStudyExtractorInterceptor());
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// Adds support for trailing slash Matches
		configurer.setUseTrailingSlashMatch(true);
	}

}
