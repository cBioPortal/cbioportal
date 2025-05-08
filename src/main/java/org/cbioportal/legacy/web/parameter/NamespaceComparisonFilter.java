package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;

public class NamespaceComparisonFilter implements Serializable {

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<SampleIdentifier> sampleIdentifiers;

  private NamespaceAttribute namespaceAttribute;
  private List<String> values;

  @AssertTrue
  private boolean isBothSampleIdentifiersAndNamespaceAttributePresent() {
    return sampleIdentifiers != null && namespaceAttribute != null;
  }

  public List<SampleIdentifier> getSampleIdentifiers() {
    return sampleIdentifiers;
  }

  public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
    this.sampleIdentifiers = sampleIdentifiers;
  }

  public NamespaceAttribute getNamespaceAttribute() {
    return namespaceAttribute;
  }

  public void setNamespaceAttribute(NamespaceAttribute namespaceAttribute) {
    this.namespaceAttribute = namespaceAttribute;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }
}
