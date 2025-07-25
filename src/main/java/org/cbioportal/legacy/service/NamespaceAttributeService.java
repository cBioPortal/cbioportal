package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;

public interface NamespaceAttributeService {

  List<NamespaceAttribute> fetchNamespaceAttributes(List<String> studyIds);

  List<NamespaceAttributeCount> fetchNamespaceAttributeCountsBySampleIds(
      List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes);
}
