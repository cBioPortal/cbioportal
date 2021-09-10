package org.cbioportal;

import io.sentry.spring.SentryExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO move to different module (web)?
@Configuration
public class SentryConfig {

    @Bean
    public SentryExceptionResolver sentryExceptionResolver() {
        return new SentryExceptionResolver();
    }

}
