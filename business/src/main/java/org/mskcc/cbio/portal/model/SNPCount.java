package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class SNPCount implements Serializable {
    private String sampleId;
    private String referenceAllele;
    private String tumorAllele;
    private Integer count;

    public SNPCount(
        String sampleId,
        String referenceAllele,
        String tumorAllele,
        Integer count
    ) {
        this.sampleId = sampleId;
        this.referenceAllele = referenceAllele;
        this.tumorAllele = tumorAllele;
        this.count = count;
    }

    public SNPCount() {}

    public String getSampleId() {
        return sampleId;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public String getTumorAllele() {
        return tumorAllele;
    }

    public Integer getCount() {
        return count;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public void setTumorAllele(String tumorAllele) {
        this.tumorAllele = tumorAllele;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
