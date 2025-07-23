package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import org.cbioportal.legacy.model.ResourceData;
import org.cbioportal.legacy.persistence.ResourceDataRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.sort.ResourceDataSortBy;

public class VSAwareResourceDataRepository implements ResourceDataRepository {

  private final VirtualizationService virtualizationService;
  private final ResourceDataRepository resourceDataRepository;

  public VSAwareResourceDataRepository(
      VirtualizationService virtualizationService, ResourceDataRepository resourceDataRepository) {
    this.virtualizationService = virtualizationService;
    this.resourceDataRepository = resourceDataRepository;
  }

  @Override
  public List<ResourceData> getAllResourceDataOfSampleInStudy(
      String studyId,
      String sampleId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    var resultStream =
        virtualizationService
            .handleStudySampleData(
                List.of(sampleId),
                List.of(sampleId),
                ResourceData::getStudyId,
                ResourceData::getSampleId,
                (studyIds, sampleIds) ->
                    resourceDataRepository.getAllResourceDataOfSampleInStudy(
                        studyIds.getFirst(),
                        sampleIds.getFirst(),
                        resourceId,
                        projection,
                        null,
                        null,
                        null,
                        null),
                this::virtualizeResourceData)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Comparator<ResourceData> composeComparator(String sortBy, String direction) {
    ResourceDataSortBy rd = ResourceDataSortBy.valueOf(sortBy);
    Comparator<ResourceData> result =
        switch (rd) {
          case ResourceId -> Comparator.comparing(ResourceData::getResourceId);
          case url -> Comparator.comparing(ResourceData::getUrl);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public List<ResourceData> getAllResourceDataOfPatientInStudy(
      String studyId,
      String patientId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    var resultStream =
        virtualizationService
            .handleStudyPatientData(
                List.of(patientId),
                List.of(patientId),
                ResourceData::getStudyId,
                ResourceData::getPatientId,
                (studyIds, patientIds) ->
                    resourceDataRepository.getAllResourceDataOfPatientInStudy(
                        studyIds.getFirst(),
                        patientIds.getFirst(),
                        resourceId,
                        projection,
                        null,
                        null,
                        null,
                        null),
                this::virtualizeResourceData)
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
  public List<ResourceData> getAllResourceDataForStudy(
      String studyId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    var resultStream =
        virtualizationService
            .handleStudyData(
                List.of(studyId),
                ResourceData::getStudyId,
                studyIds ->
                    resourceDataRepository.getAllResourceDataForStudy(
                        studyIds.getFirst(), resourceId, projection, null, null, null, null),
                this::virtualizeResourceData)
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
  public List<ResourceData> getResourceDataForAllPatientsInStudy(
      String studyId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    var resultStream =
        virtualizationService
            .handleStudyData(
                List.of(studyId),
                ResourceData::getStudyId,
                studyIds ->
                    resourceDataRepository.getResourceDataForAllPatientsInStudy(
                        studyIds.getFirst(), resourceId, projection, null, null, null, null),
                this::virtualizeResourceData)
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
  public List<ResourceData> getResourceDataForAllSamplesInStudy(
      String studyId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    var resultStream =
        virtualizationService
            .handleStudyData(
                List.of(studyId),
                ResourceData::getStudyId,
                studyIds ->
                    resourceDataRepository.getResourceDataForAllSamplesInStudy(
                        studyIds.getFirst(), resourceId, projection, null, null, null, null),
                this::virtualizeResourceData)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private ResourceData virtualizeResourceData(String virtualStudyId, ResourceData resourceData) {
    ResourceData virtualizedResourceData = new ResourceData();
    virtualizedResourceData.setStudyId(virtualStudyId);
    virtualizedResourceData.setSampleId(resourceData.getSampleId());
    virtualizedResourceData.setPatientId(resourceData.getPatientId());
    virtualizedResourceData.setResourceId(resourceData.getResourceId());
    virtualizedResourceData.setUrl(resourceData.getUrl());
    virtualizedResourceData.setResourceDefinition(resourceData.getResourceDefinition());
    return virtualizedResourceData;
  }
}
