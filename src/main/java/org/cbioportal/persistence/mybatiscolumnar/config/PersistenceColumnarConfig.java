package org.cbioportal.persistence.mybatiscolumnar.config;

import org.cbioportal.persistence.mybatis.typehandler.SampleTypeTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;


@Configuration
@MapperScan(value="org.cbioportal.persistence.mybatiscolumnar", sqlSessionFactoryRef ="sqlColumnarSessionFactory")
public class PersistenceColumnarConfig {

    @Bean("sqlColumnarSessionFactory")
    public SqlSessionFactoryBean sqlColumnarSessionFactory(@Qualifier("columnarDataSource") DataSource dataSource, ApplicationContext applicationContext) throws IOException {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
            applicationContext.getResources("classpath:org/cbioportal/persistence/mybatiscolumnar/*.xml")
        );
        sessionFactory.setTypeHandlers(new SampleTypeTypeHandler());
        return sessionFactory;
    }
    
}
