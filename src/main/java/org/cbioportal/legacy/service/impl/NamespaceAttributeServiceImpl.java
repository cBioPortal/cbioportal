package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.persistence.NamespaceRepository;
import org.cbioportal.legacy.service.NamespaceAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceAttributeServiceImpl implements NamespaceAttributeService {

  private final NamespaceRepository namespaceRepository;

  @Autowired
  public NamespaceAttributeServiceImpl(NamespaceRepository namespaceRepository) {
    this.namespaceRepository = namespaceRepository;
  }

  @Override
  public List<NamespaceAttribute> fetchNamespaceAttributes(List<String> studyIds) {
    if (studyIds == null || studyIds.isEmpty()) {
      return new ArrayList<>();
    }

    List<NamespaceAttribute> outerNamespaceKeys =
        namespaceRepository.getNamespaceOuterKey(studyIds);

    if (outerNamespaceKeys == null) {
      return new ArrayList<>();
    }

    return outerNamespaceKeys.stream()
        .flatMap(
            outerNamespaceKey -> {
              List<NamespaceAttribute> innerKeys =
                  namespaceRepository.getNamespaceInnerKey(
                      outerNamespaceKey.getOuterKey(), studyIds);
              return innerKeys == null ? Stream.empty() : innerKeys.stream();
            })
        .toList();
  }

  @Override
  public List<NamespaceAttributeCount> fetchNamespaceAttributeCountsBySampleIds(
      List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes) {

    return namespaceRepository.getNamespaceAttributeCountsBySampleIds(
        studyIds, sampleIds, namespaceAttributes);
  }
}
