package org.cbioportal;

import java.util.List;

import org.cbioportal.web.ExecuterTimeInterceptor;
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
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683
@Configuration
//@EnableAspectJAutoProxy // TODO no idea what this does; is this logging aspect still useful?
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
		registry.addRedirectViewController("/tutorials", "https://docs.cbioportal.org/user-guide/overview/#tutorial-slides");
		registry.addRedirectViewController("/oql", "https://docs.cbioportal.org/user-guide/oql/");
        registry.addRedirectViewController("/donate", "https://docs.cbioportal.org/donate/");

		List<String> endpoints = List.of(
			"/results/*",
			"/results**",
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
            "/visualize**"
		);

		endpoints.forEach( route -> registry.addViewController(route).setViewName(SINGLE_PAGE_APP_ROOT));
	}

	@Bean
	public HandlerInterceptor involvedCancerStudyExtractorInterceptor() {
		return new InvolvedCancerStudyExtractorInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(involvedCancerStudyExtractorInterceptor());
        registry.addInterceptor(new WebRequestHandlerInterceptorAdapter(
            new ExecuterTimeInterceptor()
        )).addPathPatterns("/**");
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// Adds support for trailing slash Matches
		configurer.setUseTrailingSlashMatch(true);
	}

}
