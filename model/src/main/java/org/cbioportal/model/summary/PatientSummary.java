package org.cbioportal.model.summary;

import org.cbioportal.model.ClinicalData;

import java.io.Serializable;
import java.util.List;

public class PatientSummary implements Serializable {

    private Integer internalId;
    private String stableId;
    private Integer cancerStudyId;
    private List<ClinicalData> clinicalDataList;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public List<ClinicalData> getClinicalDataList() {
        return clinicalDataList;
    }

    public void setClinicalDataList(List<ClinicalData> clinicalDataList) {
        this.clinicalDataList = clinicalDataList;
    }
}
