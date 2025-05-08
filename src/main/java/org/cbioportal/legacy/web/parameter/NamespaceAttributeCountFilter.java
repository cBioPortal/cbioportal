package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;

public class NamespaceAttributeCountFilter implements Serializable {

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<SampleIdentifier> sampleIdentifiers;

  private List<NamespaceAttribute> namespaceAttributes;

  @AssertTrue
  private boolean isBothSampleIdentifiersAndNamespaceAttributesPresent() {
    return sampleIdentifiers != null && namespaceAttributes != null;
  }

  public List<SampleIdentifier> getSampleIdentifiers() {
    return sampleIdentifiers;
  }

  public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
    this.sampleIdentifiers = sampleIdentifiers;
  }

  public List<NamespaceAttribute> getNamespaceAttributes() {
    return namespaceAttributes;
  }

  public void setNamespaceAttributes(List<NamespaceAttribute> namespaceAttributes) {
    this.namespaceAttributes = namespaceAttributes;
  }
}
