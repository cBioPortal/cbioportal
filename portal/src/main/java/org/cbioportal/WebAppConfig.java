package org.cbioportal;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS =
        {
            "classpath:/META-INF/resources/",
            "classpath:/resources/",
            "classpath:/static/",
            "classpath:/public/",
            "classpath:/reactapp/",
            "classpath:/",
            "file:/opt/myfiles/"
        };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/**")
            .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
            .setCachePeriod(3000);
    }

}
