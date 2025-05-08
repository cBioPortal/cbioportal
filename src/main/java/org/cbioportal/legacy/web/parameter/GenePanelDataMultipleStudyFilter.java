package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class GenePanelDataMultipleStudyFilter implements Serializable {

  @Size(min = 0, max = PagingConstants.MAX_PAGE_SIZE)
  private List<SampleMolecularIdentifier> sampleMolecularIdentifiers;

  @Size(min = 0, max = PagingConstants.MAX_PAGE_SIZE)
  private List<String> molecularProfileIds;

  @AssertTrue
  private boolean isEitherMolecularProfileIdsOrSampleMolecularIdentifiersPresent() {
    return molecularProfileIds != null ^ sampleMolecularIdentifiers != null;
  }

  public List<SampleMolecularIdentifier> getSampleMolecularIdentifiers() {
    return sampleMolecularIdentifiers;
  }

  public void setSampleMolecularIdentifiers(
      List<SampleMolecularIdentifier> sampleMolecularIdentifiers) {
    this.sampleMolecularIdentifiers = sampleMolecularIdentifiers;
  }

  public List<String> getMolecularProfileIds() {
    return molecularProfileIds;
  }

  public void setMolecularProfileIds(List<String> molecularProfileIds) {
    this.molecularProfileIds = molecularProfileIds;
  }
}
