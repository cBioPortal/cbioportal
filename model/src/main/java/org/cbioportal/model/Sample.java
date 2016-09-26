package org.cbioportal.model;

import org.cbioportal.model.summary.SampleSummary;

public class Sample extends SampleSummary {

    public enum SampleType {

        PRIMARY_SOLID_TUMOR("Primary Solid Tumor"),
        RECURRENT_SOLID_TUMOR("Recurrent Solid Tumor"),
        PRIMARY_BLOOD_TUMOR("Primary Blood Tumor"),
        RECURRENT_BLOOD_TUMOR("Recurrent Blood Tumor"),
        METASTATIC("Metastatic"),
        BLOOD_NORMAL("Blood Derived Normal"),
        SOLID_NORMAL("Solid Tissues Normal");

        private String value;

        SampleType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SampleType fromString(String value) {

            if (value != null) {
                for (SampleType sampleType : SampleType.values()) {
                    if (value.equalsIgnoreCase(sampleType.value)) {
                        return sampleType;
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private Patient patient;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}