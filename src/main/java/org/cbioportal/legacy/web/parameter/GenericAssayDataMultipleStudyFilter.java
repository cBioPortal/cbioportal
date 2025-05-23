package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class GenericAssayDataMultipleStudyFilter implements Serializable {

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<SampleMolecularIdentifier> sampleMolecularIdentifiers;

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<String> molecularProfileIds;

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<String> genericAssayStableIds;

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

  public List<String> getGenericAssayStableIds() {
    return genericAssayStableIds;
  }

  public void setGenericAssayStableIds(List<String> genericAssayStableIds) {
    this.genericAssayStableIds = genericAssayStableIds;
  }
}
