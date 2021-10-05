package org.cbioportal;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
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
        // TODO remove?
        registry
            .addResourceHandler("/webapp/**").addResourceLocations("classpath:/webapp/")
            .setCachePeriod(3000);
    }

}
