package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.SampleSortBy;

public class VSAwareSampleRepository implements SampleRepository {

  private SampleRepository sampleRepository;
  private VirtualizationService virtualizationService;

  public VSAwareSampleRepository(
      VirtualizationService virtualizationService, SampleRepository sampleRepository) {
    this.virtualizationService = virtualizationService;
    this.sampleRepository = sampleRepository;
  }

  @Override
  public List<Sample> getAllSamples(
      String keyword,
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sort,
      String direction) {
    Stream<Sample> resultStream = fetchSamples(studyIds, projection).stream();
    if (keyword != null) {
      resultStream =
          resultStream.filter(
              sample -> sample.getStableId().toLowerCase().contains(keyword.toLowerCase()));
    }

    if (sort != null) {
      resultStream = resultStream.sorted(composeComparator(sort, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  private Comparator<Sample> composeComparator(String sortBy, String direction) {
    SampleSortBy ca = SampleSortBy.valueOf(sortBy);
    Comparator<Sample> result =
        switch (ca) {
          case sampleId -> Comparator.comparing(Sample::getStableId);
          case sampleType -> Comparator.comparing(Sample::getSampleType);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository
            .getAllSamples(keyword, studyIds, Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<Sample> getAllSamplesInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSamples(
        null, List.of(studyId), projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public BaseMeta getMetaSamplesInStudy(String studyId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository
            .getAllSamplesInStudy(studyId, Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<Sample> getAllSamplesInStudies(
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSamples(null, studyIds, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public Sample getSampleInStudy(String studyId, String sampleId) {
    return fetchSamples(List.of(studyId), List.of(sampleId), Projection.DETAILED.name()).stream()
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Sample> getAllSamplesOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSamplesInStudy(studyId, projection, pageSize, pageNumber, sortBy, direction)
        .stream()
        .filter(sample -> sample.getPatientStableId().equals(patientId))
        .toList();
  }

  @Override
  public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository
            .getAllSamplesOfPatientInStudy(
                studyId, patientId, Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<Sample> getAllSamplesOfPatientsInStudy(
      String studyId, List<String> patientIds, String projection) {
    return getAllSamplesInStudy(studyId, projection, null, null, null, null).stream()
        .filter(sample -> patientIds.contains(sample.getPatientStableId()))
        .toList();
  }

  @Override
  public List<Sample> getSamplesOfPatientsInMultipleStudies(
      List<String> studyIds, List<String> patientIds, String projection) {
    return getAllSamplesInStudies(studyIds, projection, null, null, null, null).stream()
        .filter(sample -> patientIds.contains(sample.getPatientStableId()))
        .toList();
  }

  // TODO simplify this method by reusing the logic from fetchSamples(List<String> studyIds,
  // List<String> sampleIds, String projection)
  public List<Sample> fetchSamples(List<String> studyIds, String projection) {
    return virtualizationService.handleStudySampleData(
        studyIds,
        Sample::getCancerStudyIdentifier,
        Sample::getStableId,
        (stids, sids) -> sampleRepository.fetchSamples(stids, sids, projection),
        this::virtualizeSample);
  }

  private Sample virtualizeSample(String virtualStudyId, Sample sample) {
    Sample virtualSample = new Sample();
    virtualSample.setStableId(sample.getStableId());
    virtualSample.setSampleType(sample.getSampleType());
    virtualSample.setPatientStableId(sample.getPatientStableId());
    virtualSample.setCancerStudyIdentifier(virtualStudyId);
    virtualSample.setSequenced(sample.getSequenced());
    virtualSample.setCopyNumberSegmentPresent(sample.getCopyNumberSegmentPresent());
    // FIXME calculate these in one place
    virtualSample.setUniqueSampleKey(virtualStudyId + "_" + sample.getUniqueSampleKey());
    virtualSample.setUniquePatientKey(virtualStudyId + "_" + sample.getUniquePatientKey());
    return virtualSample;
  }

  @Override
  public List<Sample> fetchSamples(
      List<String> studyIds, List<String> sampleIds, String projection) {
    return virtualizationService.handleStudySampleData(
        studyIds,
        sampleIds,
        Sample::getCancerStudyIdentifier,
        Sample::getStableId,
        (stids, sids) -> sampleRepository.fetchSamples(stids, sids, projection),
        this::virtualizeSample);
  }

  @Override
  public List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection) {
    // FIXME
    return sampleRepository.fetchSamplesBySampleListIds(sampleListIds, projection);
  }

  @Override
  public List<Sample> fetchSampleBySampleListId(String sampleListIds, String projection) {
    // FIXME
    return sampleRepository.fetchSampleBySampleListId(sampleListIds, projection);
  }

  @Override
  public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository.fetchSamples(studyIds, sampleIds, Projection.ID.name()).size());
    return baseMeta;
  }

  @Override
  public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
    return sampleRepository.fetchMetaSamples(sampleListIds);
  }

  @Override
  public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
    return sampleRepository.getSamplesByInternalIds(internalIds);
  }
}
