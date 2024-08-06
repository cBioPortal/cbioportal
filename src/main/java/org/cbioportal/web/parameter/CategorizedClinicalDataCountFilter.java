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

    private final List<GenomicDataFilter> sampleNumericalGenomicDataFilters;
    private final List<GenomicDataFilter> sampleCategoricalGenomicDataFilters;
    private final List<GenomicDataFilter> patientNumericalGenomicDataFilters;
    private final List<GenomicDataFilter> patientCategoricalGenomicDataFilters;

    private final List<GenericAssayDataFilter> sampleNumericalGenericAssayDataFilters;
    private final List<GenericAssayDataFilter> sampleCategoricalGenericAssayDataFilters;
    private final List<GenericAssayDataFilter> patientNumericalGenericAssayDataFilters;
    private final List<GenericAssayDataFilter> patientCategoricalGenericAssayDataFilters;
    private CategorizedClinicalDataCountFilter(Builder builder) {
       this.sampleCategoricalClinicalDataFilters = builder.sampleCategoricalClinicalDataFilters;
       this.sampleNumericalClinicalDataFilters = builder.sampleNumericalClinicalDataFilters;
       this.patientCategoricalClinicalDataFilters = builder.patientCategoricalClinicalDataFilters;
       this.patientNumericalClinicalDataFilters = builder.patientNumericalClinicalDataFilters;
       this.sampleCategoricalGenomicDataFilters = builder.sampleCategoricalGenomicDataFilters;
       this.sampleNumericalGenomicDataFilters = builder.sampleNumericalGenomicDataFilters;
       this.patientCategoricalGenomicDataFilters = builder.patientCategoricalGenomicDataFilters;
       this.patientNumericalGenomicDataFilters = builder.patientNumericalGenomicDataFilters;
       this.sampleCategoricalGenericAssayDataFilters = builder.sampleCategoricalGenericAssayDataFilters;
       this.sampleNumericalGenericAssayDataFilters = builder.sampleNumericalGenericAssayDataFilters;
       this.patientCategoricalGenericAssayDataFilters = builder.patientCategoricalGenericAssayDataFilters;
       this.patientNumericalGenericAssayDataFilters = builder.patientNumericalGenericAssayDataFilters;
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
        private List<ClinicalDataFilter> sampleNumericalClinicalDataFilters;
        private List<ClinicalDataFilter> sampleCategoricalClinicalDataFilters;
        private List<ClinicalDataFilter> patientNumericalClinicalDataFilters;
        private List<ClinicalDataFilter> patientCategoricalClinicalDataFilters;
        private List<GenomicDataFilter> sampleNumericalGenomicDataFilters;
        private List<GenomicDataFilter> sampleCategoricalGenomicDataFilters;
        private List<GenomicDataFilter> patientNumericalGenomicDataFilters;
        private List<GenomicDataFilter> patientCategoricalGenomicDataFilters;
        private List<GenericAssayDataFilter> sampleNumericalGenericAssayDataFilters;
        private List<GenericAssayDataFilter> sampleCategoricalGenericAssayDataFilters;
        private List<GenericAssayDataFilter> patientNumericalGenericAssayDataFilters;
        private List<GenericAssayDataFilter> patientCategoricalGenericAssayDataFilters;

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
        
        public CategorizedClinicalDataCountFilter build() {
            return new CategorizedClinicalDataCountFilter(this);
        }
    }
}
