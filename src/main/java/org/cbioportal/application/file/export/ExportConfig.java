package org.cbioportal.application.file.export;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.cbioportal.application.file.export.mappers.CancerStudyMetadataMapper;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.export.mappers.GeneticProfileMapper;
import org.cbioportal.application.file.export.mappers.MafRecordMapper;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "dynamic_study_export_mode", havingValue = "true")
@MapperScan(basePackages = "org.cbioportal.application.file.export.mappers", sqlSessionFactoryRef = "exportSqlSessionFactory")
public class ExportConfig {

    @Bean
    public CancerStudyMetadataService cancerStudyMetadataService(CancerStudyMetadataMapper cancerStudyMetadataMapper) {
        return new CancerStudyMetadataService(cancerStudyMetadataMapper);
    }

    @Bean
    public ClinicalAttributeDataService clinicalDataAttributeDataService(ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
        return new ClinicalAttributeDataService(clinicalAttributeDataMapper);
    }

    @Bean
    public MafRecordService mafRecordService(MafRecordMapper mafRecordMapper) {
        return new MafRecordService(mafRecordMapper);
    }

    @Bean
    public GeneticProfileService geneticProfileService(GeneticProfileMapper geneticProfileMapper) {
        return new GeneticProfileService(geneticProfileMapper);
    }

    @Bean
    public ExportService exportService(CancerStudyMetadataService cancerStudyMetadataService,
                                       ClinicalAttributeDataService clinicalDataAttributeDataService,
                                       GeneticProfileService geneticProfileService,
                                       MafRecordService mafRecordService) {
        return new ExportService(cancerStudyMetadataService, clinicalDataAttributeDataService, geneticProfileService, mafRecordService);
    }
    
    @Bean("exportSqlSessionFactory")
    public SqlSessionFactoryBean exportSqlSessionFactory(@Qualifier("exportDataSource") DataSource dataSource, ApplicationContext applicationContext) throws IOException {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
            applicationContext.getResources("classpath:mappers/export/*.xml"));
        return sessionFactory;
    }

    @Bean
    public DataSource exportDataSource(DataSourceProperties mysqlDataSourceProperties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysqlDataSourceProperties.getUrl());
        hikariConfig.setUsername(mysqlDataSourceProperties.getUsername());
        hikariConfig.setPassword(mysqlDataSourceProperties.getPassword());

        // Pool settings
        //hikariConfig.setMaximumPoolSize(2);
        //hikariConfig.setMinimumIdle(1);

        // Set MySQL streaming properties
        Properties dsProperties = new Properties();
        dsProperties.setProperty("useCursorFetch", "true");
        dsProperties.setProperty("defaultFetchSize", "1000");
        hikariConfig.setDataSourceProperties(dsProperties);

        return new HikariDataSource(hikariConfig);
    }
    
}
