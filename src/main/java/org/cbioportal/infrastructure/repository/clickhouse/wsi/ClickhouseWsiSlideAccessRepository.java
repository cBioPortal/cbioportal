package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiThumbnail;
import org.cbioportal.domain.wsi.WsiTileMetadata;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseWsiSlideAccessRepository implements WsiSlideAccessRepository {

  private final ClickhouseWsiSlideAccessMapper mapper;
  private final ObjectMapper objectMapper;

  public ClickhouseWsiSlideAccessRepository(
      ClickhouseWsiSlideAccessMapper mapper, ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public WsiSlideAccess getSlideAccess(String studyId, String imageId) {
    Map<String, Object> row = mapper.getSlideAccess(studyId, imageId);
    if (row == null || !boolValue(row.get("can_serve_tiles"))) {
      return null;
    }
    String sourceUrl = stringValue(row.get("source_url"));
    String metadataJson = stringValue(row.get("tile_metadata_json"));
    String thumbnailUrl = stringValue(row.get("thumbnail_url"));
    if (sourceUrl == null || metadataJson == null || thumbnailUrl == null) {
      return null;
    }
    try {
      WsiTileMetadata metadata = objectMapper.readValue(metadataJson, WsiTileMetadata.class);
      int width = numberValue(row.get("thumbnail_width"));
      int height = numberValue(row.get("thumbnail_height"));
      String contentType = stringValue(row.get("thumbnail_content_type"));
      if (metadata == null
          || metadata.dimensions() == null
          || metadata.dimensions().width() <= 0
          || metadata.dimensions().height() <= 0
          || metadata.levels() <= 0
          || metadata.levelDimensions() == null
          || metadata.levelDimensions().isEmpty()
          || metadata.maxZoom() < 0
          || metadata.tileSize() <= 0
          || width <= 0
          || height <= 0
          || width > 8192
          || height > 8192
          || contentType == null) {
        return null;
      }
      return new WsiSlideAccess(
          imageId,
          sourceUrl,
          metadata,
          new WsiThumbnail(thumbnailUrl, width, height, contentType),
          null,
          null,
          0);
    } catch (JsonProcessingException | RuntimeException exception) {
      return null;
    }
  }

  private static String stringValue(Object value) {
    return value == null || value.toString().isBlank() ? null : value.toString();
  }

  private static int numberValue(Object value) {
    return value instanceof Number ? ((Number) value).intValue() : 0;
  }

  private static boolean boolValue(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value == null) {
      return false;
    }
    try {
      return Integer.parseInt(value.toString()) != 0;
    } catch (NumberFormatException exception) {
      return false;
    }
  }
}
