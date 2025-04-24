package org.cbioportal.application.file.model;

public class GenePanelMatrixItem {
    private Integer rowKey;
    private String sampleStableId;
    private String geneticProfileStableId;
    private String genePanelStableId;

    public Integer getRowKey() {
        return rowKey;
    }

    public void setRowKey(Integer rowKey) {
        this.rowKey = rowKey;
    }

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public String getGeneticProfileStableId() {
        return geneticProfileStableId;
    }

    public void setGeneticProfileStableId(String geneticProfileStableId) {
        this.geneticProfileStableId = geneticProfileStableId;
    }

    public String getGenePanelStableId() {
        return genePanelStableId;
    }

    public void setGenePanelStableId(String genePanelStableId) {
        this.genePanelStableId = genePanelStableId;
    }
}
