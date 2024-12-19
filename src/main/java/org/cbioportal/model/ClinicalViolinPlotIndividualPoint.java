package org.cbioportal.model;

import java.io.Serializable;

public class ClinicalViolinPlotIndividualPoint implements Serializable {
    private String sampleId;
    private String studyId;
    private double value;

    @Override
    public String toString() {
        return "ClinicalViolinPlotIndividualPoint{" +
            "sampleId='" + sampleId + '\'' +
            ", studyId='" + studyId + '\'' +
            ", value=" + value +
            '}';
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
