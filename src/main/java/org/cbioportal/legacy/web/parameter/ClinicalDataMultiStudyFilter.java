package org.cbioportal.legacy.web.parameter;

import static org.cbioportal.legacy.web.parameter.PagingConstants.MAX_PAGE_SIZE;

import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import org.cbioportal.legacy.web.ClinicalDataController;

public class ClinicalDataMultiStudyFilter implements Serializable {

  @Size(min = 1, max = ClinicalDataController.CLINICAL_DATA_MAX_PAGE_SIZE)
  private List<ClinicalDataIdentifier> identifiers;

  @Size(min = 1, max = MAX_PAGE_SIZE)
  private List<String> attributeIds;

  public List<ClinicalDataIdentifier> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(List<ClinicalDataIdentifier> identifiers) {
    this.identifiers = identifiers;
  }

  public List<String> getAttributeIds() {
    return attributeIds;
  }

  public void setAttributeIds(List<String> attributeIds) {
    this.attributeIds = attributeIds;
  }
}
