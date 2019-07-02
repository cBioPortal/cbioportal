package org.cbioportal.model;

import javax.validation.constraints.NotNull;

public class Sample extends UniqueKeyBase {

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

    private Integer internalId;
    @NotNull
    private String stableId;
    private SampleType sampleType;
    private Integer patientId;
    @NotNull
    private String patientStableId;
    private Patient patient;
    @NotNull
    private String cancerStudyIdentifier;
    private Boolean sequenced;
    private Boolean copyNumberSegmentPresent;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientStableId() {
        return patientStableId;
    }

    public void setPatientStableId(String patientStableId) {
        this.patientStableId = patientStableId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public Boolean getSequenced() {
        return sequenced;
    }

    public void setSequenced(Boolean sequenced) {
        this.sequenced = sequenced;
    }

    public Boolean getCopyNumberSegmentPresent() {
        return copyNumberSegmentPresent;
    }

    public void setCopyNumberSegmentPresent(Boolean copyNumberSegmentPresent) {
        this.copyNumberSegmentPresent = copyNumberSegmentPresent;
    }
}
