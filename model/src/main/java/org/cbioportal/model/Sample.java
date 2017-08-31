package org.cbioportal.model;

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
    private String stableId;
    private SampleType sampleType;
    private Integer patientId;
    private String patientStableId;
    private String typeOfCancerId;
    private Patient patient;
    private String cancerStudyIdentifier;

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

    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    public void setTypeOfCancerId(String typeOfCancerId) {
        this.typeOfCancerId = typeOfCancerId;
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
}