package org.cbioportal.clinical_data.usecase;

import org.cbioportal.clinical_attributes.usecase.GetClinicalAttributesForStudiesUseCase;
import org.cbioportal.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.patient.usecase.GetFilteredPatientCountUseCase;
import org.cbioportal.sample.usecase.GetFilteredSamplesCountUseCase;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetClinicalDataCountsUseCase {
    private final ClinicalDataRepository clinicalDataRepository;
    private final GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase;
    private final GetFilteredSamplesCountUseCase getFilteredSamplesCountUseCase;
    private final GetFilteredPatientCountUseCase getFilteredPatientCountUseCase;


    public GetClinicalDataCountsUseCase(ClinicalDataRepository clinicalDataRepository, GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase, GetFilteredSamplesCountUseCase getFilteredSamplesCountUseCase, GetFilteredPatientCountUseCase getFilteredPatientCountUseCase) {
        this.clinicalDataRepository = clinicalDataRepository;
        this.getClinicalAttributesForStudiesUseCase = getClinicalAttributesForStudiesUseCase;
        this.getFilteredSamplesCountUseCase = getFilteredSamplesCountUseCase;
        this.getFilteredPatientCountUseCase = getFilteredPatientCountUseCase;
    }

    public List<ClinicalDataCountItem> execute(StudyViewFilterContext studyViewFilterContext,
                          List<String> filteredAttributes){

        List<String> involvedCancerStudies = studyViewFilterContext.customDataFilterCancerStudies();

        var result = clinicalDataRepository.getClinicalDataCounts(studyViewFilterContext, filteredAttributes);

        // normalize data counts so that values like TRUE, True, and true are all merged in one count
        result.forEach(item -> item.setCounts(StudyViewColumnarServiceUtil.normalizeDataCounts(item.getCounts())));

        // attributes may be missing in result set because they have been filtered out
        // e.g. if the filtered samples happen to have no SEX data, they will not appear in the list
        // even though the inferred value of those attributes is NA
        // the following code restores these counts for missing attributes
        if (result.size() != filteredAttributes.size()) {
            var attributes = getClinicalAttributesForStudiesUseCase.execute(involvedCancerStudies)
                    .stream()
                    .filter(attribute -> filteredAttributes.contains(attribute.getAttrId()))
                    .toList();

            Integer filteredSampleCount = getFilteredSamplesCountUseCase.execute(studyViewFilterContext);
            Integer filteredPatientCount = getFilteredPatientCountUseCase.execute(studyViewFilterContext);

            result = StudyViewColumnarServiceUtil.addClinicalDataCountsForMissingAttributes(
                    result,
                    attributes,
                    filteredSampleCount,
                    filteredPatientCount
            );
        }

        return StudyViewColumnarServiceUtil.mergeClinicalDataCounts(result);
    }
}
