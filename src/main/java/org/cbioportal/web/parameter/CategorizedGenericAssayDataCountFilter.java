package org.cbioportal.web.parameter;

import java.util.List;

public final class CategorizedGenericAssayDataCountFilter {

    public static Builder getBuilder() {
        return new Builder();
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
