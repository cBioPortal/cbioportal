package org.cbioportal.application.file.model;

public class ClinicalAttribute {
    public static final ClinicalAttribute PATIENT_ID = new ClinicalAttribute(
        "Patient Identifier",
        "Patient Identifier",
        "STRING",
        "1",
        "PATIENT_ID");
    public static final ClinicalAttribute SAMPLE_ID = new ClinicalAttribute(
        "Sample Identifier",
        "Sample Identifier",
        "STRING",
        "1",
        "SAMPLE_ID");
    private String displayName;
    private String description;
    private String datatype;
    private String priority;
    private String attributeId;

    public ClinicalAttribute() {
    }

    public ClinicalAttribute(String displayName, String description, String datatype, String priority, String attributeId) {
        this.displayName = displayName;
        this.description = description;
        this.datatype = datatype;
        this.priority = priority;
        this.attributeId = attributeId;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public String toString() {
        return "ClinicalAttribute{" +
            "displayName='" + displayName + '\'' +
            ", description='" + description + '\'' +
            ", datatype='" + datatype + '\'' +
            ", priority='" + priority + '\'' +
            ", attributeId='" + attributeId + '\'' +
            '}';
    }
}