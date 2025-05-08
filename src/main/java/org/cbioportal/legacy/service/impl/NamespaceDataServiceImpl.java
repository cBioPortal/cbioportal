package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceDataCount;
import org.cbioportal.legacy.model.NamespaceDataCountItem;
import org.cbioportal.legacy.persistence.NamespaceRepository;
import org.cbioportal.legacy.service.NamespaceDataService;
import org.cbioportal.legacy.web.parameter.NamespaceDataFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceDataServiceImpl implements NamespaceDataService {

  private final NamespaceRepository namespaceRepository;

  @Autowired
  public NamespaceDataServiceImpl(NamespaceRepository namespaceRepository) {
    this.namespaceRepository = namespaceRepository;
  }

  @Override
  public List<NamespaceData> fetchNamespaceData(
      List<String> studyIds,
      List<String> sampleIds,
      List<NamespaceDataFilter> namespaceDataFitlers) {

    if (sampleIds.isEmpty()) {
      return new ArrayList<>();
    }

    return namespaceDataFitlers.stream()
        .map(
            namespaceDataFilter -> {
              String outerKey = namespaceDataFilter.getOuterKey();
              String innerKey = namespaceDataFilter.getInnerKey();

              return namespaceRepository.getNamespaceData(studyIds, sampleIds, outerKey, innerKey);
            })
        .flatMap(List::stream)
        .toList();
  }

  @Override
  public List<NamespaceData> fetchNamespaceDataForComparison(
      List<String> studyIds,
      List<String> sampleIds,
      NamespaceAttribute namespaceAttribute,
      List<String> values) {

    if (sampleIds.isEmpty()) {
      return new ArrayList<>();
    }

    return values.stream()
        .map(
            value -> {
              String outerKey = namespaceAttribute.getOuterKey();
              String innerKey = namespaceAttribute.getInnerKey();

              return namespaceRepository.getNamespaceDataForComparison(
                  studyIds, sampleIds, outerKey, innerKey, value);
            })
        .flatMap(List::stream)
        .toList();
  }

  @Override
  public List<NamespaceDataCountItem> fetchNamespaceDataCounts(
      List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes) {

    return namespaceAttributes.stream()
        .map(
            namespaceAttribute -> {
              String outerKey = namespaceAttribute.getOuterKey();
              String innerKey = namespaceAttribute.getInnerKey();

              List<NamespaceDataCount> namespaceDataCounts =
                  namespaceRepository.getNamespaceDataCounts(
                      studyIds, sampleIds, outerKey, innerKey);

              if (namespaceDataCounts == null || namespaceDataCounts.isEmpty()) {
                return null;
              }

              NamespaceDataCountItem namespaceDataCountItem = new NamespaceDataCountItem();
              namespaceDataCountItem.setOuterKey(outerKey);
              namespaceDataCountItem.setInnerKey(innerKey);
              namespaceDataCountItem.setCounts(namespaceDataCounts);

              return namespaceDataCountItem;
            })
        .toList();
  }
}
