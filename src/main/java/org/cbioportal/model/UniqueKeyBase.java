package org.cbioportal.model;

import java.io.Serializable;

public abstract class UniqueKeyBase implements Serializable {
    
    private String uniqueSampleKey;
    private String uniquePatientKey;

    public String getUniqueSampleKey() {
        return uniqueSampleKey;
    }

    public void setUniqueSampleKey(String uniqueSampleKey) {
        this.uniqueSampleKey = uniqueSampleKey;
    }

    public String getUniquePatientKey() {
        return uniquePatientKey;
    }

    public void setUniquePatientKey(String uniquePatientKey) {
        this.uniquePatientKey = uniquePatientKey;
    }
}
