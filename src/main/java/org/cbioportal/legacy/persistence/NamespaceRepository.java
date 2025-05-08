package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceDataCount;

public interface NamespaceRepository {

  List<NamespaceAttribute> getNamespaceOuterKey(List<String> studyIds);

  List<NamespaceAttribute> getNamespaceInnerKey(String outerKey, List<String> studyIds);

  List<NamespaceAttributeCount> getNamespaceAttributeCountsBySampleIds(
      List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes);

  List<NamespaceDataCount> getNamespaceDataCounts(
      List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey);

  List<NamespaceData> getNamespaceData(
      List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey);

  List<NamespaceData> getNamespaceDataForComparison(
      List<String> studyIds,
      List<String> sampleIds,
      String outerKey,
      String innerKey,
      String value);
}
