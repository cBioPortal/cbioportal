package org.cbioportal.application.file.export;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;
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
import org.cbioportal.application.file.export.repositories.CancerStudyMetadataRepository;
import org.cbioportal.application.file.export.repositories.CaseListMetadataRepository;
import org.cbioportal.application.file.export.repositories.ClinicalAttributeDataRepository;
import org.cbioportal.application.file.export.repositories.CnaSegmentRepository;
import org.cbioportal.application.file.export.repositories.GenePanelMatrixRepository;
import org.cbioportal.application.file.export.repositories.GeneticProfileDataRepository;
import org.cbioportal.application.file.export.repositories.GeneticProfileRepository;
import org.cbioportal.application.file.export.repositories.MafRecordRepository;
import org.cbioportal.application.file.export.repositories.SVRepository;
import org.cbioportal.application.file.export.repositories.mybatis.CancerStudyMetadataMapper;
import org.cbioportal.application.file.export.repositories.mybatis.CancerStudyMetadataMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.CaseListMetadataMapper;
import org.cbioportal.application.file.export.repositories.mybatis.CaseListMetadataMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.export.repositories.mybatis.ClinicalAttributeDataMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.CnaSegmentMapper;
import org.cbioportal.application.file.export.repositories.mybatis.CnaSegmentMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.GenePanelMatrixMapper;
import org.cbioportal.application.file.export.repositories.mybatis.GenePanelMatrixMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.GeneticProfileDataMapper;
import org.cbioportal.application.file.export.repositories.mybatis.GeneticProfileDataMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.GeneticProfileMapper;
import org.cbioportal.application.file.export.repositories.mybatis.GeneticProfileMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.MafRecordMapper;
import org.cbioportal.application.file.export.repositories.mybatis.MafRecordMyBatisRepository;
import org.cbioportal.application.file.export.repositories.mybatis.SVMapper;
import org.cbioportal.application.file.export.repositories.mybatis.SVMyBatisRepository;
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
import org.cbioportal.application.file.export.services.VirtualStudyExportDecoratorService;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "feature.study.export", havingValue = "true")
@MapperScan(
    basePackages = "org.cbioportal.application.file.export.repositories.mybatis",
    sqlSessionFactoryRef = "exportSqlSessionFactory")
public class ExportConfig implements WebMvcConfigurer {

  @Bean
  public CancerStudyMetadataRepository cancerStudyMetadataRepository(
      CancerStudyMetadataMapper cancerStudyMetadataMapper) {
    return new CancerStudyMetadataMyBatisRepository(cancerStudyMetadataMapper);
  }

  @Bean
  public CancerStudyMetadataService cancerStudyMetadataService(
      CancerStudyMetadataRepository cancerStudyMetadataRepository) {
    return new CancerStudyMetadataService(cancerStudyMetadataRepository);
  }

  @Bean
  public ClinicalAttributeDataRepository clinicalAttributeDataRepository(
      ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
    return new ClinicalAttributeDataMyBatisRepository(clinicalAttributeDataMapper);
  }

  @Bean
  public ClinicalAttributeDataService clinicalDataAttributeDataService(
      ClinicalAttributeDataRepository clinicalAttributeDataRepository) {
    return new ClinicalAttributeDataService(clinicalAttributeDataRepository);
  }

  @Bean
  public MafRecordRepository mafRecordRepository(MafRecordMapper mafRecordMapper) {
    return new MafRecordMyBatisRepository(mafRecordMapper);
  }

  @Bean
  public MafRecordService mafRecordService(MafRecordRepository mafRecordRepository) {
    return new MafRecordService(mafRecordRepository);
  }

  @Bean
  public SVRepository structuralVariantRepository(SVMapper svMapper) {
    return new SVMyBatisRepository(svMapper);
  }

  @Bean
  public StructuralVariantService structuralVariantService(SVRepository svRepository) {
    return new StructuralVariantService(svRepository);
  }

  @Bean
  public CnaSegmentRepository cnaSegmentRepository(CnaSegmentMapper cnaSegmentMapper) {
    return new CnaSegmentMyBatisRepository(cnaSegmentMapper);
  }

  @Bean
  public CnaSegmentService cnaSegmentService(CnaSegmentRepository cnaSegmentRepository) {
    return new CnaSegmentService(cnaSegmentRepository);
  }

  @Bean
  public GeneticProfileRepository geneticProfileRepository(
      GeneticProfileMapper geneticProfileMapper) {
    return new GeneticProfileMyBatisRepository(geneticProfileMapper);
  }

  @Bean
  public GeneticProfileService geneticProfileService(
      GeneticProfileRepository geneticProfileRepository) {
    return new GeneticProfileService(geneticProfileRepository);
  }

  @Bean
  public GeneticProfileDataRepository geneticProfileDataRepository(
      GeneticProfileDataMapper geneticProfileDataMapper) {
    return new GeneticProfileDataMyBatisRepository(geneticProfileDataMapper);
  }

  @Bean
  public GeneticProfileDataService geneticProfileDataService(
      GeneticProfileDataRepository geneticProfileDataRepository) {
    return new GeneticProfileDataService(geneticProfileDataRepository);
  }

  @Bean
  public GenePanelMatrixRepository genePanelMatrixRepository(
      GenePanelMatrixMapper genePanelMatrixMapper) {
    return new GenePanelMatrixMyBatisRepository(genePanelMatrixMapper);
  }

  @Bean
  public GenePanelMatrixService genePanelMatrixService(
      GenePanelMatrixRepository genePanelMatrixRepository) {
    return new GenePanelMatrixService(genePanelMatrixRepository);
  }

  @Bean
  public CaseListMetadataRepository caseListMetadataRepository(
      CaseListMetadataMapper caseListMetadataMapper) {
    return new CaseListMetadataMyBatisRepository(caseListMetadataMapper);
  }

  @Bean
  public CaseListMetadataService caseListMetadataService(
      CaseListMetadataRepository caseListMetadataRepository) {
    return new CaseListMetadataService(caseListMetadataRepository);
  }

  @Bean
  @ConditionalOnBean(CancerStudyPermissionEvaluator.class)
  public ExportService exportService(
      CancerStudyMetadataService cancerStudyMetadataService,
      CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator,
      List<Exporter> exporters) {
    return new ExportService(cancerStudyMetadataService, cancerStudyPermissionEvaluator, exporters);
  }

  @Bean
  @ConditionalOnMissingBean(CancerStudyPermissionEvaluator.class)
  public ExportService exportServiceWithoutAuth(
      CancerStudyMetadataService cancerStudyMetadataService, List<Exporter> exporters) {
    return new ExportService(cancerStudyMetadataService, null, exporters);
  }

  @Bean
  public VirtualStudyExportDecoratorService virtualStudyAwareExportService(
      VirtualStudyService virtualStudyService, ExportService exportService) {
    return new VirtualStudyExportDecoratorService(virtualStudyService, exportService);
  }

  @Bean("exportSqlSessionFactory")
  public SqlSessionFactoryBean exportSqlSessionFactory(
      @Qualifier("exportDataSource") DataSource dataSource, ApplicationContext applicationContext)
      throws IOException {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.setMapperLocations(
        applicationContext.getResources("classpath:mappers/export/*.xml"));
    return sessionFactory;
  }

  @Bean
  public DataSource exportDataSource(DataSourceProperties dataSourceProperties) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
    hikariConfig.setUsername(dataSourceProperties.getUsername());
    hikariConfig.setPassword(dataSourceProperties.getPassword());

    // Set streaming properties for data export
    Properties dsProperties = new Properties();
    dsProperties.setProperty("useCursorFetch", "true");
    dsProperties.setProperty("defaultFetchSize", "1000");
    hikariConfig.setDataSourceProperties(dsProperties);

    return new HikariDataSource(hikariConfig);
  }

  @Value("${feature.study.export.timeout_ms:600000}") // 10 minutes timeout by default
  private long timeoutMs;

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setDefaultTimeout(timeoutMs);
  }

  @Bean
  public List<Exporter> exporters(
      CancerStudyMetadataExporter cancerStudyMetadataExporter,
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
        readmeExporter);
  }

  @Bean
  public CancerTypeDataTypeExporter cancerTypeDataTypeExporter(
      CancerStudyMetadataService cancerStudyMetadataService) {
    return new CancerTypeDataTypeExporter(cancerStudyMetadataService);
  }

  @Bean
  public CancerStudyMetadataExporter cancerStudyMetadataExporter(
      CancerStudyMetadataService cancerStudyMetadataService) {
    return new CancerStudyMetadataExporter(cancerStudyMetadataService);
  }

  @Bean
  public ClinicalPatientAttributesDataTypeExporter clinicalPatientAttributesMetadataAndDataExporter(
      ClinicalAttributeDataService clinicalDataAttributeDataService) {
    return new ClinicalPatientAttributesDataTypeExporter(clinicalDataAttributeDataService);
  }

  @Bean
  public ClinicalSampleAttributesDataTypeExporter clinicalSampleAttributesMetadataAndDataExporter(
      ClinicalAttributeDataService clinicalDataAttributeDataService) {
    return new ClinicalSampleAttributesDataTypeExporter(clinicalDataAttributeDataService);
  }

  @Bean
  public ClinicalTimelineDataTypeExporter clinicalTimelineDataTypeExporter(
      ClinicalAttributeDataService clinicalDataAttributeDataService) {
    return new ClinicalTimelineDataTypeExporter(clinicalDataAttributeDataService);
  }

  @Bean
  public MutationExtendedDatatypeExporter mutationExtendedDatatypeExporter(
      GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
    return new MutationExtendedDatatypeExporter(geneticProfileService, mafRecordService);
  }

  @Bean
  public MutationUncalledDatatypeExporter mutationUncalledDatatypeExporter(
      GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
    return new MutationUncalledDatatypeExporter(geneticProfileService, mafRecordService);
  }

  @Bean
  public StructuralVariantDataTypeExporter structuralVariantDataTypeExporter(
      GeneticProfileService geneticProfileService,
      StructuralVariantService structuralVariantService) {
    return new StructuralVariantDataTypeExporter(geneticProfileService, structuralVariantService);
  }

  @Bean
  public MrnaExpressionContinuousDatatypeExporter mrnaExpressionContinuousDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new MrnaExpressionContinuousDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public MrnaExpressionZScoreDatatypeExporter mrnaExpressionZScoreDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new MrnaExpressionZScoreDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public MrnaExpressionDiscreteDatatypeExporter mrnaExpressionDiscreteDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new MrnaExpressionDiscreteDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public CnaDiscreteDatatypeExporter cnaDiscreteDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new CnaDiscreteDatatypeExporter(geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public CnaContinuousDatatypeExporter cnaContinuousDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new CnaContinuousDatatypeExporter(geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public CnaLog2ValueDatatypeExporter cnaLog2ValueDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new CnaLog2ValueDatatypeExporter(geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public CnaSegDatatypeExporter cnaSegDatatypeExporter(
      CancerStudyMetadataService cancerStudyMetadataService, CnaSegmentService cnaSegmentService) {
    return new CnaSegDatatypeExporter(cancerStudyMetadataService, cnaSegmentService);
  }

  @Bean
  public ProteinLevelContinuousDatatypeExporter proteinLevelContinuousDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new ProteinLevelContinuousDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public ProteinLevelZScoreDatatypeExporter proteinLevelZScoreDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new ProteinLevelZScoreDatatypeExporter(geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public ProteinLevelLog2ValueDatatypeExporter proteinLevelLog2ValueDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new ProteinLevelLog2ValueDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public GenericAssayLimitValueDatatypeExporter genericAssayLimitValueDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new GenericAssayLimitValueDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public GenericAssayCategoricalDatatypeExporter genericAssayCategoricalDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new GenericAssayCategoricalDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public GenericAssayBinaryDatatypeExporter genericAssayBinaryDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new GenericAssayBinaryDatatypeExporter(geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public MethylationContinuousDatatypeExporter methylationContinuousDatatypeExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    return new MethylationContinuousDatatypeExporter(
        geneticProfileService, geneticProfileDataService);
  }

  @Bean
  public GenePanelMatrixDatatypeExporter genePanelMatrixDatatypeExporter(
      GenePanelMatrixService genePanelMatrixService) {
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
        readmeWriter.write(
            "This is a README file for the study "
                + Optional.ofNullable(studyDetails.getExportWithStudyId())
                    .orElseGet(studyDetails::getStudyId)
                + ".\n");
        readmeWriter.write(
            """
                    This study export may not include all data types available in the study, as export is implemented only for certain data types.
                    Refer to the documentation for details on the data files included in this export.
                    """);
      } catch (IOException e) {
        throw new ExportException("Error writing README file", e);
      }
      return true;
    };
  }
}
