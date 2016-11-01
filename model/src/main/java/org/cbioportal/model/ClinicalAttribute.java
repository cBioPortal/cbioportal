package org.cbioportal.model;

import java.io.Serializable;

public class ClinicalAttribute implements Serializable {

    private String attrId;
    private String displayName;
    private String description;
    private String datatype;
    private Boolean patientAttribute;
    private String priority;
    private Integer cancerStudyId;

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Boolean getPatientAttribute() {
        return patientAttribute;
    }

    public void setPatientAttribute(Boolean patientAttribute) {
        this.patientAttribute = patientAttribute;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public Integer getCancerStudyId() {
        return cancerStudyId;
    }
    
    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }
}

