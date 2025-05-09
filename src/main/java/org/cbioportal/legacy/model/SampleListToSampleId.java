package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class SampleListToSampleId implements Serializable {

  private Integer sampleListId;
  @NotNull private String sampleId;

  public Integer getSampleListId() {
    return sampleListId;
  }

  public void setSampleListId(Integer sampleListId) {
    this.sampleListId = sampleListId;
  }

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }
}
