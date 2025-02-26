package org.cbioportal.domain.studyview;

import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.enums.DataSource;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.parameter.CategorizedGenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.CustomSampleIdentifier;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.shared.DataFilterUtil;

import java.util.List;
import java.util.Map;

public abstract class StudyViewFilterFactory {
    private StudyViewFilterFactory() {}

    public static StudyViewFilterContext make(StudyViewFilter base,
                                                CustomDataFilterUtil customDataFilterUtil,
                                              Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap) {
        List<CustomSampleIdentifier> customSampleIdentifiers = customDataFilterUtil.extractCustomDataSamples(base);
        List<String> involvedCancerStudies = customDataFilterUtil.extractInvolvedCancerStudies(base);
        CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter =
                CategorizedGenericAssayDataCountFilter.getBuilder(genericAssayProfilesMap, base).build();


        // Merge data filters
        if (base.getGenomicDataFilters() != null && !base.getGenomicDataFilters().isEmpty()) {
            List<GenomicDataFilter> mergedGenomicDataFilters =
                    DataFilterUtil.mergeDataFilters(base.getGenomicDataFilters());
            base.setGenomicDataFilters(mergedGenomicDataFilters);
        }
        if (base.getClinicalDataFilters() != null && !base.getClinicalDataFilters().isEmpty()) {
            List<ClinicalDataFilter> mergedClinicalDataFilters =
                    DataFilterUtil.mergeDataFilters(base.getClinicalDataFilters());
            base.setClinicalDataFilters(mergedClinicalDataFilters);
        }
        if (base.getGenericAssayDataFilters() != null && !base.getGenericAssayDataFilters().isEmpty()) {
            List<GenericAssayDataFilter> mergedGenericAssayDataFilters =
                    DataFilterUtil.mergeDataFilters(base.getGenericAssayDataFilters());
            base.setGenericAssayDataFilters(mergedGenericAssayDataFilters);
        }

        return make(base, customSampleIdentifiers, involvedCancerStudies, categorizedGenericAssayDataCountFilter);
    }

    public static StudyViewFilterContext make(StudyViewFilter base,
                                              List<CustomSampleIdentifier> customSampleIdentifiers,
                                              List<String> involvedCancerStudies,
                                              CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter){
        return new StudyViewFilterContext(base.getSampleIdentifiers(), base.getStudyIds(),
                base.getClinicalDataFilters(), base.getGeneFilters(), base.getStructuralVariantFilters(),
                base.getSampleTreatmentFilters(), base.getSampleTreatmentGroupFilters(),
                base.getSampleTreatmentTargetFilters(), base.getPatientTreatmentFilters(),
                base.getPatientTreatmentGroupFilters(), base.getPatientTreatmentTargetFilters(),
                base.getGenomicProfiles(), base.getGenomicDataFilters(), base.getGenericAssayDataFilters(),
                base.getCaseLists(), base.getCustomDataFilters(), base.getAlterationFilter(),
                base.getClinicalEventFilters(), base.getMutationDataFilters(),customSampleIdentifiers,
                involvedCancerStudies, categorizedGenericAssayDataCountFilter);
    }
}
