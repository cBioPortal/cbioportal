package org.cbioportal.legacy.web.parameter;

import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.enums.DataSource;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

// TODO Remove
public final class CategorizedGenericAssayDataCountFilter {

    public static Builder getBuilder() {
        return new Builder();
    }

    public static Builder getBuilder(@Nullable Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap, StudyViewFilter studyViewFilter) {
        if (genericAssayProfilesMap == null) {
            return new Builder();
        }
        return new Builder(genericAssayProfilesMap, studyViewFilter);
    }
    private final List<GenericAssayDataFilter> sampleNumericalGenericAssayDataFilters;
    private final List<GenericAssayDataFilter> sampleCategoricalGenericAssayDataFilters;
    private final List<GenericAssayDataFilter> patientNumericalGenericAssayDataFilters;
    private final List<GenericAssayDataFilter> patientCategoricalGenericAssayDataFilters;
    private CategorizedGenericAssayDataCountFilter(Builder builder) {
        this.sampleCategoricalGenericAssayDataFilters = builder.sampleCategoricalGenericAssayDataFilters;
        this.sampleNumericalGenericAssayDataFilters = builder.sampleNumericalGenericAssayDataFilters;
        this.patientCategoricalGenericAssayDataFilters = builder.patientCategoricalGenericAssayDataFilters;
        this.patientNumericalGenericAssayDataFilters = builder.patientNumericalGenericAssayDataFilters;
    }
    
    public List<GenericAssayDataFilter> getSampleNumericalGenericAssayDataFilters() {
        return sampleNumericalGenericAssayDataFilters;
    }

    public List<GenericAssayDataFilter> getSampleCategoricalGenericAssayDataFilters() {
        return sampleCategoricalGenericAssayDataFilters;
    }

    public List<GenericAssayDataFilter> getPatientNumericalGenericAssayDataFilters() {
        return patientNumericalGenericAssayDataFilters;
    }

    public List<GenericAssayDataFilter> getPatientCategoricalGenericAssayDataFilters() {
        return patientCategoricalGenericAssayDataFilters;
    }

    public static class Builder {
        private List<GenericAssayDataFilter> sampleNumericalGenericAssayDataFilters;
        private List<GenericAssayDataFilter> sampleCategoricalGenericAssayDataFilters;
        private List<GenericAssayDataFilter> patientNumericalGenericAssayDataFilters;
        private List<GenericAssayDataFilter> patientCategoricalGenericAssayDataFilters;

        private Builder(){

        }

        private Builder(Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap, StudyViewFilter studyViewFilter){
            if ((studyViewFilter.getGenericAssayDataFilters() == null || genericAssayProfilesMap.isEmpty())) {
                return ;
            }

            // No BINARY in the database yet
            if (genericAssayProfilesMap.containsKey(DataSource.SAMPLE)) {
                List<String> sampleNumericalProfileTypes = genericAssayProfilesMap.get(DataSource.SAMPLE)
                        .stream().filter(profile -> profile.getDatatype().equals("LIMIT-VALUE"))
                        .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
                        .toList();
                sampleNumericalGenericAssayDataFilters = studyViewFilter.getGenericAssayDataFilters().stream()
                        .filter(genericAssayDataFilter -> sampleNumericalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
                        .toList();
                List<String> sampleCategoricalProfileTypes = genericAssayProfilesMap.get(DataSource.SAMPLE)
                        .stream().filter(profile -> profile.getDatatype().equals("CATEGORICAL") || profile.getDatatype().equals("BINARY"))
                        .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
                        .toList();
                sampleCategoricalGenericAssayDataFilters = studyViewFilter.getGenericAssayDataFilters().stream()
                        .filter(genericAssayDataFilter -> sampleCategoricalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
                        .toList();
            }

            // patient level profile only have categorical for now
            if (genericAssayProfilesMap.containsKey(DataSource.PATIENT)) {
                List<String> patientCategoricalProfileTypes = genericAssayProfilesMap.get(DataSource.PATIENT)
                        .stream().filter(profile -> profile.getDatatype().equals("CATEGORICAL") || profile.getDatatype().equals("BINARY"))
                        .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
                        .toList();
                patientCategoricalGenericAssayDataFilters = studyViewFilter.getGenericAssayDataFilters().stream()
                        .filter(genericAssayDataFilter -> patientCategoricalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
                        .toList();
            }
        }

        public Builder setSampleCategoricalGenericAssayDataFilters(List<GenericAssayDataFilter> sampleCategoricalGenericAssayDataFilters) {
            this.sampleCategoricalGenericAssayDataFilters = sampleCategoricalGenericAssayDataFilters;
            return this;
        }

        public Builder setSampleNumericalGenericAssayDataFilters(List<GenericAssayDataFilter> sampleNumericalGenericAssayDataFilters) {
            this.sampleNumericalGenericAssayDataFilters = sampleNumericalGenericAssayDataFilters;
            return this;
        }

        public Builder setPatientCategoricalGenericAssayDataFilters(List<GenericAssayDataFilter> patientCategoricalGenericAssayDataFilters) {
            this.patientCategoricalGenericAssayDataFilters = patientCategoricalGenericAssayDataFilters;
            return this;
        }

        public Builder setPatientNumericalGenericAssayDataFilters(List<GenericAssayDataFilter> patientNumericalGenericAssayDataFilters) {
            this.patientNumericalGenericAssayDataFilters = patientNumericalGenericAssayDataFilters;
            return this;
        }

        public CategorizedGenericAssayDataCountFilter build() {
            return new CategorizedGenericAssayDataCountFilter(this);
        }
    }
}
