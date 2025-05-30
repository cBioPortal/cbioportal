package org.cbioportal.legacy.model;

import java.io.Serializable;

public class ResourceCount extends ResourceDefinition implements Serializable {
    private Integer sampleCount;
    private Integer patientCount;

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Integer getPatientCount() {
        return patientCount;
    }

    public void setPatientCount(Integer patientCount) {
        this.patientCount = patientCount;
    }
}
