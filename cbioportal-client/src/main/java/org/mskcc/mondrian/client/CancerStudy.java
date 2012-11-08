package org.mskcc.mondrian.client;

public class CancerStudy {
    private String description;
    private String studyId;
    private String name;

    public CancerStudy(String studyId, String name, String description) {
        this.studyId = studyId;
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String toString() {
    	return name;
    }
}