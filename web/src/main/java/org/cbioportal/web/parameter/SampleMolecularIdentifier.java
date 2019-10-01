package org.cbioportal.web.parameter;

import java.io.Serializable;

public class SampleMolecularIdentifier implements Serializable {

    private String sampleId;
    private String molecularProfileId;

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }
}
