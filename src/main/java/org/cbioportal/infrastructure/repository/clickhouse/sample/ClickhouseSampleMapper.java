package org.cbioportal.infrastructure.repository.clickhouse.sample;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.meta.BaseMeta;

/**
 * Mapper interface for retrieving sample data from ClickHouse. This interface provides methods for
 * fetching filtered samples and sample counts based on the study view filter context.
 */
public interface ClickhouseSampleMapper {

  /**
   * Retrieves filtered samples based on the study view filter context.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @return a list of filtered samples
   */
  List<Sample> getFilteredSamples(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  /**
   * Retrieves the sample count based on the study view filter context.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @return the sample count
   */
  int getSampleCount(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  BaseMeta getMetaSamples(
      List<String> studyIds, String patientId, List<String> sampleIds, String keyword);

  BaseMeta getMetaSamplesBySampleListIds(List<String> sampleListIds);

  List<Sample> getSamples(
      List<String> studyIds,
      String patientId,
      List<String> sampleIds,
      String keyword,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  List<Sample> getSummarySamples(
      List<String> studyIds,
      String patientId,
      List<String> sampleIds,
      String keyword,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  List<Sample> getDetailedSamples(
      List<String> studyIds,
      String patientId,
      List<String> sampleIds,
      String keyword,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  List<Sample> getSamplesBySampleListIds(List<String> sampleListIds);

  List<Sample> getSummarySamplesBySampleListIds(List<String> sampleListIds);

  List<Sample> getDetailedSamplesBySampleListIds(List<String> sampleListIds);

  Sample getSample(String studyId, String sampleId);
}
