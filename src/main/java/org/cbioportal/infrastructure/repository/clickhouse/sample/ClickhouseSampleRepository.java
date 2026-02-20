package org.cbioportal.infrastructure.repository.clickhouse.sample;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseSampleRepository implements SampleRepository {

  private final ClickhouseSampleMapper mapper;

  public ClickhouseSampleRepository(ClickhouseSampleMapper clickhouseSampleMapper) {
    this.mapper = clickhouseSampleMapper;
  }

  @Override
  public List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext) {
    return mapper.getFilteredSamples(studyViewFilterContext);
  }

  @Override
  public int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext) {
    return mapper.getSampleCount(studyViewFilterContext);
  }

  @Override
  public List<Sample> fetchSamples(
      List<String> studyIds, List<String> sampleIds, ProjectionType projection) {
    return switch (projection) {
      case ID -> mapper.getSamples(studyIds, null, sampleIds, null, 0, 0, null, null);
      case SUMMARY -> mapper.getSummarySamples(studyIds, null, sampleIds, null, 0, 0, null, null);
      case DETAILED -> mapper.getDetailedSamples(studyIds, null, sampleIds, null, 0, 0, null, null);
      default -> Collections.emptyList();
    };
  }

  @Override
  public List<Sample> fetchSamplesBySampleListIds(
      List<String> sampleListIds, ProjectionType projection) {
    return switch (projection) {
      case ID -> mapper.getSamplesBySampleListIds(sampleListIds);
      case SUMMARY -> mapper.getSummarySamplesBySampleListIds(sampleListIds);
      case DETAILED -> mapper.getDetailedSamplesBySampleListIds(sampleListIds);
      default -> Collections.emptyList();
    };
  }

  @Override
  public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
    return mapper.getMetaSamples(studyIds, null, sampleIds, null);
  }

  @Override
  public BaseMeta fetchMetaSamplesBySampleListIds(List<String> sampleListIds) {
    return mapper.getMetaSamplesBySampleListIds(sampleListIds);
  }

  @Override
  public List<Sample> getAllSamplesInStudy(
      String studyId,
      ProjectionType projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return switch (projection) {
      case ID ->
          mapper.getSamples(
              Collections.singletonList(studyId),
              null,
              null,
              null,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sortBy,
              direction);
      case SUMMARY ->
          mapper.getSummarySamples(
              Collections.singletonList(studyId),
              null,
              null,
              null,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sortBy,
              direction);
      case DETAILED ->
          mapper.getDetailedSamples(
              Collections.singletonList(studyId),
              null,
              null,
              null,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sortBy,
              direction);
      default -> Collections.emptyList();
    };
  }

  @Override
  public BaseMeta getMetaSamplesInStudy(String studyId) {
    return mapper.getMetaSamples(Collections.singletonList(studyId), null, null, null);
  }

  @Override
  public Sample getSampleInStudy(String studyId, String sampleId) {
    return mapper.getSample(studyId, sampleId);
  }

  @Override
  public List<Sample> getAllSamplesOfPatientInStudy(
      String studyId,
      String patientId,
      ProjectionType projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return switch (projection) {
      case ID ->
          mapper.getSamples(
              Collections.singletonList(studyId),
              patientId,
              null,
              null,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sortBy,
              direction);
      case SUMMARY ->
          mapper.getSummarySamples(
              Collections.singletonList(studyId),
              patientId,
              null,
              null,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sortBy,
              direction);
      case DETAILED ->
          mapper.getDetailedSamples(
              Collections.singletonList(studyId),
              patientId,
              null,
              null,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sortBy,
              direction);
      default -> Collections.emptyList();
    };
  }

  @Override
  public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
    return mapper.getMetaSamples(Collections.singletonList(studyId), patientId, null, null);
  }

  @Override
  public List<Sample> getAllSamples(
      String keyword,
      List<String> studyIds,
      ProjectionType projection,
      Integer pageSize,
      Integer pageNumber,
      String sort,
      String direction) {
    return switch (projection) {
      case ID ->
          mapper.getSamples(
              studyIds,
              null,
              null,
              keyword,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sort,
              direction);
      case SUMMARY ->
          mapper.getSummarySamples(
              studyIds,
              null,
              null,
              keyword,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sort,
              direction);
      case DETAILED ->
          mapper.getDetailedSamples(
              studyIds,
              null,
              null,
              keyword,
              pageSize,
              PaginationCalculator.offset(pageSize, pageNumber),
              sort,
              direction);
      default -> Collections.emptyList();
    };
  }

  @Override
  public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
    return mapper.getMetaSamples(studyIds, null, null, keyword);
  }
}
