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

  private static final int TILE_METADATA_SCHEMA_VERSION = 2;
  private static final int MAX_DECODE_PIXELS = 16_777_216;
  private static final String DECODE_POLICY_VERSION =
      "geometry-v2;tile-max=16777216;thumbnail-max=16777216";

  private final ClickhouseWsiSlideAccessMapper mapper;
  private final ClickhouseWsiContextMapper contextMapper;
  private final ObjectMapper objectMapper;

  public ClickhouseWsiSlideAccessRepository(
      ClickhouseWsiSlideAccessMapper mapper,
      ClickhouseWsiContextMapper contextMapper,
      ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.contextMapper = contextMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public WsiSlideAccess getSlideAccess(String studyId, String imageId) {
    Map<String, Object> context = contextMapper.getStudyContext(studyId);
    if (context == null) {
      return null;
    }
    Map<String, Object> row =
        mapper.getSlideAccess(
            longValue(context.get("cancer_study_id")),
            stringValue(context.get("release_id")),
            imageId);
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
    boolean isCurrentSchema =
        metadata != null
            && metadata.tileMetadataSchemaVersion() != null
            && metadata.tileMetadataSchemaVersion() == TILE_METADATA_SCHEMA_VERSION;
    if (metadata == null
        || metadata.dimensions() == null
        || metadata.dimensions().width() <= 0
        || metadata.dimensions().height() <= 0
        || metadata.levels() <= 0
        || metadata.levelDimensions() == null
        || metadata.levelDimensions().size() != metadata.levels()
        || metadata.maxZoom() < 0
        || (metadata.safeMinLevel() != null
            && (metadata.safeMinLevel() < 0 || metadata.safeMinLevel() > metadata.maxZoom()))
        || (metadata.tileMetadataSchemaVersion() != null
            && metadata.tileMetadataSchemaVersion() != TILE_METADATA_SCHEMA_VERSION)
        || (isCurrentSchema
            && (metadata.safeMinLevel() == null
                || metadata.levelDownsamples() == null
                || metadata.levelDownsamples().size() != metadata.levels()
                || metadata.levelDownsamples().stream()
                    .anyMatch(value -> value == null || !Double.isFinite(value) || value <= 0)))
        || (isCurrentSchema
            && (metadata.maxDecodePixels() == null
                || metadata.maxDecodePixels() != MAX_DECODE_PIXELS
                || metadata.thumbnailMaxDecodePixels() == null
                || metadata.thumbnailMaxDecodePixels() != MAX_DECODE_PIXELS
                || !DECODE_POLICY_VERSION.equals(metadata.decodePolicyVersion())))
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

  private static long longValue(Object value) {
    return value == null ? 0L : ((Number) value).longValue();
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
