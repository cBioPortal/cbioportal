package org.cbioportal;

import io.sentry.spring.SentryExceptionResolver;
import org.cbioportal.web.util.InvolvedCancerStudyExtractorInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683
@Configuration
@EnableAspectJAutoProxy // TODO no idea what this does; is this logging aspect still useful?
public class WebAppConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry
            .addResourceHandler("/images/**").addResourceLocations("classpath:/images/")
            .setCachePeriod(3000);
        registry
            .addResourceHandler("/reactapp/**").addResourceLocations("classpath:/reactapp/")
            .setCachePeriod(3000);
        registry
            .addResourceHandler("/js/**").addResourceLocations("classpath:/js/")
            .setCachePeriod(3000);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/api", "/swagger-ui.html");
    }
    
    @Bean
    public HandlerInterceptor involvedCancerStudyExtractorInterceptor() {
        return new InvolvedCancerStudyExtractorInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(involvedCancerStudyExtractorInterceptor());
    }
    
}
