package org.cbioportal.web.parameter;

import java.util.List;

public final class CategorizedClinicalDataCountFilter {

    public static Builder getBuilder() {
        return new Builder();
    }
    private final List<ClinicalDataFilter> sampleNumericalClinicalDataFilters;
    private final List<ClinicalDataFilter> sampleCategoricalClinicalDataFilters;
    private final List<ClinicalDataFilter> patientNumericalClinicalDataFilters;
    private final List<ClinicalDataFilter> patientCategoricalClinicalDataFilters;

    private CategorizedClinicalDataCountFilter(Builder builder) {
       this.sampleCategoricalClinicalDataFilters = builder.sampleCategoricalClinicalDataFilters;
       this.sampleNumericalClinicalDataFilters = builder.sampleNumericalClinicalDataFilters;
       this.patientCategoricalClinicalDataFilters = builder.patientCategoricalClinicalDataFilters;
       this.patientNumericalClinicalDataFilters = builder.patientNumericalClinicalDataFilters;
    }

    public List<ClinicalDataFilter> getSampleNumericalClinicalDataFilters() {
        return sampleNumericalClinicalDataFilters;
    }

    public List<ClinicalDataFilter> getSampleCategoricalClinicalDataFilters() {
        return sampleCategoricalClinicalDataFilters;
    }

    public List<ClinicalDataFilter> getPatientNumericalClinicalDataFilters() {
        return patientNumericalClinicalDataFilters;
    }

    public List<ClinicalDataFilter> getPatientCategoricalClinicalDataFilters() {
        return patientCategoricalClinicalDataFilters;
    }
    
    public static class Builder {
        private List<ClinicalDataFilter> sampleNumericalClinicalDataFilters;
        private List<ClinicalDataFilter> sampleCategoricalClinicalDataFilters;
        private List<ClinicalDataFilter> patientNumericalClinicalDataFilters;
        private List<ClinicalDataFilter> patientCategoricalClinicalDataFilters;

        private Builder(){

        }
        public Builder setSampleCategoricalClinicalDataFilters(List<ClinicalDataFilter> sampleCategoricalClinicalDataFilters) {
            this.sampleCategoricalClinicalDataFilters = sampleCategoricalClinicalDataFilters;
            return this;
        }

        public Builder setSampleNumericalClinicalDataFilters(List<ClinicalDataFilter> sampleNumericalClinicalDataFilters) {
            this.sampleNumericalClinicalDataFilters = sampleNumericalClinicalDataFilters;
            return this;
        }

        public Builder setPatientCategoricalClinicalDataFilters(List<ClinicalDataFilter> patientCategoricalClinicalDataFilters) {
            this.patientCategoricalClinicalDataFilters = patientCategoricalClinicalDataFilters;
            return this;
        }

        public Builder setPatientNumericalClinicalDataFilters(List<ClinicalDataFilter> patientNumericalClinicalDataFilters) {
            this.patientNumericalClinicalDataFilters = patientNumericalClinicalDataFilters;
            return this; 
        }
        
        public CategorizedClinicalDataCountFilter build() {
            return new CategorizedClinicalDataCountFilter(this);
        }
    }
}
