package org.cbioportal.domain.studyview;

import org.cbioportal.domain.alteration.usecase.AlterationCountByGeneUseCases;
import org.cbioportal.domain.clinical_attributes.usecase.GetClinicalAttributesDataTypeMapUseCase;
import org.cbioportal.domain.clinical_attributes.usecase.GetClinicalAttributesForStudiesUseCase;
import org.cbioportal.domain.clinical_data.usecase.ClinicalDataUseCases;
import org.cbioportal.domain.clinical_event.usecase.GetClinicalEventTypeCountsUseCase;
import org.cbioportal.domain.generic_assay.usecase.GenericAssayUseCases;
import org.cbioportal.domain.genomic_data.usecase.GenomicDataUseCases;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationType;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.persistence.enums.DataSource;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.domain.patient.usecase.GetCaseListDataCountsUseCase;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.usecase.GetFilteredSamplesUseCase;
import org.cbioportal.shared.util.ClinicalDataCountItemUtil;
import org.cbioportal.domain.treatment.usecase.TreatmentCountReportUseCases;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * A service class responsible for handling study view-related operations, including retrieving filtered samples,
 * genomic data counts, clinical data, and other study-specific information. This class acts as a central hub
 * for coordinating various use cases and delegating tasks to the appropriate repositories and utilities.
 */
public class StudyViewService {

    private final GetFilteredSamplesUseCase getFilteredSamplesUseCase;
    private final AlterationCountByGeneUseCases alterationCountByGeneUseCase;
    private final GetClinicalEventTypeCountsUseCase getClinicalEventTypeCountsUseCase;
    private final TreatmentCountReportUseCases treatmentCountReportUseCases;
    private final GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase;
    private final GetCaseListDataCountsUseCase getCaseListDataCountsUseCase;
    private final GetClinicalAttributesDataTypeMapUseCase getClinicalAttributesDataTypeMapUseCase;
    private final ClinicalDataUseCases clinicalDataUseCases;
    private final GenomicDataUseCases genomicDataUseCases;
    private final GenericAssayUseCases genericAssayUseCases;
    private final CustomDataFilterUtil customDataFilterUtil;

    private Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap = new EnumMap<>(DataSource.class);


    public StudyViewService(GetFilteredSamplesUseCase getFilteredSamplesUseCase,
                            AlterationCountByGeneUseCases alterationCountByGeneUseCase,
                            GetClinicalEventTypeCountsUseCase getClinicalEventTypeCountsUseCase,
                            TreatmentCountReportUseCases treatmentCountReportUseCases,
                            GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase,
                            GetCaseListDataCountsUseCase getCaseListDataCountsUseCase,
                            GetClinicalAttributesDataTypeMapUseCase getClinicalAttributesDataTypeMapUseCase,
                            ClinicalDataUseCases clinicalDataUseCases,
                            GenomicDataUseCases genomicDataUseCases, GenericAssayUseCases genericAssayUseCases,
                            CustomDataFilterUtil customDataFilterUtil) {
        this.getFilteredSamplesUseCase = getFilteredSamplesUseCase;
        this.alterationCountByGeneUseCase = alterationCountByGeneUseCase;
        this.clinicalDataUseCases = clinicalDataUseCases;
        this.genomicDataUseCases = genomicDataUseCases;
        this.getClinicalEventTypeCountsUseCase = getClinicalEventTypeCountsUseCase;
        this.treatmentCountReportUseCases = treatmentCountReportUseCases;
        this.getClinicalAttributesForStudiesUseCase = getClinicalAttributesForStudiesUseCase;
        this.getCaseListDataCountsUseCase = getCaseListDataCountsUseCase;
        this.getClinicalAttributesDataTypeMapUseCase = getClinicalAttributesDataTypeMapUseCase;
        this.genericAssayUseCases = genericAssayUseCases;
        this.customDataFilterUtil = customDataFilterUtil;
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter){
        return getFilteredSamplesUseCase.execute(buildStudyViewFilterContext(studyViewFilter));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountByGeneUseCase.getAlterationCountByGeneUseCase()
                .execute(buildStudyViewFilterContext(studyViewFilter), AlterationType.MUTATION_EXTENDED);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountByGeneUseCase.getCnaAlterationCountByGeneUseCase().execute(buildStudyViewFilterContext(studyViewFilter));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountByGeneUseCase.getAlterationCountByGeneUseCase()
                .execute(buildStudyViewFilterContext(studyViewFilter), AlterationType.STRUCTURAL_VARIANT);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return genomicDataUseCases.getMolecularProfileSampleCountsUseCase().execute(buildStudyViewFilterContext(studyViewFilter));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter) {
        return getClinicalEventTypeCountsUseCase.execute(buildStudyViewFilterContext(studyViewFilter));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportUseCases.getPatientTreatmentReportUseCase().execute(buildStudyViewFilterContext(studyViewFilter));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportUseCases.getSampleTreatmentReportUseCase().execute(buildStudyViewFilterContext(studyViewFilter));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalDataCountItem> getGenomicDataBinCounts(StudyViewFilter studyViewFilter,
                                                               List<GenomicDataBinFilter> genomicDataBinFilters) {
       return ClinicalDataCountItemUtil.generateDataCountItems(genomicDataUseCases.getGenomicDataBinCountsUseCase().execute(buildStudyViewFilterContext(studyViewFilter), genomicDataBinFilters));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalDataCountItem> getGenericAssayDataBinCounts(StudyViewFilter studyViewFilter,
                                                                    List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        return ClinicalDataCountItemUtil.generateDataCountItems(genericAssayUseCases.getGenericAssayDataBinCounts().execute(buildStudyViewFilterContext(studyViewFilter), genericAssayDataBinFilters));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public Map<String, ClinicalDataType> getClinicalAttributeDataTypeMap(StudyViewFilter studyViewFilter) {
        return getClinicalAttributesDataTypeMapUseCase.execute();
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter,
                                                             List<String> filteredAttributes){
        return clinicalDataUseCases.getClinicalDataCountsUseCase().execute(buildStudyViewFilterContext(studyViewFilter)
                , filteredAttributes);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds){
        return getClinicalAttributesForStudiesUseCase.execute(studyIds);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter){
        return StudyViewColumnarServiceUtil.mergeCaseListCounts(getCaseListDataCountsUseCase.execute(buildStudyViewFilterContext(studyViewFilter)));
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds){
        return clinicalDataUseCases.getPatientClinicalDataUseCase().execute(buildStudyViewFilterContext(studyViewFilter), attributeIds);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter,List<String> attributeIds){
        return clinicalDataUseCases.getSampleClinicalDataUseCase().execute(buildStudyViewFilterContext(studyViewFilter), attributeIds);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<GenomicDataCountItem> getCNACountsByGeneSpecific(StudyViewFilter studyViewFilter,
                                                                  List<GenomicDataFilter> genomicDataFilters){
        return genomicDataUseCases.getCNACountsByGeneSpecificUseCase().execute(buildStudyViewFilterContext(studyViewFilter), genomicDataFilters);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilter studyViewFilter,
                                                                     List<GenericAssayDataFilter> genericAssayDataFilters){
        return genericAssayUseCases.getGenericAssayDataCountsUseCase().execute(buildStudyViewFilterContext(studyViewFilter), genericAssayDataFilters);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(StudyViewFilter studyViewFilter,
                                                                      List<GenomicDataFilter> genomicDataFilters){
        List<GenomicDataCountItem> genomicDataCountItemList = new ArrayList<>();
        for (GenomicDataFilter genomicDataFilter : genomicDataFilters) {
            Map<String, Integer> counts =
                    genomicDataUseCases.getMutationCountsUseCase().execute(buildStudyViewFilterContext(studyViewFilter), genomicDataFilter);
            genomicDataCountItemList.add(StudyViewColumnarServiceUtil.createGenomicDataCountItemFromMutationCounts(genomicDataFilter, counts));
        }
        return genomicDataCountItemList;
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(StudyViewFilter studyViewFilter,
                                                                          List<GenomicDataFilter> genomicDataFilters){
        return genomicDataUseCases.getMutationCountsByTypeUseCase().execute(buildStudyViewFilterContext(studyViewFilter), genomicDataFilters);
    }

    @Cacheable(
            cacheResolver = "staticRepositoryCacheOneResolver",
            condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    public List<ClinicalData> getClinicalDataForXyPlot(StudyViewFilter studyViewFilter, List<String> attributeIds,
                                                       boolean shouldFilterNonEmptyClinicalData){
        return clinicalDataUseCases.getClinicalDataForXyPlotUseCase().execute(buildStudyViewFilterContext(studyViewFilter),attributeIds,
                shouldFilterNonEmptyClinicalData);
    }

    private StudyViewFilterContext buildStudyViewFilterContext(StudyViewFilter studyViewFilter){
        return StudyViewFilterFactory.make(studyViewFilter,this.customDataFilterUtil, getGenericAssayProfilesMap()  );
    }

    private Map<DataSource, List<MolecularProfile>> getGenericAssayProfilesMap() {
        if (genericAssayProfilesMap.isEmpty()) {
            buildGenericAssayProfilesMap();
        }
        return genericAssayProfilesMap;
    }

    private void buildGenericAssayProfilesMap() {
        genericAssayProfilesMap = genericAssayUseCases.getGenericAssayProfilesUseCase().execute()
                .stream()
                .collect(Collectors.groupingBy(ca -> ca.getPatientLevel() ? DataSource.PATIENT : DataSource.SAMPLE));
    }

}
