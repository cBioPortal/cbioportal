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
    if (!isServableRow(row, objectMapper)) {
      return null;
    }
    String sourceUrl = stringValue(row.get("source_url"));
    String metadataJson = stringValue(row.get("tile_metadata_json"));
    String thumbnailUrl = stringValue(row.get("thumbnail_url"));
    try {
      WsiTileMetadata metadata = objectMapper.readValue(metadataJson, WsiTileMetadata.class);
      int width = numberValue(row.get("thumbnail_width"));
      int height = numberValue(row.get("thumbnail_height"));
      String contentType = stringValue(row.get("thumbnail_content_type"));
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

  static boolean isServableRow(Map<String, Object> row, ObjectMapper objectMapper) {
    if (row == null || !boolValue(row.get("can_serve_tiles"))) {
      return false;
    }
    String sourceUrl = stringValue(row.get("source_url"));
    String metadataJson = stringValue(row.get("tile_metadata_json"));
    String thumbnailUrl = stringValue(row.get("thumbnail_url"));
    String contentType = stringValue(row.get("thumbnail_content_type"));
    int width = numberValue(row.get("thumbnail_width"));
    int height = numberValue(row.get("thumbnail_height"));
    if (sourceUrl == null || metadataJson == null || thumbnailUrl == null
        || contentType == null || width <= 0 || height <= 0
        || width > 8192 || height > 8192) {
      return false;
    }
    try {
      return validMetadata(objectMapper.readValue(metadataJson, WsiTileMetadata.class));
    } catch (JsonProcessingException | RuntimeException exception) {
      return false;
    }
  }

  private static boolean validMetadata(WsiTileMetadata metadata) {
    if (metadata == null
        || metadata.dimensions() == null
        || metadata.dimensions().width() <= 0
        || metadata.dimensions().height() <= 0
        || metadata.levels() <= 0
        || metadata.levelDimensions() == null
        || metadata.levelDimensions().size() != metadata.levels()
        || metadata.maxZoom() < 0
        || metadata.tileSize() <= 0) {
      return false;
    }
    return metadata.levelDimensions().stream()
        .allMatch(level -> level != null && level.width() > 0 && level.height() > 0);
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
