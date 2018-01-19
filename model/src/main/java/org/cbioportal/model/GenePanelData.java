package org.cbioportal.model;

public class GenePanelData extends UniqueKeyBase {
    
    private String molecularProfileId;
    private String sampleId;
    private String patientId;
    private String studyId;
    private String genePanelId;
    private Integer entrezGeneId;
    private Boolean sequenced;
    private Boolean wholeExomeSequenced;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getGenePanelId() {
        return genePanelId;
    }

    public void setGenePanelId(String genePanelId) {
        this.genePanelId = genePanelId;
    }

	public Integer getEntrezGeneId() {
		return entrezGeneId;
	}

	public void setEntrezGeneId(Integer entrezGeneId) {
		this.entrezGeneId = entrezGeneId;
	}

	public Boolean getSequenced() {
		return sequenced;
	}

	public void setSequenced(Boolean sequenced) {
		this.sequenced = sequenced;
	}

	public Boolean getWholeExomeSequenced() {
		return wholeExomeSequenced;
	}

	public void setWholeExomeSequenced(Boolean wholeExomeSequenced) {
		this.wholeExomeSequenced = wholeExomeSequenced;
	}
}
