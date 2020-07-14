package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.cbioportal.model.GeneFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class StudyViewFilter implements Serializable {

    @Size(min = 1)
    private List<SampleIdentifier> sampleIdentifiers;
    @Size(min = 1)
    private List<String> studyIds;
    private List<ClinicalDataFilter> clinicalDataFilters;
    private List<GeneFilter> geneFilters;
    private AndedSampleTreatmentFilters sampleTreatmentFilters;
    private AndedPatientTreatmentFilters patientTreatmentFilters;
	private List<List<String>> genomicProfiles;
    private List<GenomicDataFilter> genomicDataFilters;
    private List<GenericAssayDataFilter> genericAssayDataFilters;
    private List<List<String>> caseLists;
    private List<ClinicalDataFilter> customDataFilters;
    private Map<String,Boolean> selectedTiers;
    private boolean includeDriver;
    private boolean includeVUS;
    private boolean includeUnknownOncogenicity;
    private boolean includeUnknownTier;
    private boolean includeGermline;
    private boolean includeSomatic;
    private boolean includeUnknownStatus;

    public Map<String, Boolean> getSelectedTiers() {
        return selectedTiers;
    }

    public void setSelectedTiers(Map<String, Boolean> selectedTiers) {
        this.selectedTiers = selectedTiers;
    }

    public boolean isIncludeDriver() {
        return includeDriver;
    }

    public void setIncludeDriver(boolean includeDriver) {
        this.includeDriver = includeDriver;
    }

    public boolean isIncludeVUS() {
        return includeVUS;
    }

    public void setIncludeVUS(boolean includeVUS) {
        this.includeVUS = includeVUS;
    }

    public boolean isIncludeUnknownOncogenicity() {
        return includeUnknownOncogenicity;
    }

    public void setIncludeUnknownOncogenicity(boolean includeUnknownOncogenicity) {
        this.includeUnknownOncogenicity = includeUnknownOncogenicity;
    }

    public boolean isIncludeUnknownTier() {
        return includeUnknownTier;
    }

    public void setIncludeUnknownTier(boolean includeUnknownTier) {
        this.includeUnknownTier = includeUnknownTier;
    }

    public boolean isIncludeGermline() {
        return includeGermline;
    }

    public void setIncludeGermline(boolean includeGermline) {
        this.includeGermline = includeGermline;
    }

    public boolean isIncludeSomatic() {
        return includeSomatic;
    }

    public void setIncludeSomatic(boolean includeSomatic) {
        this.includeSomatic = includeSomatic;
    }

    public boolean isIncludeUnknownStatus() {
        return includeUnknownStatus;
    }

    public void setIncludeUnknownStatus(boolean includeUnknownStatus) {
        this.includeUnknownStatus = includeUnknownStatus;
    }

    @AssertTrue
    private boolean isEitherSampleIdentifiersOrStudyIdsPresent() {
        return sampleIdentifiers != null ^ studyIds != null;
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInClinicalDataIntervalFilters() {
        return validateDataFilters(clinicalDataFilters);
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInGenomicDataIntervalFilters() {
        return validateDataFilters(genomicDataFilters);
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInGenericAssayDataIntervalFilters() {
        return validateDataFilters(genericAssayDataFilters);
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInCustomDataFilters() {
        return validateDataFilters(customDataFilters);
    }

    private <T extends DataFilter> boolean validateDataFilters(List<T> dataFilters) {
        long invalidCount = 0;

        if (dataFilters != null) {
            invalidCount = dataFilters.stream()
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

    public List<List<String>> getGenomicProfiles() {
        return genomicProfiles;
    }

    public void setGenomicProfiles(List<List<String>> genomicProfiles) {
        this.genomicProfiles = genomicProfiles;
    }

    public List<GenomicDataFilter> getGenomicDataFilters() {
        return genomicDataFilters;
    }

    public void setGenomicDataFilters(List<GenomicDataFilter> genomicDataFilters) {
        this.genomicDataFilters = genomicDataFilters;
    }

    public AndedSampleTreatmentFilters getSampleTreatmentFilters() {
        return sampleTreatmentFilters;
    }

    public void setSampleTreatmentFilters(AndedSampleTreatmentFilters sampleTreatmentFilters) {
        this.sampleTreatmentFilters = sampleTreatmentFilters;
    }

    public AndedPatientTreatmentFilters getPatientTreatmentFilters() {
        return patientTreatmentFilters;
    }

    public void setPatientTreatmentFilters(AndedPatientTreatmentFilters patientTreatmentFilters) {
        this.patientTreatmentFilters = patientTreatmentFilters;
    }

    public List<List<String>> getCaseLists() {
        return caseLists;
    }

    public void setCaseLists(List<List<String>> caseLists) {
        this.caseLists = caseLists;
    }

	public List<GenericAssayDataFilter> getGenericAssayDataFilters() {
		return genericAssayDataFilters;
	}

	public void setGenericAssayDataFilters(List<GenericAssayDataFilter> genericAssayDataFilters) {
		this.genericAssayDataFilters = genericAssayDataFilters;
	}

    public List<ClinicalDataFilter> getCustomDataFilters() {
        return customDataFilters;
    }

    public void setCustomDataFilters(List<ClinicalDataFilter> customDataFilters) {
        this.customDataFilters = customDataFilters;
    }

}
