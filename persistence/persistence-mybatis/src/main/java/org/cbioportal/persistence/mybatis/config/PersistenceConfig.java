package org.cbioportal.persistence.mybatis.config;

import org.cbioportal.model.Sample;
import org.cbioportal.persistence.mybatis.typehandler.SampleTypeHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@MapperScan("org.cbioportal.persistence.mybatis")
@Configuration
public class PersistenceConfig {

    // This is the only way I was able to register the SampleType TypeHandler to MyBatis.
    // The bean-based approach described in the MyBatis docs for mybatis-spring-boot-autoconfigure
    // did not work in my hands (See: https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure#Detecting_MyBatis_components).
    @Bean
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.getTypeHandlerRegistry().register(Sample.SampleType.class, new SampleTypeHelper());
            }
        };
    }

}