package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateUniqueKey;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceDataCount;
import org.cbioportal.legacy.persistence.NamespaceRepository;

public class VSAwareNamespaceRepository implements NamespaceRepository {

  private final VirtualizationService virtualizationService;
  private final NamespaceRepository namespaceRepository;

  public VSAwareNamespaceRepository(
      VirtualizationService virtualizationService, NamespaceRepository namespaceRepository) {
    this.virtualizationService = virtualizationService;
    this.namespaceRepository = namespaceRepository;
  }

  @Override
  public List<NamespaceAttribute> getNamespaceOuterKey(List<String> studyIds) {
    Map<String, Set<String>> materializedStudyIds =
        virtualizationService.toMaterializedStudyIds(studyIds);
    return getNamespaceOuterKey(materializedStudyIds.keySet().stream().toList());
  }

  @Override
  public List<NamespaceAttribute> getNamespaceInnerKey(String outerKey, List<String> studyIds) {
    Map<String, Set<String>> materializedStudyIds =
        virtualizationService.toMaterializedStudyIds(studyIds);
    return getNamespaceInnerKey(outerKey, materializedStudyIds.keySet().stream().toList());
  }

  @Override
  public List<NamespaceAttributeCount> getNamespaceAttributeCountsBySampleIds(
      List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes) {
    Pair<List<String>, List<String>> idsListsPair =
        virtualizationService.toMaterializedStudySampleIds(studyIds, sampleIds);
    return namespaceRepository.getNamespaceAttributeCountsBySampleIds(
        idsListsPair.getLeft(), idsListsPair.getRight(), namespaceAttributes);
  }

  @Override
  public List<NamespaceDataCount> getNamespaceDataCounts(
      List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey) {
    Pair<List<String>, List<String>> idsListsPair =
        virtualizationService.toMaterializedStudySampleIds(studyIds, sampleIds);
    return namespaceRepository.getNamespaceDataCounts(
        idsListsPair.getLeft(), idsListsPair.getRight(), outerKey, innerKey);
  }

  @Override
  public List<NamespaceData> getNamespaceData(
      List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey) {
    return virtualizationService.handleStudySampleData(
        studyIds,
        sampleIds,
        NamespaceData::getStudyId,
        NamespaceData::getSampleId,
        (stids, sids) -> namespaceRepository.getNamespaceData(stids, sids, outerKey, innerKey),
        this::virtualizeNamespaceData);
  }

  @Override
  public List<NamespaceData> getNamespaceDataForComparison(
      List<String> studyIds,
      List<String> sampleIds,
      String outerKey,
      String innerKey,
      String value) {
    return virtualizationService.handleStudySampleData(
        studyIds,
        sampleIds,
        NamespaceData::getStudyId,
        NamespaceData::getSampleId,
        (stids, sids) -> namespaceRepository.getNamespaceData(stids, sids, outerKey, innerKey),
        this::virtualizeNamespaceData);
  }

  private NamespaceData virtualizeNamespaceData(
      String virtualizeStudyId, NamespaceData namespaceData) {
    NamespaceData virtualizedData = new NamespaceData();
    virtualizedData.setStudyId(virtualizeStudyId);
    virtualizedData.setOuterKey(namespaceData.getOuterKey());
    virtualizedData.setInnerKey(namespaceData.getInnerKey());
    virtualizedData.setAttrValue(namespaceData.getAttrValue());
    virtualizedData.setSampleId(namespaceData.getSampleId());
    virtualizedData.setPatientId(namespaceData.getPatientId());
    virtualizedData.setUniquePatientKey(
        calculateUniqueKey(namespaceData.getUniquePatientKey(), virtualizeStudyId));
    virtualizedData.setUniqueSampleKey(
        calculateUniqueKey(namespaceData.getUniqueSampleKey(), virtualizeStudyId));
    return virtualizedData;
  }

  private NamespaceDataCount virtualizeNamespaceDataCount(
      String virtualizeStudyId, NamespaceDataCount namespaceDataCount) {
    NamespaceDataCount virtualizedDataCount = new NamespaceDataCount();
    virtualizedDataCount.setTotalCount(namespaceDataCount.getTotalCount());
    virtualizedDataCount.setCount(namespaceDataCount.getCount());
    virtualizedDataCount.setValue(namespaceDataCount.getValue());
    return virtualizedDataCount;
  }
}
