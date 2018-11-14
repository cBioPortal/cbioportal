package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class ClinicalAttribute implements Serializable {

    @NotNull
    private String attrId;
    @NotNull
    private String displayName;
    private String description;
    private String datatype;
    @NotNull
    private Boolean patientAttribute;
    private String priority;
    private Integer cancerStudyId;
    @NotNull
    private String cancerStudyIdentifier;

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

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }
}
