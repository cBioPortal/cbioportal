package org.cbioportal.model;

import java.util.Objects;

public class StudyViewGenePanel {
    private String molecularProfileId;
    private String genePanelId;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getGenePanelId() {
        return genePanelId;
    }

    public void setGenePanelId(String genePanelId) {
        this.genePanelId = genePanelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyViewGenePanel)) return false;
        StudyViewGenePanel that = (StudyViewGenePanel) o;
        return getMolecularProfileId().equals(that.getMolecularProfileId()) && getGenePanelId().equals(that.getGenePanelId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMolecularProfileId(), getGenePanelId());
    }
}
