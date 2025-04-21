package org.cbioportal.application.file.export;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.cbioportal.application.file.export.exporters.CancerStudyMetadataExporter;
import org.cbioportal.application.file.export.exporters.CancerTypeDataTypeExporter;
import org.cbioportal.application.file.export.exporters.CaseListsExporter;
import org.cbioportal.application.file.export.exporters.ClinicalPatientAttributesDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ClinicalSampleAttributesDataTypeExporter;
import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.export.exporters.GenericAssayLimitValueDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MafDataTypeExporter;
import org.cbioportal.application.file.export.exporters.MrnaExpressionContinuousDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MrnaExpressionDiscreteDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MrnaExpressionZScoreDatatypeExporter;
import org.cbioportal.application.file.export.mappers.CancerStudyMetadataMapper;
import org.cbioportal.application.file.export.mappers.CaseListMetadataMapper;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.export.mappers.GeneticProfileDataMapper;
import org.cbioportal.application.file.export.mappers.GeneticProfileMapper;
import org.cbioportal.application.file.export.mappers.MafRecordMapper;
import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.export.services.CaseListMetadataService;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.export.services.ExportService;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

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
    public GeneticProfileDataService geneticProfileDataService(GeneticProfileDataMapper geneticProfileDataMapper) {
        return new GeneticProfileDataService(geneticProfileDataMapper);
    }

    @Bean
    public CaseListMetadataService caseListMetadataService(CaseListMetadataMapper caseListMetadataMapper) {
        return new CaseListMetadataService(caseListMetadataMapper);
    }

    @Bean
    public ExportService exportService(List<Exporter> exporters) {
        return new ExportService(exporters);
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

    @Bean(name = "exportThreadPool")
    public Executor exportThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);         // minimum number of threads
        executor.setMaxPoolSize(10);         // maximum number of threads
        executor.setQueueCapacity(100);       // queue size before rejecting new tasks
        executor.setThreadNamePrefix("ExportExecutor-");
        //make low priority threads. So OS can schedule other tasks first
        executor.setThreadFactory(new ExportThreadFactory("ExportPool-", Thread.MIN_PRIORITY));
        executor.initialize();
        return executor;
    }

    @Bean
    public List<Exporter> exporters(CancerStudyMetadataExporter cancerStudyMetadataExporter,
                                    CancerTypeDataTypeExporter cancerTypeDataTypeExporter,
                                    ClinicalPatientAttributesDataTypeExporter clinicalPatientAttributesMetadataAndDataExporter,
                                    ClinicalSampleAttributesDataTypeExporter clinicalSampleAttributesMetadataAndDataExporter,
                                    MafDataTypeExporter mafMetadataAndDataExporter,
                                    MrnaExpressionContinuousDatatypeExporter mrnaExpressionContinuousDatatypeExporter,
                                    MrnaExpressionZScoreDatatypeExporter mrnaExpressionZScoreDatatypeExporter,
                                    MrnaExpressionDiscreteDatatypeExporter mrnaExpressionDiscreteDatatypeExporter,
                                    GenericAssayLimitValueDatatypeExporter genericAssayLimitValueDatatypeExporter,
                                    CaseListsExporter caseListsExporter) {
        return List.of(
            cancerStudyMetadataExporter,
            cancerTypeDataTypeExporter,
            clinicalPatientAttributesMetadataAndDataExporter,
            clinicalSampleAttributesMetadataAndDataExporter,
            mafMetadataAndDataExporter,
            mrnaExpressionContinuousDatatypeExporter,
            mrnaExpressionZScoreDatatypeExporter,
            mrnaExpressionDiscreteDatatypeExporter,
            genericAssayLimitValueDatatypeExporter,
            caseListsExporter
        );
    }

    @Bean
    public CancerTypeDataTypeExporter cancerTypeDataTypeExporter(CancerStudyMetadataService cancerStudyMetadataService) {
        return new CancerTypeDataTypeExporter(cancerStudyMetadataService);
    }

    @Bean
    public CancerStudyMetadataExporter cancerStudyMetadataExporter(CancerStudyMetadataService cancerStudyMetadataService) {
        return new CancerStudyMetadataExporter(cancerStudyMetadataService);
    }

    @Bean
    public ClinicalPatientAttributesDataTypeExporter clinicalPatientAttributesMetadataAndDataExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        return new ClinicalPatientAttributesDataTypeExporter(clinicalDataAttributeDataService);
    }

    @Bean
    public ClinicalSampleAttributesDataTypeExporter clinicalSampleAttributesMetadataAndDataExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        return new ClinicalSampleAttributesDataTypeExporter(clinicalDataAttributeDataService);
    }

    @Bean
    public MafDataTypeExporter mafMetadataAndDataExporter(GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
        return new MafDataTypeExporter(geneticProfileService, mafRecordService);
    }

    @Bean
    public MrnaExpressionContinuousDatatypeExporter mrnaExpressionContinuousDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new MrnaExpressionContinuousDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public MrnaExpressionZScoreDatatypeExporter mrnaExpressionZScoreDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new MrnaExpressionZScoreDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public MrnaExpressionDiscreteDatatypeExporter mrnaExpressionDiscreteDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new MrnaExpressionDiscreteDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public GenericAssayLimitValueDatatypeExporter genericAssayLimitValueDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new GenericAssayLimitValueDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public CaseListsExporter caseListsExporter(CaseListMetadataService caseListMetadataService) {
        return new CaseListsExporter(caseListMetadataService);
    }

    public static class ExportThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final int priority;
        private int count = 0;

        public ExportThreadFactory(String namePrefix, int priority) {
            this.namePrefix = namePrefix;
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + count++);
            thread.setDaemon(true);
            thread.setPriority(priority);
            return thread;
        }
    }

}
