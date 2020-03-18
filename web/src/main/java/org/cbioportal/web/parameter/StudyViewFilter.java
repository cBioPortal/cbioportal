package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.web.parameter.filter.BooleanOperatorJoinedFilters;
import org.cbioportal.web.parameter.filter.PatientTreatmentFilter;
import org.cbioportal.web.parameter.filter.SampleTreatmentFilter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class StudyViewFilter implements Serializable {

    @Size(min = 1)
    private List<SampleIdentifier> sampleIdentifiers;
    @Size(min = 1)
    private List<String> studyIds;
    private List<ClinicalDataFilter> clinicalDataFilters;
    private List<GeneFilter> geneFilters;
    private BooleanOperatorJoinedFilters<
        SampleIdentifier,
        Map<String, SampleTreatmentRow>,
        BooleanOperatorJoinedFilters<
            SampleIdentifier,
            Map<String, SampleTreatmentRow>,
            SampleTreatmentFilter>>
        sampleTreatmentFilters;
    private BooleanOperatorJoinedFilters<
        SampleIdentifier,
        Map<String, PatientTreatmentRow>,
        BooleanOperatorJoinedFilters<
            SampleIdentifier,
            Map<String, PatientTreatmentRow>,
            PatientTreatmentFilter>>
        patientTreatmentFilters;
	private Boolean withMutationData;
	private Boolean withCNAData;
	private Boolean withFusionData;

    @AssertTrue
    private boolean isEitherSampleIdentifiersOrStudyIdsPresent() {
        return sampleIdentifiers != null ^ studyIds != null;
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInClinicalDataIntervalFilters() {
        long invalidCount = 0;

        if (clinicalDataFilters != null) {
            invalidCount = clinicalDataFilters.stream()
                .flatMap(f -> f.getValues().stream())
                .filter(Objects::nonNull)
                .filter(v -> v.getValue() != null == (v.getStart() != null || v.getEnd() != null))
                .count();
        }

        return invalidCount == 0;
    }

    public List<SampleIdentifier> getSampleIdentifiers() {
        return sampleIdentifiers;
    }

    public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
        this.sampleIdentifiers = sampleIdentifiers;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }
    
    public List<ClinicalDataFilter> getClinicalDataFilters() {
        return clinicalDataFilters;
    }

    public void setClinicalDataFilters(List<ClinicalDataFilter> clinicalDataFilters) {
        this.clinicalDataFilters = clinicalDataFilters;
    }

    public List<GeneFilter> getGeneFilters() {
        return geneFilters;
    }

    public void setGeneFilters(List<GeneFilter> geneFilters) {
        this.geneFilters = geneFilters;
    }

    public Boolean getWithMutationData() {
        return withMutationData;
    }

    public void setWithMutationData(Boolean withMutationData) {
        this.withMutationData = withMutationData;
    }

    public Boolean getWithCNAData() {
        return withCNAData;
    }

    public void setWithCNAData(Boolean withCNAData) {
        this.withCNAData = withCNAData;
    }

    public Boolean getWithFusionData() {
        return withFusionData;
    }

    public void setWithFusionData(Boolean withFusionData) {
        this.withFusionData = withFusionData;
    }

    public BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, SampleTreatmentRow>, BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, SampleTreatmentRow>, SampleTreatmentFilter>> getSampleTreatmentFilters() {
        return sampleTreatmentFilters;
    }

    public void setSampleTreatmentFilters(BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, SampleTreatmentRow>, BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, SampleTreatmentRow>, SampleTreatmentFilter>> sampleTreatmentFilters) {
        this.sampleTreatmentFilters = sampleTreatmentFilters;
    }

    public BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, PatientTreatmentRow>, BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, PatientTreatmentRow>, PatientTreatmentFilter>> getPatientTreatmentFilters() {
        return patientTreatmentFilters;
    }

    public void setPatientTreatmentFilters(BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, PatientTreatmentRow>, BooleanOperatorJoinedFilters<SampleIdentifier, Map<String, PatientTreatmentRow>, PatientTreatmentFilter>> patientTreatmentFilters) {
        this.patientTreatmentFilters = patientTreatmentFilters;
    }
}
