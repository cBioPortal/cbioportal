package org.cbioportal.file.model;

public record ClinicalAttribute(
    String displayName,
    String description,
    String datatype,
    String priority,
    String attributeId
) {
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
}