package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.Map;
import org.cbioportal.domain.wsi.WsiSlideSource;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseWsiSlideAccessRepository implements WsiSlideAccessRepository {

  private final ClickhouseWsiSlideAccessMapper mapper;

  public ClickhouseWsiSlideAccessRepository(ClickhouseWsiSlideAccessMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public WsiSlideSource getSlideSource(String studyId, String patientId, String slideKey) {
    Map<String, Object> row = mapper.getSlideAccess(studyId, patientId, slideKey);
    return row == null ? null : servableSource(slideKey, row);
  }

  /**
   * The capability inputs of a row marked servable, or {@code null} when the row lacks a sealed
   * source or a thumbnail size. The data provider validates the row's contents upstream; the tile
   * server authenticates the sealed source against the slide key.
   */
  static WsiSlideSource servableSource(String slideKey, Map<String, Object> row) {
    Object sealedSource = row.get("sealed_source");
    int width = intValue(row.get("thumbnail_width"));
    int height = intValue(row.get("thumbnail_height"));
    if (!boolValue(row.get("can_serve_tiles"))
        || !(sealedSource instanceof String)
        || ((String) sealedSource).isBlank()
        || width <= 0
        || height <= 0) {
      return null;
    }
    return new WsiSlideSource(slideKey, (String) sealedSource, width, height);
  }

  private static int intValue(Object value) {
    return value instanceof Number ? ((Number) value).intValue() : 0;
  }

  private static boolean boolValue(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return value instanceof Number && ((Number) value).intValue() != 0;
  }
}
