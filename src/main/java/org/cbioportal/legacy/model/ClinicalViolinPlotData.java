package org.cbioportal.legacy.model;

import java.io.Serializable;
import java.util.List;

public class ClinicalViolinPlotData implements Serializable {
  private List<ClinicalViolinPlotRowData> rows;
  private Double axisStart;
  private Double axisEnd;
  private Boolean patientAttribute;

  @Override
  public String toString() {
    return "ClinicalViolinPlotData{"
        + "rows="
        + rows
        + ", axisStart="
        + axisStart
        + ", axisEnd="
        + axisEnd
        + ", patientAttribute="
        + patientAttribute
        + '}';
  }

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

  public Boolean getPatientAttribute() {
    return patientAttribute;
  }

  public void setPatientAttribute(Boolean patientAttribute) {
    this.patientAttribute = patientAttribute;
  }
}
