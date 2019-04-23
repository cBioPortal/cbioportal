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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attrId == null) ? 0 : attrId.hashCode());
        result = prime * result + ((patientAttribute == null) ? 0 : patientAttribute.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClinicalAttribute other = (ClinicalAttribute) obj;
        if (attrId == null) {
            if (other.attrId != null)
                return false;
        } else if (!attrId.equals(other.attrId))
            return false;
        if (patientAttribute == null) {
            if (other.patientAttribute != null)
                return false;
        } else if (!patientAttribute.equals(other.patientAttribute))
            return false;
        return true;
    }
    
}
