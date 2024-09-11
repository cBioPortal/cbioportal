package org.cbioportal.web.parameter;

import java.util.List;

public final class CategorizedGenomicDataCountFilter {

    public static Builder getBuilder() {
        return new Builder();
    }
    
    private final List<GenomicDataFilter> sampleNumericalGenomicDataFilters;
    private final List<GenomicDataFilter> sampleCategoricalGenomicDataFilters;
    private final List<GenomicDataFilter> patientNumericalGenomicDataFilters;
    private final List<GenomicDataFilter> patientCategoricalGenomicDataFilters;
    
    private CategorizedGenomicDataCountFilter(Builder builder) {
        this.sampleCategoricalGenomicDataFilters = builder.sampleCategoricalGenomicDataFilters;
        this.sampleNumericalGenomicDataFilters = builder.sampleNumericalGenomicDataFilters;
        this.patientCategoricalGenomicDataFilters = builder.patientCategoricalGenomicDataFilters;
        this.patientNumericalGenomicDataFilters = builder.patientNumericalGenomicDataFilters;
    }

    public List<GenomicDataFilter> getSampleNumericalGenomicDataFilters() {
        return sampleNumericalGenomicDataFilters;
    }

    public List<GenomicDataFilter> getSampleCategoricalGenomicDataFilters() {
        return sampleCategoricalGenomicDataFilters;
    }

    public List<GenomicDataFilter> getPatientNumericalGenomicDataFilters() {
        return patientNumericalGenomicDataFilters;
    }

    public List<GenomicDataFilter> getPatientCategoricalGenomicDataFilters() {
        return patientCategoricalGenomicDataFilters;
    }
    
    public static class Builder {
        private List<GenomicDataFilter> sampleNumericalGenomicDataFilters;
        private List<GenomicDataFilter> sampleCategoricalGenomicDataFilters;
        private List<GenomicDataFilter> patientNumericalGenomicDataFilters;
        private List<GenomicDataFilter> patientCategoricalGenomicDataFilters;

        private Builder(){

        }

        public Builder setSampleCategoricalGenomicDataFilters(List<GenomicDataFilter> sampleCategoricalGenomicDataFilters) {
            this.sampleCategoricalGenomicDataFilters = sampleCategoricalGenomicDataFilters;
            return this;
        }

        public Builder setSampleNumericalGenomicDataFilters(List<GenomicDataFilter> sampleNumericalGenomicDataFilters) {
            this.sampleNumericalGenomicDataFilters = sampleNumericalGenomicDataFilters;
            return this;
        }

        public Builder setPatientCategoricalGenomicDataFilters(List<GenomicDataFilter> patientCategoricalGenomicDataFilters) {
            this.patientCategoricalGenomicDataFilters = patientCategoricalGenomicDataFilters;
            return this;
        }

        public Builder setPatientNumericalGenomicDataFilters(List<GenomicDataFilter> patientNumericalGenomicDataFilters) {
            this.patientNumericalGenomicDataFilters = patientNumericalGenomicDataFilters;
            return this;
        }
        
        public CategorizedGenomicDataCountFilter build() {
            return new CategorizedGenomicDataCountFilter(this);
        }
    }
}
