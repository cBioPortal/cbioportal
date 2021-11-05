package org.cbioportal.model;

import java.util.List;

public class ClinicalViolinPlotData {
    private List<ClinicalViolinPlotRowData> rows;
    private Double axisStart;
    private Double axisEnd;

    public List<ClinicalViolinPlotRowData> getRows() {
        return rows;
    }

    public void setRows(List<ClinicalViolinPlotRowData> rows) {
        this.rows = rows;
    }

    public Double getAxisStart() {
        return axisStart;
    }

    public void setAxisStart(Double axisStart) {
        this.axisStart = axisStart;
    }

    public Double getAxisEnd() {
        return axisEnd;
    }

    public void setAxisEnd(Double axisEnd) {
        this.axisEnd = axisEnd;
    }
}
