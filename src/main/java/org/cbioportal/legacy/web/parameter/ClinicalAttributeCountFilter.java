package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class ClinicalAttributeCountFilter implements Serializable {

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<SampleIdentifier> sampleIdentifiers;

  private String sampleListId;

  @AssertTrue
  private boolean isEitherSampleListIdOrSampleIdsPresent() {
    return sampleListId != null ^ sampleIdentifiers != null;
  }

  public List<SampleIdentifier> getSampleIdentifiers() {
    return sampleIdentifiers;
  }

  public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
    this.sampleIdentifiers = sampleIdentifiers;
  }

  public String getSampleListId() {
    return sampleListId;
  }

  public void setSampleListId(String sampleListId) {
    this.sampleListId = sampleListId;
  }
}
