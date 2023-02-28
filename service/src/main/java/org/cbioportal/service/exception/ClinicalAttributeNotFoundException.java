package org.cbioportal.service.exception;

public class ClinicalAttributeNotFoundException extends Exception {

    private String studyId;
    private String clinicalAttributeId;

    public ClinicalAttributeNotFoundException(String studyId, String clinicalAttributeId) {
        super();
        this.studyId = studyId;
        this.clinicalAttributeId = clinicalAttributeId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getClinicalAttributeId() {
        return clinicalAttributeId;
    }

    public void setClinicalAttributeId(String clinicalAttributeId) {
        this.clinicalAttributeId = clinicalAttributeId;
    }
}
