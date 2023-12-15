package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public class SampleList implements Serializable {
    
    private Integer listId;
    @NotNull
    private String stableId;
    private String category;
    private Integer cancerStudyId;
    private String cancerStudyIdentifier;
    private CancerStudy cancerStudy;
    private String name;
    private String description;
    private Integer sampleCount;
    private List<String> sampleIds;
    
    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public CancerStudy getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(CancerStudy cancerStudy) {
        this.cancerStudy = cancerStudy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

	public List<String> getSampleIds() {
		return sampleIds;
	}

	public void setSampleIds(List<String> sampleIds) {
		this.sampleIds = sampleIds;
	}
}
