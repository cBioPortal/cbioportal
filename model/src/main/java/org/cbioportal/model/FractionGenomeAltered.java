package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class FractionGenomeAltered implements Serializable {

    private String studyId;
    private String sampleId;
    private BigDecimal value;

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
