package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.Map;
import org.cbioportal.domain.resource.ResourceServing;
import org.cbioportal.domain.resource.repository.ResourceServingRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseResourceServingRepository implements ResourceServingRepository {

  private final ClickhouseResourceServingMapper mapper;

  public ClickhouseResourceServingRepository(ClickhouseResourceServingMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public ResourceServing getServing(
      String studyId, String resourceId, String key, String patientId) {
    Map<String, Object> row = mapper.getServing(studyId, resourceId, key, patientId);
    return row == null ? null : servingOf(resourceId, key, row);
  }

  /**
   * The serving data of a row that has a sealed source and a thumbnail size, or {@code null}. The
   * data provider checks the rest of the serving object upstream; the tile server authenticates the
   * sealed source against the key.
   */
  static ResourceServing servingOf(String resourceId, String key, Map<String, Object> row) {
    Object sealedSource = row.get("sealed_source");
    int width = intValue(row.get("thumbnail_width"));
    int height = intValue(row.get("thumbnail_height"));
    if (!(sealedSource instanceof String)
        || ((String) sealedSource).isBlank()
        || width <= 0
        || height <= 0) {
      return null;
    }
    return new ResourceServing(resourceId, key, (String) sealedSource, width, height);
  }

  private static int intValue(Object value) {
    return value instanceof Number ? ((Number) value).intValue() : 0;
  }
}
