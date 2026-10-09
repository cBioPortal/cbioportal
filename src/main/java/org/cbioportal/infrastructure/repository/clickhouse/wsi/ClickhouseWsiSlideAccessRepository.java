package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.wsi.WsiDeidentification;
import org.cbioportal.domain.wsi.WsiSlideSource;
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
  private static final Set<String> THUMBNAIL_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
  private static final Set<String> ALLOWED_METADATA_KEYS =
      Set.of(
          "dimensions",
          "levels",
          "level_dimensions",
          "level_downsamples",
          "max_zoom",
          "tile_size",
          "mpp",
          "objective_power",
          "vendor",
          "identity_version",
          "safe_min_level",
          "tile_metadata_schema_version",
          "decode_policy_version",
          "max_decode_pixels",
          "thumbnail_max_decode_pixels",
          "source_fingerprint");

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
  public WsiSlideSource getSlideSource(String studyId, String patientId, String slideKey) {
    if (!WsiDeidentification.isSlideKey(slideKey)) {
      return null;
    }
    Map<String, Object> context = contextMapper.getStudyContext(studyId);
    if (context == null) {
      return null;
    }
    Map<String, Object> row =
        mapper.getSlideAccess(longValue(context.get("cancer_study_id")), patientId, slideKey);
    if (!isServableRow(row, objectMapper) || !slideKey.equals(stringValue(row.get("slide_key")))) {
      return null;
    }
    try {
      WsiTileMetadata metadata =
          objectMapper.readValue(stringValue(row.get("tile_metadata_json")), WsiTileMetadata.class);
      int width = numberValue(row.get("thumbnail_width"));
      int height = numberValue(row.get("thumbnail_height"));
      String contentType =
          stringValue(row.get("thumbnail_content_type")).trim().toLowerCase(Locale.ROOT);
      return new WsiSlideSource(
          slideKey,
          stringValue(row.get("sealed_source")),
          metadata,
          new WsiThumbnail(width, height, contentType));
    } catch (JsonProcessingException | RuntimeException exception) {
      return null;
    }
  }

  /**
   * A row is servable when it is marked servable, carries a valid slide key and a well-formed
   * sealed source, and its tile metadata and thumbnail fields pass the checks below. The sealed
   * source is opaque here: the tile server authenticates it against the slide key and validates the
   * object URIs it contains.
   */
  static boolean isServableRow(Map<String, Object> row, ObjectMapper objectMapper) {
    if (row == null || !boolValue(row.get("can_serve_tiles"))) {
      return false;
    }
    String slideKey = stringValue(row.get("slide_key"));
    String sealedSource = stringValue(row.get("sealed_source"));
    String metadataJson = stringValue(row.get("tile_metadata_json"));
    String contentType = stringValue(row.get("thumbnail_content_type"));
    int width = numberValue(row.get("thumbnail_width"));
    int height = numberValue(row.get("thumbnail_height"));
    if (!WsiDeidentification.isSlideKey(slideKey)
        || !WsiDeidentification.isSealedSource(sealedSource)
        || metadataJson == null
        || contentType == null
        || !THUMBNAIL_CONTENT_TYPES.contains(contentType.trim().toLowerCase(Locale.ROOT))
        || width <= 0
        || height <= 0
        || width > 8192
        || height > 8192) {
      return false;
    }
    try {
      JsonNode metadataNode = objectMapper.readTree(metadataJson);
      if (!metadataNode.isObject()) {
        return false;
      }
      // Metadata is serialized into the portal response. Apply the same
      // fail-closed identifier/date checks as the importer and hierarchy
      // endpoint to string values while ignoring numeric geometry.
      if (containsForbiddenMetadataText(metadataNode)) {
        return false;
      }
      var fieldNames = metadataNode.fieldNames();
      while (fieldNames.hasNext()) {
        if (!ALLOWED_METADATA_KEYS.contains(fieldNames.next())) {
          return false;
        }
      }
      return validMetadata(objectMapper.treeToValue(metadataNode, WsiTileMetadata.class));
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

  private static boolean containsForbiddenMetadataText(JsonNode node) {
    if (node == null) {
      return false;
    }
    if (node.isTextual()) {
      return WsiDeidentification.containsIdentifyingText(node.asText());
    }
    if (node.isObject() || node.isArray()) {
      var children = node.elements();
      while (children.hasNext()) {
        if (containsForbiddenMetadataText(children.next())) {
          return true;
        }
      }
    }
    return false;
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
