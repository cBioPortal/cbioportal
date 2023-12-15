package org.cbioportal.model;

/**
 * Data that can be binned, clinical or custom
 */
public interface Binnable {
    String getAttrId();
    String getAttrValue();
    String getSampleId();
    String getPatientId();
    String getStudyId();
    Boolean isPatientAttribute();
}