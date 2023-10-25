package org.cbioportal.persistence.mybatis.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;


@Configuration
@MapperScan("org.cbioportal.persistence.mybatis")
public class PersistenceConfig {

    // This is the only way I was able to register the SampleType TypeHandler to MyBatis.
    // The bean-based approach described in the MyBatis docs for mybatis-spring-boot-autoconfigure
    // did not work in my hands (See: https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure#Detecting_MyBatis_components).
    @Bean
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.getTypeHandlerRegistry().register(Sample.SampleType.class, new SampleTypeTypeHandler());
            }
        };
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        // Specify the location of your mapper XML files in the 'mappers' directory
        Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath:org/cbioportal/persistence/mybatis/*.xml");

        sessionFactory.setMapperLocations(resources);
        sessionFactory.setTypeHandlers(new SampleTypeTypeHandler());
        return sessionFactory;
    }
    
}
