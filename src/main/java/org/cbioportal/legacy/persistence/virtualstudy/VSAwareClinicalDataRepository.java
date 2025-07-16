package org.cbioportal.legacy.persistence.virtualstudy;

import com.esotericsoftware.minlog.Log;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalDataRepository;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VSAwareClinicalDataRepository implements ClinicalDataRepository {
  private final VirtualStudyService virtualStudyService;
  private final ClinicalDataRepository clinicalDataRepository;
  private final VSAwarePatientRepository vsAwarePatientRepository;

  public VSAwareClinicalDataRepository(
      VirtualStudyService virtualStudyService,
      ClinicalDataRepository clinicalDataRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    this.virtualStudyService = virtualStudyService;
    this.clinicalDataRepository = clinicalDataRepository;
    this.vsAwarePatientRepository = vsAwarePatientRepository;
  }

  @Override
  public List<ClinicalData> getAllClinicalDataOfSampleInStudy(
      String studyId,
      String sampleId,
      String attributeId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Stream<ClinicalData> resultStream =
        fetchClinicalData(
            List.of(studyId),
            List.of(sampleId),
            List.of(attributeId),
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE,
            projection)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  private Comparator<ClinicalData> composeComparator(String sortBy, String direction) {
    return (a, b) -> 0;
    // TODO this is not used by frontend, but might be useful in the future. Implementation is not
    // complete
    /*
    ClinicalDataSortBy ca = ClinicalDataSortBy.valueOf(sortBy);
    Comparator<ClinicalData> result =
        switch (ca) {
          case patientId -> Comparator.comparing(ClinicalData::getPatientId);
          case clinicalAttributeId -> Comparator.comparing(ClinicalData::getAttrId);
          case value -> Comparator.comparing(ClinicalData::getAttrValue);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
     */
  }

  @Override
  public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchClinicalData(
                List.of(studyId),
                List.of(sampleId),
                List.of(attributeId),
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE,
                null)
            .size());
    return baseMeta;
  }

  @Override
  public List<ClinicalData> getAllClinicalDataOfPatientInStudy(
      String studyId,
      String patientId,
      String attributeId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Stream<ClinicalData> resultStream =
        fetchClinicalData(
            List.of(studyId),
            List.of(patientId),
            attributeId == null ? null : List.of(attributeId),
            "PATIENT",
            projection)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  @Override
  public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchClinicalData(
                List.of(studyId), List.of(patientId), List.of(attributeId), "PATIENT", null)
            .size());
    return baseMeta;
  }

  @Override
  public List<ClinicalData> getAllClinicalDataInStudy(
      String studyId,
      String attributeId,
      String clinicalDataType,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Stream<ClinicalData> resultStream =
        // FIXME NPE
        fetchClinicalData(
            List.of(studyId), null, List.of(attributeId), clinicalDataType, projection)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  @Override
  public BaseMeta getMetaAllClinicalData(
      String studyId, String attributeId, String clinicalDataType) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        // FIXME NPE
        fetchClinicalData(List.of(studyId), null, List.of(attributeId), clinicalDataType, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<ClinicalData> fetchAllClinicalDataInStudy(
      String studyId,
      List<String> ids,
      List<String> attributeIds,
      String clinicalDataType,
      String projection) {
    return fetchClinicalData(List.of(studyId), ids, attributeIds, clinicalDataType, projection);
  }

  @Override
  public BaseMeta fetchMetaClinicalDataInStudy(
      String studyId, List<String> ids, List<String> attributeIds, String clinicalDataType) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchClinicalData(
                List.of(studyId), ids, attributeIds, clinicalDataType, Projection.ID.name())
            .size());
    return baseMeta;
  }

  @Override
  public List<ClinicalData> fetchClinicalData(
      List<String> studyIds,
      List<String> ids,
      List<String> attributeIds,
      String clinicalDataType,
      String projection) {
    List<VirtualStudy> allVirtualStudies = virtualStudyService.getPublishedVirtualStudies();
    Map<String, VirtualStudy> allVirtualStudyIds =
        allVirtualStudies.stream()
            .collect(Collectors.toMap(VirtualStudy::getId, virtualStudy -> virtualStudy));
    List<String> virtualStudyIds = new ArrayList<>();
    List<String> virtualIds = new ArrayList<>();
    List<String> materializedStudyIds = new ArrayList<>();
    List<String> materializedIds = new ArrayList<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      if (allVirtualStudyIds.containsKey(studyId)) {
        virtualStudyIds.add(studyId);
        virtualIds.add(ids.get(i));
      } else {
        materializedStudyIds.add(studyId);
        materializedIds.add(ids.get(i));
      }
    }
    List<ClinicalData> result = new ArrayList<>();
    if (!materializedStudyIds.isEmpty()) {
      result.addAll(
          clinicalDataRepository.fetchClinicalData(
              materializedStudyIds, materializedIds, attributeIds, clinicalDataType, projection));
    }
    if (!virtualStudyIds.isEmpty()) {
      if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
        Map<String, Map<String, ImmutablePair<String, String>>> studySamplesByVirtualStudyId =
            new HashMap<>();
        LinkedHashSet<String> vMaterializedStudyIds = new LinkedHashSet<>();
        LinkedHashSet<String> vMaterializedSampleIds = new LinkedHashSet<>();
        Map<ImmutablePair<String, String>, LinkedHashSet<String>>
            virtualStudyIdsByMaterializedSamples = new HashMap<>();
        for (int i = 0; i < virtualStudyIds.size(); i++) {
          String virtualStudyId = virtualStudyIds.get(i);
          String virtualSample = virtualIds.get(i);
          Map<String, ImmutablePair<String, String>> studySamples;
          if (studySamplesByVirtualStudyId.containsKey(virtualStudyId)) {
            studySamples = studySamplesByVirtualStudyId.get(virtualStudyId);
          } else {
            VirtualStudy virtualStudy = allVirtualStudyIds.get(virtualStudyId);
            studySamples =
                virtualStudy.getData().getStudies().stream()
                    .flatMap(
                        vss ->
                            vss.getSamples().stream()
                                .map(s -> new ImmutableTriple<>(s, vss.getId(), s)))
                    .collect(
                        Collectors.toMap(
                            ImmutableTriple::getLeft,
                            triple -> new ImmutablePair<>(triple.getMiddle(), triple.getRight())));
            studySamplesByVirtualStudyId.put(virtualStudyId, studySamples);
          }
          ImmutablePair<String, String> materializedSample = studySamples.get(virtualSample);
          if (materializedSample != null) {
            vMaterializedStudyIds.add(materializedSample.getLeft());
            vMaterializedSampleIds.add(materializedSample.getRight());
            virtualStudyIdsByMaterializedSamples.computeIfAbsent(
                materializedSample, k -> new LinkedHashSet<>());
            virtualStudyIdsByMaterializedSamples.get(materializedSample).add(virtualStudyId);
          } else {
            Log.trace(
                "VSAwareSampleRepository",
                "Virtual sample "
                    + virtualSample
                    + " not found in virtual study "
                    + virtualStudyId);
          }
        }
        for (ClinicalData clinicalData :
            clinicalDataRepository.fetchClinicalData(
                vMaterializedStudyIds.stream().toList(),
                vMaterializedSampleIds.stream().toList(),
                attributeIds,
                clinicalDataType,
                projection)) {
          LinkedHashSet<String> sampleRequestingVirtualStudyIds =
              virtualStudyIdsByMaterializedSamples.get(
                  ImmutablePair.of(clinicalData.getStudyId(), clinicalData.getSampleId()));
          if (sampleRequestingVirtualStudyIds == null
              || sampleRequestingVirtualStudyIds.isEmpty()) {
            throw new IllegalStateException(
                "Virtual study IDs not found for materialized sample: "
                    + clinicalData.getSampleId());
          }
          sampleRequestingVirtualStudyIds.forEach(
              virtualStudyId -> result.add(virtualizeClinicalData(virtualStudyId, clinicalData)));
        }
      } else {
        Map<String, Set<String>> patientIdsByVirtualStudyId = new HashMap<>();
        for (int i = 0; i < virtualIds.size(); i++) {
          String virtualStudyId = virtualStudyIds.get(i);
          String virtualPatientId = virtualIds.get(i);
          if (!patientIdsByVirtualStudyId.containsKey(virtualStudyId)) {
            patientIdsByVirtualStudyId.put(virtualStudyId, new LinkedHashSet<>());
          }
          patientIdsByVirtualStudyId.get(virtualStudyId).add(virtualPatientId);
        }
        // TODO might be not performant to fetch all patients for each virtual study
        for (Map.Entry<String, Set<String>> entry : patientIdsByVirtualStudyId.entrySet()) {
          String virtualStudyId = entry.getKey();
          Set<String> virtualPatientIds = entry.getValue();
          VirtualStudy virtualStudy = allVirtualStudyIds.get(virtualStudyId);
          List<String> vsDefStudyIds =
              virtualStudy.getData().getStudies().stream()
                  .flatMap(s -> s.getSamples().stream().map(s1 -> s.getId()))
                  .collect(Collectors.toList());
          List<String> vsDefSampleIds =
              virtualStudy.getData().getStudies().stream()
                  .flatMap(s -> s.getSamples().stream())
                  .collect(Collectors.toList());
          // TODO reuse patient id calculation
          // TODO Optimize. filtering all patients in the database can be expensive
          List<Patient> patients =
              vsAwarePatientRepository.getPatientsOfSamples(vsDefStudyIds, vsDefSampleIds).stream()
                  .filter(p -> virtualPatientIds.contains(p.getStableId()))
                  .toList();
          result.addAll(
              clinicalDataRepository
                  .fetchClinicalData(
                      patients.stream().map(Patient::getCancerStudyIdentifier).toList(),
                      patients.stream().map(Patient::getStableId).toList(),
                      attributeIds,
                      clinicalDataType,
                      projection)
                  .stream()
                  .map(cd -> virtualizeClinicalData(virtualStudyId, cd))
                  .toList());
        }
      }
    }
    return result;
  }

  @Override
  public BaseMeta fetchMetaClinicalData(
      List<String> studyIds, List<String> ids, List<String> attributeIds, String clinicalDataType) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchClinicalData(studyIds, ids, attributeIds, clinicalDataType, Projection.ID.name())
            .size());
    return baseMeta;
  }

  @Override
  public List<ClinicalDataCount> fetchClinicalDataCounts(
      List<String> studyIds,
      List<String> sampleIds,
      List<String> attributeIds,
      String clinicalDataType,
      String projection) {
    List<ClinicalData> clinicalData =
        fetchClinicalData(studyIds, sampleIds, attributeIds, clinicalDataType, projection);
    return clinicalData.stream()
        .collect(
            Collectors.groupingBy(cld -> ImmutablePair.of(cld.getAttrId(), cld.getAttrValue())))
        .entrySet()
        .stream()
        .map(
            entry -> {
              ClinicalDataCount clinicalDataCount = new ClinicalDataCount();
              clinicalDataCount.setAttributeId(entry.getKey().getLeft());
              clinicalDataCount.setValue(entry.getKey().getRight());
              clinicalDataCount.setCount(entry.getValue().size());
              return clinicalDataCount;
            })
        .toList();
  }

  @Override
  public List<ClinicalData> getPatientClinicalDataDetailedToSample(
      List<String> studyIds, List<String> patientIds, List<String> attributeIds) {
    return fetchClinicalData(
        studyIds, patientIds, attributeIds, "PATIENT", Projection.DETAILED.name());
  }

  @Override
  public List<StudyScopedId> getVisibleSampleIdsForClinicalTable(
      List<String> studyIds,
      List<String> sampleIds,
      Integer pageSize,
      Integer pageNumber,
      String searchTerm,
      String sortBy,
      String direction) {
    Stream<ClinicalData> resultStream =
        fetchClinicalData(
            studyIds,
            sampleIds,
            null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE,
            Projection.ID.name())
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    // TODO optimize
    return resultStream
        .map(cd -> new StudyScopedId(cd.getStudyId(), cd.getSampleId()))
        .distinct()
        .toList();
  }

  @Override
  public List<ClinicalData> getSampleClinicalDataBySampleIds(List<StudyScopedId> visibleSampleIds) {
    Map<StudyScopedId, Set<String>> studySamplePairsByStudyIds =
        virtualStudyService.toMaterializedStudySamplePairsMap(visibleSampleIds);
    List<ClinicalData> result = new ArrayList<>();
    Set<String> virtualStudyIds = virtualStudyService.getPublishedVirtualStudyIds();
    for (ClinicalData clinicalData :
        clinicalDataRepository.getSampleClinicalDataBySampleIds(
            studySamplePairsByStudyIds.keySet().stream().toList())) {
      Set<String> studyIds =
          studySamplePairsByStudyIds.get(
              new StudyScopedId(clinicalData.getStudyId(), clinicalData.getSampleId()));
      for (String studyId : studyIds) {
        if (virtualStudyIds.contains(studyId)) {
          result.add(virtualizeClinicalData(studyId, clinicalData));
        } else {
          result.add(clinicalData);
        }
      }
    }
    return result;
  }

  @Override
  public List<ClinicalData> getPatientClinicalDataBySampleIds(
      List<StudyScopedId> visibleSampleIds) {
    Map<StudyScopedId, Set<String>> studySamplePairsByStudyIds =
        virtualStudyService.toMaterializedStudySamplePairsMap(visibleSampleIds);
    List<ClinicalData> result = new ArrayList<>();
    Set<String> virtualStudyIds = virtualStudyService.getPublishedVirtualStudyIds();
    for (ClinicalData clinicalData :
        clinicalDataRepository.getPatientClinicalDataBySampleIds(
            studySamplePairsByStudyIds.keySet().stream().toList())) {
      Set<String> studyIds =
          studySamplePairsByStudyIds.get(
              new StudyScopedId(clinicalData.getStudyId(), clinicalData.getSampleId()));
      for (String studyId : studyIds) {
        if (virtualStudyIds.contains(studyId)) {
          result.add(virtualizeClinicalData(studyId, clinicalData));
        } else {
          result.add(clinicalData);
        }
      }
    }
    return result;
  }

  private ClinicalData virtualizeClinicalData(String virtualStudyId, ClinicalData clinicalData) {
    ClinicalData virtualClinicalData = new ClinicalData();
    virtualClinicalData.setStudyId(virtualStudyId);
    virtualClinicalData.setSampleId(clinicalData.getSampleId());
    virtualClinicalData.setPatientId(clinicalData.getPatientId());
    virtualClinicalData.setAttrId(clinicalData.getAttrId());
    virtualClinicalData.setAttrValue(clinicalData.getAttrValue());

    // FIXME: these are nulls
    if (clinicalData.getUniquePatientKey() != null) {
      virtualClinicalData.setUniquePatientKey(
          virtualStudyId + "_" + clinicalData.getUniquePatientKey());
    }
    if (clinicalData.getUniqueSampleKey() != null) {
      virtualClinicalData.setUniqueSampleKey(
          virtualStudyId + "_" + clinicalData.getUniqueSampleKey());
    }

    ClinicalAttribute virtualClinicalAttribute = new ClinicalAttribute();
    ClinicalAttribute clinicalAttribute = clinicalData.getClinicalAttribute();
    if (clinicalAttribute != null) {
      virtualClinicalAttribute.setAttrId(clinicalAttribute.getAttrId());
      virtualClinicalAttribute.setDisplayName(clinicalAttribute.getDisplayName());
      virtualClinicalAttribute.setDescription(clinicalAttribute.getDescription());
      virtualClinicalAttribute.setDatatype(clinicalAttribute.getDatatype());
      virtualClinicalAttribute.setPatientAttribute(clinicalAttribute.getPatientAttribute());
      virtualClinicalAttribute.setPriority(clinicalAttribute.getPriority());
      virtualClinicalAttribute.setCancerStudyIdentifier(virtualStudyId);
    }
    virtualClinicalData.setClinicalAttribute(virtualClinicalAttribute);
    return virtualClinicalData;
  }
}
