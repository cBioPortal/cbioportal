package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateUniqueKey;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalDataRepository;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.cbioportal.legacy.web.parameter.Projection;

public class VSAwareClinicalDataRepository implements ClinicalDataRepository {
  private final VirtualizationService virtualizationService;
  private final ClinicalDataRepository clinicalDataRepository;
  private final VSAwarePatientRepository patientRepository;
  private final VSAwareSampleRepository sampleRepository;

  public VSAwareClinicalDataRepository(
      VirtualizationService virtualizationService,
      ClinicalDataRepository clinicalDataRepository,
      VSAwarePatientRepository patientRepository,
      VSAwareSampleRepository sampleRepository) {
    this.virtualizationService = virtualizationService;
    this.clinicalDataRepository = clinicalDataRepository;
    this.patientRepository = patientRepository;
    this.sampleRepository = sampleRepository;
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
    List<String> ids = null;
    if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equals(clinicalDataType)) {
      ids =
          sampleRepository
              .getAllSamplesInStudy(studyId, Projection.ID.name(), null, null, null, null)
              .stream()
              .map(Sample::getStableId)
              .toList();
    } else {
      ids =
          patientRepository
              .getAllPatientsInStudy(studyId, Projection.ID.name(), null, null, null, null)
              .stream()
              .map(Patient::getStableId)
              .toList();
    }
    List<String> studyIds = ids.stream().map(i -> studyId).toList();
    Stream<ClinicalData> resultStream =
        fetchClinicalData(studyIds, ids, List.of(attributeId), clinicalDataType, projection)
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
        getAllClinicalDataInStudy(
                studyId,
                attributeId,
                clinicalDataType,
                Projection.ID.name(),
                null,
                null,
                null,
                null)
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
    if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equals(clinicalDataType)) {
      return virtualizationService.handleStudySampleData(
          studyIds,
          ids,
          ClinicalData::getStudyId,
          ClinicalData::getSampleId,
          (stids, sids) ->
              clinicalDataRepository.fetchClinicalData(
                  stids, sids, attributeIds, clinicalDataType, projection),
          this::virtualizeClinicalData);
    }
    return virtualizationService.handleStudyPatientData(
        studyIds,
        ids,
        ClinicalData::getStudyId,
        ClinicalData::getPatientId,
        (stids, pids) ->
            clinicalDataRepository.fetchClinicalData(
                stids, pids, attributeIds, clinicalDataType, projection),
        this::virtualizeClinicalData);
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
    return virtualizationService.handleStudySampleData2(
        visibleSampleIds,
        ClinicalData::getStudyId,
        ClinicalData::getSampleId,
        clinicalDataRepository::getSampleClinicalDataBySampleIds,
        this::virtualizeClinicalData);
  }

  @Override
  public List<ClinicalData> getPatientClinicalDataBySampleIds(
      List<StudyScopedId> visibleSampleIds) {
    return virtualizationService.handleStudySampleData2(
        visibleSampleIds,
        ClinicalData::getStudyId,
        ClinicalData::getSampleId,
        clinicalDataRepository::getPatientClinicalDataBySampleIds,
        this::virtualizeClinicalData);
  }

  private ClinicalData virtualizeClinicalData(String virtualStudyId, ClinicalData clinicalData) {
    ClinicalData virtualClinicalData = new ClinicalData();
    virtualClinicalData.setStudyId(virtualStudyId);
    virtualClinicalData.setSampleId(clinicalData.getSampleId());
    virtualClinicalData.setPatientId(clinicalData.getPatientId());
    virtualClinicalData.setAttrId(clinicalData.getAttrId());
    virtualClinicalData.setAttrValue(clinicalData.getAttrValue());

    virtualClinicalData.setUniquePatientKey(
        calculateUniqueKey(virtualStudyId, clinicalData.getUniquePatientKey()));
    virtualClinicalData.setUniqueSampleKey(
        calculateUniqueKey(virtualStudyId, clinicalData.getUniqueSampleKey()));

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
