package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.domain.resource.ResourceUrlTemplate;
import org.cbioportal.legacy.model.ResourceData;
import org.cbioportal.legacy.persistence.ResourceDataRepository;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceDataMyBatisRepository implements ResourceDataRepository {

  @Autowired private ResourceDataMapper resourceDataMapper;

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

    return retainTemplateMetadata(
        resourceDataMapper.getResourceDataOfSampleInStudy(
            studyId,
            sampleId,
            resourceId,
            projection,
            pageSize,
            PaginationCalculator.offset(pageSize, pageNumber),
            sortBy,
            direction));
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

    return retainTemplateMetadata(
        resourceDataMapper.getResourceDataOfPatientInStudy(
            studyId,
            patientId,
            resourceId,
            projection,
            pageSize,
            PaginationCalculator.offset(pageSize, pageNumber),
            sortBy,
            direction));
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

    return retainTemplateMetadata(
        resourceDataMapper.getResourceDataForStudy(
            studyId,
            resourceId,
            projection,
            pageSize,
            PaginationCalculator.offset(pageSize, pageNumber),
            sortBy,
            direction));
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

    return retainTemplateMetadata(
        resourceDataMapper.getResourceDataOfPatientInStudy(
            studyId,
            null,
            resourceId,
            projection,
            pageSize,
            PaginationCalculator.offset(pageSize, pageNumber),
            sortBy,
            direction));
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

    return retainTemplateMetadata(
        resourceDataMapper.getResourceDataOfSampleInStudy(
            studyId,
            null,
            resourceId,
            projection,
            pageSize,
            PaginationCalculator.offset(pageSize, pageNumber),
            sortBy,
            direction));
  }

  /**
   * Keeps of each row's metadata only what its URL template reads, and drops it from rows that do
   * not need it.
   *
   * <p>The rows are cached (the study-wide ones in the static repository cache), and a templated
   * row's full metadata JSON can be large. Trimming here, as the rows are loaded, keeps that cost
   * out of the cache while leaving the controller everything it needs to expand the URL.
   */
  static List<ResourceData> retainTemplateMetadata(List<ResourceData> rows) {
    if (rows == null) {
      return null;
    }
    for (ResourceData row : rows) {
      if (row.getMetadata() == null) {
        continue;
      }
      if (row.getUrlTemplate() == null || ResourceUrlTemplate.hasUrl(row.getUrl())) {
        row.setMetadata(null);
      } else {
        row.setMetadata(
            ResourceUrlTemplate.retainTemplateMetadata(row.getMetadata(), row.getUrlTemplate()));
      }
    }
    return rows;
  }
}
