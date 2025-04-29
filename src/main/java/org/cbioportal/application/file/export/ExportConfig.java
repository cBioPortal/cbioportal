package org.cbioportal.application.file.export;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.cbioportal.application.file.export.exporters.CancerStudyMetadataExporter;
import org.cbioportal.application.file.export.exporters.CancerTypeDataTypeExporter;
import org.cbioportal.application.file.export.exporters.CaseListsExporter;
import org.cbioportal.application.file.export.exporters.ClinicalPatientAttributesDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ClinicalSampleAttributesDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ClinicalTimelineDataTypeExporter;
import org.cbioportal.application.file.export.exporters.CnaContinuousDatatypeExporter;
import org.cbioportal.application.file.export.exporters.CnaDiscreteDatatypeExporter;
import org.cbioportal.application.file.export.exporters.CnaLog2ValueDatatypeExporter;
import org.cbioportal.application.file.export.exporters.CnaSegDatatypeExporter;
import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.export.exporters.GenePanelMatrixDatatypeExporter;
import org.cbioportal.application.file.export.exporters.GenericAssayBinaryDatatypeExporter;
import org.cbioportal.application.file.export.exporters.GenericAssayCategoricalDatatypeExporter;
import org.cbioportal.application.file.export.exporters.GenericAssayLimitValueDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MethylationContinuousDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MrnaExpressionContinuousDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MrnaExpressionDiscreteDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MrnaExpressionZScoreDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MutationExtendedDatatypeExporter;
import org.cbioportal.application.file.export.exporters.MutationUncalledDatatypeExporter;
import org.cbioportal.application.file.export.exporters.ProteinLevelContinuousDatatypeExporter;
import org.cbioportal.application.file.export.exporters.ProteinLevelLog2ValueDatatypeExporter;
import org.cbioportal.application.file.export.exporters.ProteinLevelZScoreDatatypeExporter;
import org.cbioportal.application.file.export.exporters.StructuralVariantDataTypeExporter;
import org.cbioportal.application.file.export.mappers.CancerStudyMetadataMapper;
import org.cbioportal.application.file.export.mappers.CaseListMetadataMapper;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.export.mappers.CnaSegmentMapper;
import org.cbioportal.application.file.export.mappers.GenePanelMatrixMapper;
import org.cbioportal.application.file.export.mappers.GeneticProfileDataMapper;
import org.cbioportal.application.file.export.mappers.GeneticProfileMapper;
import org.cbioportal.application.file.export.mappers.MafRecordMapper;
import org.cbioportal.application.file.export.mappers.SVMapper;
import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.export.services.CaseListMetadataService;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.export.services.CnaSegmentService;
import org.cbioportal.application.file.export.services.ExportService;
import org.cbioportal.application.file.export.services.GenePanelMatrixService;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.application.file.export.services.StructuralVariantService;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

//2. Add posibility to specify alternative study id while exporting
//3. Add posiblity to filter out data based on set of sample ids
//1. Add posibility to specify base (study folder) path without disruptiong the current implementation to much
//4. Read definition of Virtual study from the mongodb
//5. Think how to reevaluate the dynamic virtual study
//5. Add explanation if virtual study is exported
@Configuration
@ConditionalOnProperty(name = "dynamic_study_export_mode", havingValue = "true")
@MapperScan(basePackages = "org.cbioportal.application.file.export.mappers", sqlSessionFactoryRef = "exportSqlSessionFactory")
public class ExportConfig implements WebMvcConfigurer {

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
    public StructuralVariantService structuralVariantService(SVMapper structuralVariantMapper) {
        return new StructuralVariantService(structuralVariantMapper);
    }

    @Bean
    public CnaSegmentService cnaSegmentService(CnaSegmentMapper cnaSegmentMapper) {
        return new CnaSegmentService(cnaSegmentMapper);
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
    public GenePanelMatrixService genePanelMatrixService(GenePanelMatrixMapper genePanelMatrixMapper) {
        return new GenePanelMatrixService(genePanelMatrixMapper);
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

    @Value("${dynamic_study_export_mode.timeout_ms:600000}") //10 minutes timeout by default
    private long timeoutMs;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(timeoutMs);
    }

    @Bean
    public List<Exporter> exporters(CancerStudyMetadataExporter cancerStudyMetadataExporter,
                                    CancerTypeDataTypeExporter cancerTypeDataTypeExporter,
                                    ClinicalPatientAttributesDataTypeExporter clinicalPatientAttributesMetadataAndDataExporter,
                                    ClinicalSampleAttributesDataTypeExporter clinicalSampleAttributesMetadataAndDataExporter,
                                    ClinicalTimelineDataTypeExporter clinicalTimelineDataTypeExporter,
                                    MutationExtendedDatatypeExporter mutationExtendedDatatypeExporter,
                                    MutationUncalledDatatypeExporter mutationUncalledDatatypeExporter,
                                    StructuralVariantDataTypeExporter structuralVariantDataTypeExporter,
                                    MrnaExpressionContinuousDatatypeExporter mrnaExpressionContinuousDatatypeExporter,
                                    MrnaExpressionZScoreDatatypeExporter mrnaExpressionZScoreDatatypeExporter,
                                    MrnaExpressionDiscreteDatatypeExporter mrnaExpressionDiscreteDatatypeExporter,
                                    CnaDiscreteDatatypeExporter cnaDiscreteDatatypeExporter,
                                    CnaContinuousDatatypeExporter cnaContinuousDatatypeExporter,
                                    CnaLog2ValueDatatypeExporter cnaLog2ValueDatatypeExporter,
                                    CnaSegDatatypeExporter cnaSegDatatypeExporter,
                                    ProteinLevelContinuousDatatypeExporter proteinLevelContinuousDatatypeExporter,
                                    ProteinLevelZScoreDatatypeExporter proteinLevelZScoreDatatypeExporter,
                                    ProteinLevelLog2ValueDatatypeExporter proteinLevelLog2ValueDatatypeExporter,
                                    GenericAssayLimitValueDatatypeExporter genericAssayLimitValueDatatypeExporter,
                                    GenericAssayCategoricalDatatypeExporter genericAssayCategoricalDatatypeExporter,
                                    GenericAssayBinaryDatatypeExporter genericAssayBinaryDatatypeExporter,
                                    MethylationContinuousDatatypeExporter methylationContinuousDatatypeExporter,
                                    GenePanelMatrixDatatypeExporter genePanelMatrixDatatypeExporter,
                                    CaseListsExporter caseListsExporter,
                                    Exporter readmeExporter) {
        return List.of(
            cancerStudyMetadataExporter,
            cancerTypeDataTypeExporter,
            clinicalPatientAttributesMetadataAndDataExporter,
            clinicalSampleAttributesMetadataAndDataExporter,
            clinicalTimelineDataTypeExporter,
            mutationExtendedDatatypeExporter,
            mutationUncalledDatatypeExporter,
            structuralVariantDataTypeExporter,
            mrnaExpressionContinuousDatatypeExporter,
            mrnaExpressionZScoreDatatypeExporter,
            mrnaExpressionDiscreteDatatypeExporter,
            cnaDiscreteDatatypeExporter,
            cnaContinuousDatatypeExporter,
            cnaLog2ValueDatatypeExporter,
            cnaSegDatatypeExporter,
            proteinLevelContinuousDatatypeExporter,
            proteinLevelZScoreDatatypeExporter,
            proteinLevelLog2ValueDatatypeExporter,
            genericAssayLimitValueDatatypeExporter,
            genericAssayCategoricalDatatypeExporter,
            genericAssayBinaryDatatypeExporter,
            methylationContinuousDatatypeExporter,
            genePanelMatrixDatatypeExporter,
            caseListsExporter,
            readmeExporter
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
    public ClinicalTimelineDataTypeExporter clinicalTimelineDataTypeExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        return new ClinicalTimelineDataTypeExporter(clinicalDataAttributeDataService);
    }

    @Bean
    public MutationExtendedDatatypeExporter mutationExtendedDatatypeExporter(GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
        return new MutationExtendedDatatypeExporter(geneticProfileService, mafRecordService);
    }

    @Bean
    public MutationUncalledDatatypeExporter mutationUncalledDatatypeExporter(GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
        return new MutationUncalledDatatypeExporter(geneticProfileService, mafRecordService);
    }

    @Bean
    public StructuralVariantDataTypeExporter structuralVariantDataTypeExporter(GeneticProfileService geneticProfileService, StructuralVariantService structuralVariantService) {
        return new StructuralVariantDataTypeExporter(geneticProfileService, structuralVariantService);
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
    public CnaDiscreteDatatypeExporter cnaDiscreteDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new CnaDiscreteDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public CnaContinuousDatatypeExporter cnaContinuousDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new CnaContinuousDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public CnaLog2ValueDatatypeExporter cnaLog2ValueDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new CnaLog2ValueDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public CnaSegDatatypeExporter cnaSegDatatypeExporter(CancerStudyMetadataService cancerStudyMetadataService, CnaSegmentService cnaSegmentService) {
        return new CnaSegDatatypeExporter(cancerStudyMetadataService, cnaSegmentService);
    }

    @Bean
    public ProteinLevelContinuousDatatypeExporter proteinLevelContinuousDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new ProteinLevelContinuousDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public ProteinLevelZScoreDatatypeExporter proteinLevelZScoreDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new ProteinLevelZScoreDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public ProteinLevelLog2ValueDatatypeExporter proteinLevelLog2ValueDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new ProteinLevelLog2ValueDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public GenericAssayLimitValueDatatypeExporter genericAssayLimitValueDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new GenericAssayLimitValueDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public GenericAssayCategoricalDatatypeExporter genericAssayCategoricalDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new GenericAssayCategoricalDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public GenericAssayBinaryDatatypeExporter genericAssayBinaryDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new GenericAssayBinaryDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public MethylationContinuousDatatypeExporter methylationContinuousDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        return new MethylationContinuousDatatypeExporter(geneticProfileService, geneticProfileDataService);
    }

    @Bean
    public GenePanelMatrixDatatypeExporter genePanelMatrixDatatypeExporter(GenePanelMatrixService genePanelMatrixService) {
        return new GenePanelMatrixDatatypeExporter(genePanelMatrixService);
    }

    @Bean
    public CaseListsExporter caseListsExporter(CaseListMetadataService caseListMetadataService) {
        return new CaseListsExporter(caseListMetadataService);
    }

    @Bean
    public Exporter readmeExporter() {
        return (fileWriterFactory, studyDetails) -> {
            try (Writer readmeWriter = fileWriterFactory.newWriter("README.txt")) {
                readmeWriter.write("This is a README file for the study " + Optional.ofNullable(studyDetails.getExportAsStudyId()).orElseGet(studyDetails::getStudyId) + ".\n");
                readmeWriter.write("""
                    This study export may not include all data types available in the study, as export is implemented only for certain data types.
                    Refer to the documentation for details on the data files included in this export.
                    """);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        };
    }
}
