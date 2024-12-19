package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class ClinicalViolinPlotRowData implements Serializable {
    private String category;
    private Integer numSamples;
    private List<Double> curveData;
    private ClinicalViolinPlotBoxData boxData;
    private List<ClinicalViolinPlotIndividualPoint> individualPoints;

    @Override
    public String toString() {
        return "ClinicalViolinPlotRowData{" +
            "category='" + category + '\'' +
            ", numSamples=" + numSamples +
            ", curveData=" + curveData +
            ", boxData=" + boxData +
            ", individualPoints=" + individualPoints +
            '}';
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(Integer numSamples) {
        this.numSamples = numSamples;
    }

    public List<Double> getCurveData() {
        return curveData;
    }

    public void setCurveData(List<Double> curveData) {
        this.curveData = curveData;
    }

    public ClinicalViolinPlotBoxData getBoxData() {
        return boxData;
    }

    public void setBoxData(ClinicalViolinPlotBoxData boxData) {
        this.boxData = boxData;
    }

    public List<ClinicalViolinPlotIndividualPoint> getIndividualPoints() {
        return individualPoints;
    }

    public void setIndividualPoints(List<ClinicalViolinPlotIndividualPoint> individualPoints) {
        this.individualPoints = individualPoints;
    }

}
