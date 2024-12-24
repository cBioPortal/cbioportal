package org.cbioportal.persistence.mybatisclickhouse.config;

import org.cbioportal.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;


@Configuration
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
@MapperScan(value= "org.cbioportal.persistence.mybatisclickhouse", sqlSessionFactoryRef ="sqlColumnarSessionFactory")
public class PersistenceColumnarConfig {

    @Bean("sqlColumnarSessionFactory")
    public SqlSessionFactoryBean sqlColumnarSessionFactory(@Qualifier("clickhouseDataSource") DataSource dataSource, ApplicationContext applicationContext) throws IOException {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
            applicationContext.getResources("classpath:org/cbioportal/persistence/mybatisclickhouse/*.xml")
        );
        sessionFactory.setTypeHandlers(new SampleTypeTypeHandler());
        return sessionFactory;
    }
    
}
